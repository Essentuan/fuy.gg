package com.busted_moments.core.util;

import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.ChronoUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TempMap<Key, Value> implements Map<Key, Value> {
   public class Entry {
      private final Date ENTERED_AT;

      private final Value value;
      private final Duration expiry;

      Entry(Key key, Value value) {
         this.value = value;
         this.expiry = duration.apply(key, value);

         this.ENTERED_AT = new Date();
      }

      Value get() {
         return value;
      }

      public boolean hasExpired() {
         return Duration.since(ENTERED_AT).greaterThanOrEqual(expiry);
      }

      @Override
      @SuppressWarnings("unchecked")
      public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null) return false;

         if (o.getClass() == getClass()) {
            Entry entry = (Entry) o;
            return Objects.equals(value, entry.value);
         }

         return Objects.equals(o, value);
      }

      @Override
      public int hashCode() {
         return value.hashCode();
      }

      @Override
      public String toString() {
         return value.toString();
      }
   }

   private final Map<Key, Entry> map;

   private final BiFunction<Key, Value, Duration> duration;

   public TempMap(Duration expiry) {
      this(expiry, ConcurrentHashMap::new);
   }

   public TempMap(double length, ChronoUnit unit) {
      this(Duration.of(length, unit));
   }

   public TempMap(long length, ChronoUnit unit) {
      this(Duration.of(length, unit));
   }

   public TempMap(long length, ChronoUnit unit, Supplier<Map<Key, Entry>> map) {
      this(Duration.of(length, unit), map);
   }

   public TempMap(long length, ChronoUnit unit, Map<Key, Entry> map) {
      this(length, unit, () -> map);
   }

   public TempMap(Duration expiry, Supplier<Map<Key, Entry>> map) {
      this((key, value) -> expiry, map);
   }

   public TempMap(BiFunction<Key, Value, Duration> expiry) {
      this(expiry, ConcurrentHashMap::new);
   }

   public TempMap(BiFunction<Key, Value, Duration> expiry, Supplier<Map<Key, Entry>> map) {
      this.map = map.get();

      this.duration = expiry;
   }

   public void cleanup(Consumer<Value> consumer) {
      map.values().removeIf(v -> {
         if (v.hasExpired()) {
            consumer.accept(v.get());
            return true;
         } else return false;
      });
   }

   public void cleanup() {
      cleanup(v -> {});
   }

   private Value getAndValidate(Object key) {
      Entry entry = map.get(key);

      if (entry != null) {
         if (entry.hasExpired()) {
            map.remove(key);

            return null;
         }

         return entry.get();
      }

      return null;
   }

   private Value valueOf(Entry entry) {
      return entry == null ? null : entry.get();
   }

   @Override
   public int size() {
      cleanup();

      return map.size();
   }

   @Override
   public boolean isEmpty() {
      cleanup();

      return map.isEmpty();
   }

   @Override
   public boolean containsKey(Object key) {
      return getAndValidate(key) != null;
   }

   @Override
   public boolean containsValue(Object value) {
      for (var it = new Iter<>(entry -> entry); it.hasNext(); ) {
         var entry = it.next();

         if (entry.getValue().equals(value)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public Value get(Object key) {
      return getAndValidate(key);
   }

   @Nullable
   @Override
   public Value put(Key key, Value value) {
      return valueOf(map.put(key, new Entry(key, value)));
   }

   @Override
   public Value remove(Object key) {
      return valueOf(map.remove(key));
   }

   @Override
   public void putAll(@NotNull Map<? extends Key, ? extends Value> m) {
      m.forEach(this::put);
   }

   @Override
   public void clear() {
      map.clear();
   }

   @NotNull
   @Override
   public Set<Key> keySet() {
      return new KeySet();
   }

   @NotNull
   @Override
   public Collection<Value> values() {
      return new Values();
   }

   @NotNull
   @Override
   public Set<Map.Entry<Key, Value>> entrySet() {
      return new EntrySet();
   }

   private class Iter<T> implements Iterator<T> {
      private final Function<Map.Entry<Key, Entry>, T> mapper;

      private final Iterator<Map.Entry<Key, Entry>> iter;
      private Map.Entry<Key, Entry> next;

      Iter(Function<Map.Entry<Key, Entry>, T> mapper) {
         this.mapper = mapper;
         this.iter = map.entrySet().iterator();
      }

      @Override
      public boolean hasNext() {
         if (!iter.hasNext()) return false;

         while (iter.hasNext() && next == null) {
            var next = iter.next();

            if (!next.getValue().hasExpired()) this.next = next;
            else remove();
         }

         return next != null;
      }

      @Override
      public T next() {
         if (next != null) {
            var current = next;
            next = null;

            return mapper.apply(current);
         }

         throw new NoSuchElementException();
      }

      @Override
      public void remove() {
         iter.remove();
      }
   }

   private class Values implements Collection<Value> {
      @Override
      public int size() {
         return TempMap.this.size();
      }

      @Override
      public boolean isEmpty() {
         return TempMap.this.isEmpty();
      }

      @Override
      public boolean contains(Object o) {
         return TempMap.this.containsValue(o);
      }

      @NotNull
      @Override
      public Iterator<Value> iterator() {
         return new Iter<Value>(entry -> entry.getValue().get());
      }

      @NotNull
      @Override
      public Object @NotNull [] toArray() {
         return map.values().stream()
                 .filter(entry -> !entry.hasExpired())
                 .map(Entry::get)
                 .toArray();
      }

      @NotNull
      @Override
      public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
         return map.values().stream()
                 .filter(entry -> !entry.hasExpired())
                 .map(Entry::get)
                 .toList().toArray(a);
      }

      @Override
      public boolean add(Value value) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(Object o) {
         return map.values().remove(o);
      }

      @Override
      public boolean containsAll(@NotNull Collection<?> c) {
         return map.values().containsAll(c);
      }

      @Override
      public boolean addAll(@NotNull Collection<? extends Value> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean removeAll(@NotNull Collection<?> c) {
         return map.values().removeAll(c);
      }

      @Override
      public boolean retainAll(@NotNull Collection<?> c) {
         return map.values().retainAll(c);
      }

      @Override
      public void clear() {
         TempMap.this.clear();
      }
   }

   private class KeySet implements Set<Key> {

      @Override
      public int size() {
         return TempMap.this.size();
      }

      @Override
      public boolean isEmpty() {
         return TempMap.this.isEmpty();
      }

      @Override
      public boolean contains(Object o) {
         return TempMap.this.containsKey(o);
      }

      @NotNull
      @Override
      public Iterator<Key> iterator() {
         return new Iter<>(Map.Entry::getKey);
      }

      @NotNull
      @Override
      public Object[] toArray() {
         return map.entrySet().stream()
                 .filter(entry -> !entry.getValue().hasExpired())
                 .map(Map.Entry::getKey)
                 .toArray();
      }

      @NotNull
      @Override
      public <T> T[] toArray(@NotNull T[] a) {
         return map.entrySet().stream()
                 .filter(entry -> !entry.getValue().hasExpired())
                 .map(Map.Entry::getKey)
                 .toList().toArray(a);
      }

      @Override
      public boolean add(Key key) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(Object o) {
         return TempMap.this.remove(o) != null;
      }

      @Override
      public boolean containsAll(@NotNull Collection<?> c) {
          for (Object obj : c) if (!contains(obj)) return false;

          return true;
      }

      @Override
      public boolean addAll(@NotNull Collection<? extends Key> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean retainAll(@NotNull Collection<?> c) {
         final boolean[] modified = {false};

         removeIf(key -> {
            boolean toRemove = !c.contains(key);
            modified[0] |= toRemove;

            return toRemove;
         });

         return modified[0];
      }

      @Override
      public boolean removeAll(@NotNull Collection<?> c) {
         final boolean[] modified = {false};

         removeIf(key -> {
            boolean toRemove = c.contains(key);
            modified[0] |= toRemove;

            return toRemove;
         });

         return modified[0];
      }

      @Override
      public void clear() {
         TempMap.this.clear();
      }
   }

   private class EntrySet implements Set<Map.Entry<Key, Value>> {
      private class Node implements Map.Entry<Key, Value> {
         private final Map.Entry<Key, Entry> original;

         Node(Map.Entry<Key, Entry> original) {
            this.original = original;
         }

         @Override
         public Key getKey() {
            return original.getKey();
         }

         @Override
         public Value getValue() {
            return valueOf(original.getValue());
         }

         @Override
         public Value setValue(Value value) {
            return valueOf(original.setValue(new Entry(getKey(), value)));
         }

         @Override
         public boolean equals(Object obj) {
            return original.equals(obj);
         }

         @Override
         public int hashCode() {
            return original.hashCode();
         }

         @Override
         public String toString() {
            return "Map.Entry{key=" +
                    getKey().toString() +
                    ", value=" +
                    getValue().toString() + "}";
         }
      }

      @Override
      public int size() {
         return TempMap.this.size();
      }

      @Override
      public boolean isEmpty() {
         return TempMap.this.isEmpty();
      }

      @Override
      public boolean contains(Object o) {
         return map.entrySet().contains(o);
      }

      @NotNull
      @Override
      public Iterator<Map.Entry<Key, Value>> iterator() {
         return new Iter<Map.Entry<Key, Value>>(Node::new);
      }

      @NotNull
      @Override
      public Object[] toArray() {
         return map.entrySet().stream()
                 .filter(entry -> !entry.getValue().hasExpired())
                 .map(Node::new)
                 .toArray();
      }

      @NotNull
      @Override
      public <T> T[] toArray(@NotNull T[] a) {
         return map.entrySet().stream()
                 .filter(entry -> !entry.getValue().hasExpired())
                 .map(Node::new)
                 .toList().toArray(a);
      }

      @Override
      public boolean add(Map.Entry<Key, Value> keyValueEntry) {
         return map.put(keyValueEntry.getKey(), new Entry(keyValueEntry.getKey(), keyValueEntry.getValue())) != null;
      }

      @Override
      public boolean remove(Object o) {
         return map.entrySet().remove(o);
      }

      @Override
      public boolean containsAll(@NotNull Collection<?> c) {
         return map.entrySet().containsAll(c);
      }

      @Override
      public boolean addAll(@NotNull Collection<? extends Map.Entry<Key, Value>> c) {
         boolean added = false;
         for (Map.Entry<Key, Value> e : c) {
            if (add(e))
               added = true;
         }
         return added;
      }

      @Override
      public boolean retainAll(@NotNull Collection<?> c) {
         return map.entrySet().retainAll(c);
      }

      @Override
      public boolean removeAll(@NotNull Collection<?> c) {
         return map.entrySet().removeAll(c);
      }

      @Override
      public void clear() {
         TempMap.this.clear();
      }
   }
}