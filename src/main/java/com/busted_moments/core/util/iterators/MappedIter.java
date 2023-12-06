package com.busted_moments.core.util.iterators;

import java.util.Iterator;
import java.util.function.Function;

public record MappedIter<I, O>(Iterator<I> iter, Function<I, O> mapper, boolean allowRemove) implements Iter<O> {
   @Override
   public boolean hasNext() {
      return iter.hasNext();
   }

   @Override
   public O next() {
      return mapper.apply(iter.next());
   }

   @Override
   public void remove() {
      if (allowRemove)
         iter.remove();
      else
         throw new UnsupportedOperationException();
   }
}
