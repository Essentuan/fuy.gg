package com.busted_moments.core.collector;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import static com.busted_moments.client.FuyMain.LOGGER;

public class EnumMapCollector<T, K extends Enum<K>, V> extends SimpleCollector<T, Map<K, V>, Map<K, V>> {

   private final Class<K> clazz;
   private final Function<T, K> keyMapper;
   private final Function<T, V> valueMapper;

   public EnumMapCollector(Class<K> clazz, Function<T, K> keyMapper, Function<T, V> valueMapper) {
      this.clazz = clazz;
      this.keyMapper = keyMapper;
      this.valueMapper = valueMapper;
   }

   @Override
   protected Map<K, V> supply() {
      return new EnumMap<>(clazz);
   }

   @Override
   protected void accumulate(Map<K, V> container, T value) {
      try {
         var key = keyMapper.apply(value);
         var v = valueMapper.apply(value);

         if (key != null && v != null)
            container.put(key, v);
      } catch (Exception e) {
         LOGGER.info("Error trying to add element {}", value, e);
      }
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