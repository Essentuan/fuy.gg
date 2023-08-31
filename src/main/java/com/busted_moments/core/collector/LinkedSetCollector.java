package com.busted_moments.core.collector;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class LinkedSetCollector<T, V> extends SimpleCollector<T, Set<V>, Set<V>> {
   private final Function<T, V> valueMapper;

   public LinkedSetCollector(Function<T, V> valueMapper) {
      this.valueMapper = valueMapper;
   }

   @Override
   protected Set<V> supply() {
      return new LinkedHashSet<>();
   }

   @Override
   protected void accumulate(Set<V> container, T value) {
      container.add(valueMapper.apply(value));
   }

   @Override
   protected Set<V> combine(Set<V> left, Set<V> right) {
      left.addAll(right);

      return left;
   }

   @Override
   protected Set<V> finish(Set<V> container) {
      return container;
   }
}
