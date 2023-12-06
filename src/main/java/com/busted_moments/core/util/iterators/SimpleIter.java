package com.busted_moments.core.util.iterators;

import java.util.Iterator;

record SimpleIter<T>(Iterator<T> iter) implements Iter<T> {
   @Override
   public boolean hasNext() {
      return iter.hasNext();
   }

   @Override
   public T next() {
      return iter.next();
   }

   @Override
   public void remove() {
      iter.remove();
   }
}
