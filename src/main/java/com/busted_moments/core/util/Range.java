package com.busted_moments.core.util;

import com.busted_moments.core.util.iterators.SimpleIterator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public abstract class Range<T extends Number> implements Set<T> {
   protected final T min;
   protected final T max;

   protected final T step;

   private final int size;

   protected Range(@NotNull T min, @NotNull T max, T step) {
      this.min = min;
      this.max = max;
      this.step = step;

      this.size = (int) ((max.doubleValue() - min.doubleValue())/step.doubleValue());
   }
   protected abstract T increment(T value);

   public T min() {
      return min;
   }

   public T max() {
      return max;
   }

   @Override
   public int size() {
      return size;
   }

   @Override
   public boolean isEmpty() {
      return size() == 0;
   }

   @Override
   public boolean contains(Object o) {
      if (o instanceof Number object) {
         double num = object.doubleValue();

         return num >= min.doubleValue() && num <= max.doubleValue() && num % step.doubleValue() == 0;
      }

      return false;
   }

   @Override
   public @NotNull Iterator<T> iterator() {
      return new Iter();
   }

   private class Iter extends SimpleIterator<T> {
      private T next = min;

      @Override
      protected T compute() {
         T current = next;

         next = increment(next);

         return current;
      }

      @Override
      public boolean hasNext() {
         return next.doubleValue() < max.doubleValue();
      }
   }

   @NotNull
   @Override
   public Object @NotNull [] toArray() {
      return toArray(new Object[size]);
   }

   @NotNull
   @Override
   @SuppressWarnings("unchecked")
   public <E> E @NotNull [] toArray(@NotNull E @NotNull [] a) {
      int i = 0;

      int size = this.size();
      E[] arr = a.length >= size ? a : (E[]) Array.newInstance(a.getClass().getComponentType(), size);

      for (T value : this)
         arr[i++] = (E) value;

      return arr;
   }

   @Override
   public boolean add(T t) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean remove(Object o) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean containsAll(@NotNull Collection<?> collection) {
      for (Object value : collection)
         if (!contains(value))
            return false;

      return true;
   }

   @Override
   public boolean addAll(@NotNull Collection<? extends T> collection) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean removeAll(@NotNull Collection<?> collection) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean removeIf(Predicate<? super T> filter) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean retainAll(@NotNull Collection<?> collection) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void clear() {
      throw new UnsupportedOperationException();
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder("[");

      for (T number : this)
         builder.append(number);

      return builder.append(']').toString();
   }

   public static Range<Integer> between(int min, int max) {
      return new Range<>(min, max, 1) {
         @Override
         protected Integer increment(Integer value) {
            return value + step;
         }
      };
   }


   public static Range<Long> between(long min, long max) {
      return new Range<>(min, max, 1L) {
         @Override
         protected Long increment(Long value) {
            return value + step;
         }
      };
   }

   public static Range<Long> of(List<Long> list) {
      long low = list.get(0);
      long high = list.get(0);

      for (long x : list) {
         if (x < low) {
            low = x;
         }
         if (x > high) {
            high = x;
         }
      }

      return between(low, high);
   }

   public static Range<Float> between(float min, float max, float step) {
      return new Range<>(min, max, step) {
         @Override
         protected Float increment(Float value) {
            return value + step;
         }
      };
   }

   public static Range<Double> between(double min, double max, double step) {
      return new Range<>(min, max, step) {
         @Override
         protected Double increment(Double value) {
            return value + step;
         }
      };
   }
}
