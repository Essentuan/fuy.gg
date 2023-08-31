package com.busted_moments.core.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

public interface Comparing {
   @SafeVarargs
   static <T> Comparator<T> of(Comparator<T>... comparators) {
      Comparator<T> comparator = (o1, o2) -> 0;
      for (Comparator<T> c : comparators) comparator = comparator.thenComparing(c);

      return comparator;
   }

   @SafeVarargs
   @SuppressWarnings({"rawtypes", "unchecked"})
   static <T> Comparator<T> of(Function<T, Comparable>... extractors) {
      return of(Arrays.stream(extractors).map(Comparator::comparing).toArray(Comparator[]::new));
   }
}
