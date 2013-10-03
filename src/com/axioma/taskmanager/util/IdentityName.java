package com.axioma.taskmanager.util;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class IdentityName implements Comparable<IdentityName> {

   public static final String SEPARATOR = ".";
   private static final String MARKET_ATTRIBUTE_PREFIX = "market.";

   public static enum ToStringMode {
      RAW_NAME, NAME, PREFIXED
   }

   private final String rawName;
   private final String prefix;
   private final String name;
   private ToStringMode toStringMode = ToStringMode.RAW_NAME;

   public IdentityName(final String prefix, final String name) {
      this(IdentityName.getRawName(prefix, name));
   }

   @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
   public IdentityName(final String rawName) {
      super();
      Preconditions.checkArgument(!IdentityName.isBlank(rawName));
      this.rawName = rawName;

      /*
       * set name and prefix
       */

      //check if market
      if (this.hasMarketPrefix()) {
         this.prefix = IdentityName.MARKET_ATTRIBUTE_PREFIX;
         final int startIndex = (IdentityName.MARKET_ATTRIBUTE_PREFIX + IdentityName.SEPARATOR).length();
         this.name = this.rawName.substring(startIndex, rawName.length());
      } else {
         //otherwise the prefix is all text to the last dot
         final int separatorIndex = rawName.lastIndexOf(IdentityName.SEPARATOR);
         if (separatorIndex >= 0) {
            this.prefix = rawName.substring(0, separatorIndex);
            this.name = rawName.substring(separatorIndex + 1, rawName.length());
         } else {
            //no prefix
            this.prefix = "";
            this.name = rawName;
         }
      }
   }

   public static IdentityName create(final String name, final ToStringMode mode) {
      Preconditions.checkNotNull(mode);
      final IdentityName identityName = new IdentityName(name);
      identityName.toStringMode = mode;
      return identityName;
   }

   public static IdentityName create(final String prefix, final String name, final ToStringMode mode) {
      Preconditions.checkNotNull(mode);
      final IdentityName identityName = new IdentityName(prefix, name);
      identityName.toStringMode = mode;
      return identityName;
   }

   public static String getRawName(final String prefix, final String name) {
      if (IdentityName.isBlank(prefix)) {
         return name;
      } else {
         return prefix + IdentityName.SEPARATOR + name;
      }
   }

   public String getRawName() {
      return this.rawName;
   }

   public String getPrefix() {
      return this.prefix;
   }

   public String getName() {
      return this.name;
   }

   public boolean hasMarketPrefix() {
      return this.rawName.startsWith(IdentityName.MARKET_ATTRIBUTE_PREFIX + IdentityName.SEPARATOR);
   }

   public static boolean hasMarketPrefix(final String rawName) {
      return rawName.startsWith(IdentityName.MARKET_ATTRIBUTE_PREFIX + IdentityName.SEPARATOR);
   }

   public String toString(final ToStringMode mode) {
      if (ToStringMode.RAW_NAME.equals(mode)) {
         return this.rawName;
      } else if (ToStringMode.PREFIXED.equals(mode)) {
         String prefixed = this.getName();
         if (!IdentityName.isBlank(this.getPrefix())) {
            prefixed = prefixed + " (" + this.getPrefix() + ")";
         }
         return prefixed;
      }
      return this.name;
   }

   @Override
   public String toString() {
      return this.toString(this.toStringMode);
   }

   @Override
   public int compareTo(final IdentityName other) {
      return this.rawName.compareTo(other.rawName);
   }

   @Override
   public boolean equals(final Object obj) {
      if (obj == null) {
         return false;
      }
      if (obj == this) {
         return true;
      }
      if (obj.getClass() != this.getClass()) {
         return false;
      }

      final IdentityName params = (IdentityName) obj;
      return this.rawName.equals(params.rawName);
   }

   @Override
   public int hashCode() {
      return this.rawName.hashCode();
   }

   public static IdentityName valueOf(final String rawName) {
      return new IdentityName(rawName);
   }

   public static List<IdentityName> toIdentityNames(final Collection<String> rawNames) {
      final List<IdentityName> names = Lists.newArrayList();
      for (final String rawName : rawNames) {
         names.add(IdentityName.valueOf(rawName));
      }
      return names;
   }

   public static List<String> toStringsWithoutPrefixes(final Collection<String> rawNames) {
      final List<String> names = Lists.newArrayList();
      for (final String rawName : rawNames) {
         names.add(new IdentityName(rawName).getName());
      }
      return names;
   }

   public static List<IdentityName> toIdentityNames(final Collection<?> objects, final Function<Object, IdentityName> function) {
      final List<IdentityName> names = Lists.newArrayList();
      for (final Object object : objects) {
         names.add(function.apply(object));
      }
      return names;
   }

   private static boolean isBlank(final String str) {
      return str == null || str.trim().equals("");
   }
}