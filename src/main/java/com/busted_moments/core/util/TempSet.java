package com.busted_moments.core.util;

import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;

@SuppressWarnings("ClassEscapesDefinedScope")
public class TempSet<T> implements Set<T> {
   private final Set<T> SET;

   public TempSet(Duration expiry) {
      this(expiry, ConcurrentHashMap::new);
   }

   public TempSet(double length, TimeUnit unit) {
      this(Duration.of(length, unit));
   }

   public TempSet(long length, TimeUnit unit) {
      this(Duration.of(length, unit));
   }

   public TempSet(long length, TimeUnit unit, Supplier<Map<T, TempMap<T, Boolean>.Entry>> map) {
      this(Duration.of(length, unit), map);
   }

   public TempSet(long length, TimeUnit unit, Map<T, TempMap<T, Boolean>.Entry> map) {
      this(length, unit, () -> map);
   }


   public TempSet(Duration expiry, Supplier<Map<T, TempMap<T, Boolean>.Entry>> map) {
      this(value -> expiry, map);
   }

   public TempSet(Function<T, Duration> expiry) {
      this(expiry, ConcurrentHashMap::new);
   }

   public TempSet(Function<T, Duration> expiry, Supplier<Map<T, TempMap<T, Boolean>.Entry>> map) {
      this.SET = Collections.newSetFromMap(new TempMap<T, Boolean>((key, value) -> expiry.apply(key), map));
   }

   public TempSet(Function<T, Duration> expiry, Map<T, TempMap<T, Boolean>.Entry> map) {
      this.SET = Collections.newSetFromMap(new TempMap<T, Boolean>((key, value) -> expiry.apply(key), () -> map));
   }

   @Override
   public int size() {
      return SET.size();
   }

   @Override
   public boolean isEmpty() {
      return SET.isEmpty();
   }

   @Override
   public boolean contains(Object o) {
      return SET.contains(o);
   }

   @NotNull
   @Override
   public Iterator<T> iterator() {
      return SET.iterator();
   }

   @NotNull
   @Override
   public Object @NotNull [] toArray() {
      return SET.toArray();
   }

   @NotNull
   @Override
   public <T1> T1 @NotNull [] toArray(@NotNull T1 @NotNull [] a) {
      return SET.toArray(a);
   }

   @Override
   public boolean add(T t) {
      return SET.add(t);
   }

   @Override
   public boolean remove(Object o) {
      return SET.remove(o);
   }

   @Override
   public boolean containsAll(@NotNull Collection<?> c) {
      return SET.containsAll(c);
   }

   @Override
   public boolean addAll(@NotNull Collection<? extends T> c) {
      return SET.addAll(c);
   }

   @Override
   public boolean retainAll(@NotNull Collection<?> c) {
      return SET.retainAll(c);
   }

   @Override
   public boolean removeAll(@NotNull Collection<?> c) {
      return SET.removeAll(c);
   }

   @Override
   public void clear() {
      SET.clear();
   }
}
