package com.axioma.taskmanager.util;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class ConcurrentMessageDigest {

   /* Lock used to guarantee no two objects are created at the same time */
   private static final Lock CREATION_LOCK = new ReentrantLock();

   private final MessageDigest messageDigest;

   private ConcurrentMessageDigest(final MessageDigest messageDigest) {
      this.messageDigest = messageDigest;
   }

   public void update(final byte input) {
      synchronized (this) {
         this.messageDigest.update(input);
      }
   }

   public void update(final byte[] input, final int offset, final int len) {
      synchronized (this) {
         this.messageDigest.update(input, offset, len);
      }
   }

   public void update(final byte[] input) {
      synchronized (this) {
         this.messageDigest.update(input);
      }
   }

   public byte[] digest() {
      synchronized (this) {
         return this.messageDigest.digest();
      }
   }

   public int digest(final byte[] buf, final int offset, final int len) throws DigestException {
      synchronized (this) {
         return this.messageDigest.digest(buf, offset, len);
      }
   }

   public byte[] digest(final byte[] input) {
      synchronized (this) {
         return this.messageDigest.digest(input);
      }
   }

   @Override
   public String toString() {
      synchronized (this) {
         return this.messageDigest.toString();
      }
   }

   public void reset() {
      synchronized (this) {
         this.messageDigest.reset();
      }
   }

   public static ConcurrentMessageDigest getInstance(final String algorithm) throws NoSuchAlgorithmException {
      ConcurrentMessageDigest.CREATION_LOCK.lock();
      try {
         return new ConcurrentMessageDigest(MessageDigest.getInstance(algorithm));
      } finally {
         ConcurrentMessageDigest.CREATION_LOCK.unlock();
      }
   }

   public static ConcurrentMessageDigest getInstance(final String algorithm, final String provider)
      throws NoSuchAlgorithmException, NoSuchProviderException {
      ConcurrentMessageDigest.CREATION_LOCK.lock();
      try {
         return new ConcurrentMessageDigest(MessageDigest.getInstance(algorithm, provider));
      } finally {
         ConcurrentMessageDigest.CREATION_LOCK.unlock();
      }
   }

   public static ConcurrentMessageDigest getInstance(final String algorithm, final Provider provider)
      throws NoSuchAlgorithmException {
      ConcurrentMessageDigest.CREATION_LOCK.lock();
      try {
         return new ConcurrentMessageDigest(MessageDigest.getInstance(algorithm, provider));
      } finally {
         ConcurrentMessageDigest.CREATION_LOCK.unlock();
      }
   }
}