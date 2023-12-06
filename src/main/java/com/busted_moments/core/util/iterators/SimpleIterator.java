package com.busted_moments.core.util.iterators;

import java.util.NoSuchElementException;

public abstract class SimpleIterator<T> implements Iter<T> {
   protected abstract T compute();

   @Override
   public T next() {
      if (!hasNext()) {
         throw new NoSuchElementException();
      }

      return compute();
   }

   static final SimpleIterator<Object> EMPTY = new SimpleIterator<>() {
      @Override
      protected Object compute() {
         return null;
      }

      @Override
      public boolean hasNext() {
         return false;
      }
   };
}
