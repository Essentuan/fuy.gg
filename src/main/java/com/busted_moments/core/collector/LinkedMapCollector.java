package com.busted_moments.core.collector;

import com.google.common.collect.Multimap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class LinkedMapCollector<T, K, V> extends SimpleCollector<T, Map<K, V>, Map<K, V>> {
   private final Function<T, K> keyMapper;
   private final Function<T, V> valueMapper;

   public LinkedMapCollector(Function<T, K> keyMapper, Function<T, V> valueMapper) {
      this.keyMapper = keyMapper;
      this.valueMapper = valueMapper;
   }

   @Override
   protected Map<K, V> supply() {
      return new LinkedHashMap<>();
   }

   @Override
   protected void accumulate(Map<K, V> container, T value) {
      container.put(
              keyMapper.apply(value),
              valueMapper.apply(value)
      );
   }

   @Override
   protected Map<K, V> combine(Map<K, V> left, Map<K, V> right) {
      left.putAll(right);

      return left;
   }

   @Override
   protected Map<K, V> finish(Map<K, V> container) {
      return container;
   }
}
