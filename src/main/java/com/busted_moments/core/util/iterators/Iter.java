package com.busted_moments.core.util.iterators;

import com.google.common.collect.Iterators;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Iter<T> extends Iterator<T>, Iterable<T> {
   default Iter<T> concat(Iterator<T>... iters) {
      return join(ArrayUtils.addFirst(iters, this));
   }

   default <U> Iter<T> concat(Function<U, T> mapper, Iterator<U>... iters) {
      return Iter.join(
              this,
              Iter.join(iters).map(mapper)
      );
   }

   default <U> Iter<U> flatMap(Function<T, Iterator<U>> mapper) {
      return Iter.of(Iterators.concat(map(mapper)));
   }

   default <U> Iter<U> map(Function<T, U> mapper) {
      return mapped(this, mapper);
   }

   default <U> Iter<U> cast(Class<U> clazz) {
      return map(clazz::cast);
   }

   default <U> Iter<U> cast() {
      return map(o -> (U) o);
   }

   default Iter<T> filter(Predicate<T> predicate) {
      return new FilterIter<>(this, predicate);
   }

   default Iter<T> distinct(Function<T, Object> extractor) {
      HashSet<Object> seen = new HashSet<>();

      return filter(seen::add);
   }

   default Iter<T> distinct() {
      return distinct(e -> e);
   }

   default Iter<T> immutable() {
      return immutable(this);
   }

   default Iter<T> append(T... items) {
      return join(this, of(items));
   }

   default Iter<T> append(Iterable<T> items) {
      return join(this, of(items));
   }

   default Iter<T> append(Iterator<T> items) {
      return join(this, of(items));
   }

   default Iter<T> prepend(T... items) {
      return join(of(items), this);
   }

   default Iter<T> prepend(Iterable<T> items) {
      return join(of(items), this);
   }

   default Iter<T> prepend(Iterator<T> items) {
      return join(of(items), this);
   }


   default Object[] toArray() {
      return toArray(new Object[0]);
   }

   default <U> U[] toArray(@NotNull U[] a) {
      List<T> list = new ArrayList<>();

      for (T value : this)
         list.add(value);

      return list.toArray(a);
   }

   @NotNull
   @Override
   default Iterator<T> iterator() {
      return this;
   }

   default Stream<T> stream() {
      return StreamSupport.stream(
              Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED),
              false
      );
   }

   @SuppressWarnings("unchecked")
   static <T> Iter<T> empty() {
      return (Iter<T>) SimpleIterator.EMPTY;
   }

   static <T> Iter<T> of(Iterable<T> iterable) {
      return of(iterable.iterator());
   }

   static <T> Iter<T> of(Iterator<T> iterator) {
      if (iterator instanceof Iter<T> iter)
         return iter;

      return new SimpleIter<>(iterator);
   }

   @SafeVarargs
   static <T> Iter<T> of(T... items) {
      return new ArrayIter<>(items);
   }

   @SafeVarargs
   static <T> Iter<T> join(Iterator<? extends T>... iters) {
      return of(Iterators.concat(iters));
   }

   static <T> Iter<T> immutable(Iterator<T> base) {
      return immutable(base, Function.identity());
   }

   static <I, O> Iter<O> immutable(Iterator<I> base, Function<I, O> mapper) {
      return new MappedIter<>(base, mapper, false);
   }

   static <I, O> Iter<O> mapped(Iterator<I> base, Function<I, O> mapper) {
      return new MappedIter<>(base, mapper, true);
   }
}
