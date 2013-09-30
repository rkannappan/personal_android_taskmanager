package com.axioma.taskmanager.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.LocalDate;

public final class DigestUtil {

   private static final Charset ENCRYPTION_CHARSET = Charset.forName("UTF-8");

   private static final String DEFAULT_ASSET = "asset";
   private static final LocalDate DEFAULT_LOCAL_DATE = new LocalDate(2005, 03, 16);
   private static final String DEFAULT_ENTITY_NAME = "entityName";

   private ConcurrentMessageDigest hashAlgorithm;

   public DigestUtil() {
      try {
         this.hashAlgorithm = ConcurrentMessageDigest.getInstance("SHA-1");
      } catch (final NoSuchAlgorithmException ex) {
         throw new IllegalStateException("Can't find encryption algorithm for attribute values");
      }
   }

   public long scrambleDouble(final Double inputValue, final String seed1, final LocalDate seed2, final String seed3) {
      final byte[] digestBytes = this.getDigestBytes(seed1, seed2, seed3);
      final long hashVal = ByteBuffer.wrap(digestBytes).getLong();
      Double value;
      if (inputValue == null) {
         value = Double.NaN;
      } else {
         value = inputValue;
      }
      long longVal = Double.doubleToRawLongBits(value);
      longVal = longVal ^ hashVal;
      return longVal;
   }

   public Double unscrambleDouble(final long value, final String seed1, final LocalDate seed2, final String seed3) {
      final byte[] digestBytes = this.getDigestBytes(seed1, seed2, seed3);
      final long hashVal = ByteBuffer.wrap(digestBytes).getLong();
      final long longVal = value ^ hashVal;
      return Double.longBitsToDouble(longVal);
   }

   /**
    * Encodes inputValue using seed1, seed2 and seed3
    * @param inputValue
    * @param minLength
    * @param seed1
    * @param seed2
    * @param seed3
    * @return
    */
   public byte[] scrambleString(final String inputValue, final int minLength, final String seed1, final LocalDate seed2,
            final String seed3) {
      final byte[] digestBytes = this.getDigestBytes(seed1, seed2, seed3);
      String value = inputValue;
      if (value == null) {
         value = "\u0001";
      }
      while (value.length() < minLength) {
         value += "\u0000";
      }
      final byte[] stringBytes = DigestUtil.ENCRYPTION_CHARSET.encode(value).array();
      for (int i = 0; i < stringBytes.length; ++i) {
         stringBytes[i] ^= digestBytes[i % digestBytes.length];
      }
      return stringBytes;
   }

   /**
    * Decodes inputValue using seed1, seed2 and seed3
    * @param inputValue
    * @param seed1
    * @param seed2
    * @param seed3
    * @return
    */
   public String unscrambleString(final byte[] inputValue, final String seed1, final LocalDate seed2, final String seed3) {
      final byte[] digestBytes = this.getDigestBytes(seed1, seed2, seed3);
      final byte[] stringBytes = inputValue;
      for (int i = 0; i < stringBytes.length; ++i) {
         stringBytes[i] ^= digestBytes[i % digestBytes.length];
      }
      final CharBuffer cb = DigestUtil.ENCRYPTION_CHARSET.decode(ByteBuffer.wrap(stringBytes));
      String rawString = cb.toString();
      final int paddingIndex = rawString.indexOf('\u0000');
      if (paddingIndex != -1) {
         rawString = rawString.substring(0, paddingIndex);
      }
      if ((rawString.length() == 1) && (rawString.charAt(0) == '\u0001')) {
         rawString = null;
      }
      return rawString;
   }

   /**
    * Encodes inputValue using default seeds
    * @param inputValue
    * @return
    */
   public String scrambleString(final String inputValue) {
      final byte[] scrambledArray =
               this.scrambleString(inputValue, inputValue.length(), DigestUtil.DEFAULT_ASSET, DigestUtil.DEFAULT_LOCAL_DATE,
                        DigestUtil.DEFAULT_ENTITY_NAME);
      try {
         return new String(Base64.encodeBase64(scrambledArray), "UTF-8");
      } catch (final UnsupportedEncodingException e) {
         throw new IllegalArgumentException(e);
      }
   }

   /**
    * Decodes inputValue using default seeds
    * @param inputValue
    * @return
    */
   public String unscrambleString(final String inputValue) {
      // Make sure that the string they've provided as an encoded string is valid.
      if ((0 == (inputValue.length() % 4)) && Base64.isBase64(inputValue)) {
         return this.unscrambleString(Base64.decodeBase64(inputValue), DigestUtil.DEFAULT_ASSET, DigestUtil.DEFAULT_LOCAL_DATE,
                  DigestUtil.DEFAULT_ENTITY_NAME);
      } else {
         throw new IllegalArgumentException("Input value was not a valid Base64 encoded String.");
      }
   }

   private byte[] getDigestBytes(final String asset, final LocalDate date, final String entityName) {
      final ByteBuffer assetBytes = DigestUtil.ENCRYPTION_CHARSET.encode(asset + entityName);
      final ByteBuffer buffer = ByteBuffer.allocate(assetBytes.limit() + 8);
      buffer.put(assetBytes);
      buffer.putLong((((((date.getYear() - 1900) * 100) + date.getMonthOfYear()) - 1) * 100) + date.getDayOfWeek());
      return this.hashAlgorithm.digest(buffer.array());
   }
}