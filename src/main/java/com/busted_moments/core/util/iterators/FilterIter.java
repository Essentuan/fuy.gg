package com.busted_moments.core.util.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

public class FilterIter<T> implements Iter<T> {
   private static final Object END_MARKER = new Object();

   private final Iterator<T> iter;
   private final Predicate<T> predicate;

   private Object next = null;

   public FilterIter(Iterator<T> iter, Predicate<T> predicate) {
      this.iter = iter;
      this.predicate = predicate;
   }

   private Optional<T> peek() {
      if (next == END_MARKER)
         return Optional.empty();
      else if (next != null)
         return Optional.ofNullable((T) next);
      else if (!iter.hasNext()) {
         finish();

         return Optional.empty();
      }

      while (iter.hasNext() && next == null) {
         var value = iter.next();
         if (predicate.test(value))
            this.next = value;
      }

      if (next == null) {
         finish();

         return Optional.empty();
      }

      return Optional.ofNullable((T) next);
   }
   private void finish() {
      next = END_MARKER;
   }

   @Override
   public boolean hasNext() {
      return peek().isPresent();
   }

   @Override
   public T next() {
      T next = peek().orElseThrow(NoSuchElementException::new);
      this.next = null;

      return next;
   }

   @Override
   public void remove() {
      iter.remove();
   }
}
