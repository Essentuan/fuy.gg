package com.busted_moments.core.util;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClassOrdering implements Comparator<Class<?>> {
   private final Map<Class<?>, Integer> order = new LinkedHashMap<>();

   private Comparator<Class<?>> secondary;

   public ClassOrdering(Class<?>... classes) {
      this((c1, c2) -> 0, classes);
   }

   public ClassOrdering(Comparator<Class<?>> comparator, Class<?>... classes) {
      for (Class<?> clazz : classes) {
         order.put(clazz, order.size());
      }

      this.secondary = comparator;
   }


   @SuppressWarnings("EqualsWithItself")
   @Override
   public int compare(Class<?> o1, Class<?> o2) {
      Integer one = order.getOrDefault(o1, Integer.MIN_VALUE);
      Integer two = order.getOrDefault(o2, Integer.MIN_VALUE);

      var iter = order.entrySet().iterator();

      while((one == Integer.MIN_VALUE || two == Integer.MIN_VALUE) && iter.hasNext()) {
         var entry = iter.next();

         if (one == Integer.MIN_VALUE && entry.getKey().isAssignableFrom(o1)) one = entry.getValue();
         if (two == Integer.MIN_VALUE && entry.getKey().isAssignableFrom(o2)) two = entry.getValue();
      };

      int result = one - two;

      if (result == 0) return secondary.compare(o2, o2);
      else return result;
   }
}
