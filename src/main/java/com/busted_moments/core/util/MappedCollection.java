package com.busted_moments.core.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("SuspiciousMethodCalls")
public class MappedCollection<U, T> implements Collection<T> {
   protected final Collection<U> collection;

   protected final Map<T, U> refMap = new HashMap<>();

   protected final Function<U, T> mapper;

   public MappedCollection(Collection<U> collection, Function<U, T> mapper) {
      this.collection = collection;

      this.mapper = mapper;

      collection.forEach(value -> refMap.put(mapper.apply(value), value));
   }

   @Override
   public int size() {
      return collection.size();
   }

   @Override
   public boolean isEmpty() {
      return collection.isEmpty();
   }

   @Override
   public boolean contains(Object o) {
      return refMap.containsKey(o) && collection.contains(refMap.get(o));
   }

   @NotNull
   @Override
   public Iterator<T> iterator() {
      Iterator<U> iterator = collection.iterator();

      return new Iterator<>() {
         @Override
         public boolean hasNext() {
            return iterator.hasNext();
         }

         @Override
         public T next() {
            return mapper.apply(iterator.next());
         }

         @Override
         public void remove() {
            iterator.remove();
         }
      };
   }

   @NotNull
   @Override
   public Object[] toArray() {
      return collection.stream()
              .map(mapper).toArray();
   }

   @NotNull
   @Override
   public <T1> T1[] toArray(@NotNull T1[] a) {
      return collection.stream()
              .map(mapper).toList().toArray(a);
   }

   @Override
   public boolean add(T t) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean remove(Object o) {
      if (!refMap.containsKey(o)) {
         return false;
      }

      return collection.remove(refMap.get(o));
   }

   @Override
   public boolean containsAll(@NotNull Collection<?> c) {
      return collection.containsAll(c.stream().map(refMap::get).toList());
   }

   @Override
   public boolean addAll(@NotNull Collection<? extends T> c) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean removeAll(@NotNull Collection<?> c) {
      return collection.removeAll(c.stream().map(refMap::get).toList());
   }

   @Override
   public boolean retainAll(@NotNull Collection<?> c) {
      return collection.retainAll(c.stream().map(refMap::get).toList());
   }

   @Override
   public void clear() {
      collection.clear();
   }
}
