package com.busted_moments.core.util.iterators;

public class ArrayIter<T> extends SimpleIterator<T> {
   private final T[] array;
   private int cursor = 0;

   public ArrayIter(T[] array) {
      this.array = array;
   }

   @Override
   protected T compute() {
      return array[cursor++];
   }

   @Override
   public boolean hasNext() {
      return array.length > cursor;
   }
}
