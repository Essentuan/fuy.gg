package com.busted_moments.core.toml;

import com.busted_moments.core.tuples.Pair;
import me.shedaniel.cloth.clothconfig.shadowed.com.moandjiezana.toml.TomlWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.String.format;

class TomlObject implements Toml {
   private final Map<String, Object> map;

   TomlObject() {
      this(new LinkedHashMap<>());
   }

   TomlObject(final String key, final Object value) {
      this();

      set(key, value);
   }

   @SuppressWarnings("unchecked")
   TomlObject(final Map<String, ?> map) {
      this.map = (Map<String, Object>) map;
   }

   private Optional<Pair<String, Toml>> find(String key, boolean create) {
      if (key.contains(".")) {
         String[] keys = key.split("\\.");

         Toml next = this;

         for (int i = 0; i < keys.length - 1; i++) {
            String part = keys[i];

            if (!next.has(part)) {
               if (create) {
                  next.put(part, Toml.empty());
               } else return Optional.empty();
            }

            Object object = next.get(part);

            if (object instanceof Toml toml) {
               next = toml;
            } else
               throw new ClassCastException("Key %s is %s not Toml".formatted(part, object.getClass().getSimpleName()));
         }

         return Optional.of(new Pair<>(keys[keys.length - 1], next));
      } else {
         return Optional.of(new Pair<>(key, this));
      }
   }

   private <T> T operation(Object key, boolean create, Function<Optional<Pair<String, Toml>>, T> operation, Supplier<T> base) {
      if (key instanceof String string && string.contains(".")) {
         return operation.apply(find(string, create));
      } else return base.get();
   }

   @Override
   public int size() {
      return map.size();
   }

   @Override
   public boolean isEmpty() {
      return map.isEmpty();
   }

   @Override
   public boolean containsKey(Object key) {
      return operation(key, false,
              result -> result.isPresent() && result.map(pair -> pair.two().has(pair.one())).get(),
              () -> map.containsKey(key)
      );
   }

   @Override
   public boolean containsValue(Object value) {
      return map.containsValue(value);
   }

   @Override
   public Object get(Object key) {
      return operation(key, false,
              result -> result.map(pair -> pair.two().get(pair.one())).orElse(null),
              () -> map.get(key)
      );
   }

   @Nullable
   @Override
   public Object put(String key, Object value) {
      return operation(key, true,
              result -> result.map(pair -> pair.two().put(pair.one(), value)).orElse(null),
              () -> map.put(key, value)
      );
   }

   @SuppressWarnings("SuspiciousMethodCalls")
   private Object removeKey(Object key) {
      return operation(key, false,
              result -> result.map(pair -> pair.two().remove(pair.one())).orElse(null),
              () -> map.remove(key)
      );
   }

   @Override
   public Object remove(Object key) {
      return removeKey(key);
   }

   @Override
   public void putAll(@NotNull Map<? extends String, ?> m) {
      map.forEach(this::put);
   }

   @Override
   public void clear() {
      map.clear();
   }

   @NotNull
   @Override
   public Set<String> keySet() {
      return map.keySet();
   }

   @NotNull
   @Override
   public Collection<Object> values() {
      return map.values();
   }

   @NotNull
   @Override
   public Set<Entry<String, Object>> entrySet() {
      return map.entrySet();
   }

   @Override
   public Toml set(String key, Object value) {
      put(key, value);

      return this;
   }

   @Override
   public Toml remove(String key) {
      removeKey(key);

      return this;
   }

   @Override
   public <T> T get(String key, Class<T> clazz) {
      return clazz.cast(get(key));
   }

   @Override
   @SuppressWarnings("unchecked")
   public <T> T get(String key, T defaultValue) {
      return get(key, (Class<T>) defaultValue.getClass(), defaultValue);
   }

   @Override
   public <T> T get(String key, Class<T> clazz, T defaultValue) {
      Object res = get(key);

      return res == null ? defaultValue : clazz.cast(res);
   }

   @Override
   public Number getNumber(String key) {
      return get(key, Number.class);
   }

   @Override
   public Number getNumber(String key, Number defaultValue) {
      return get(key, Number.class, defaultValue);
   }

   @Override
   public Integer getInteger(final String key) throws ClassCastException {
      return getNumber(key, Number::intValue);
   }

   @Override
   public int getInteger(final String key, final int defaultValue) throws ClassCastException {
      return getNumber(key, defaultValue, Number::intValue);
   }

   @Override
   public Long getLong(final String key) throws ClassCastException {
      return getNumber(key, Number::longValue);
   }

   @Override
   public long getLong(final String key, final long defaultValue) throws ClassCastException {
      return getNumber(key, defaultValue, Number::longValue);
   }

   @Override
   public Float getFloat(final String key) throws ClassCastException {
      return getNumber(key, Number::floatValue);
   }

   @Override
   public float getFloat(final String key, final float defaultValue) throws ClassCastException {
      return getNumber(key, defaultValue, Number::floatValue);
   }

   @Override
   public Double getDouble(final String key) throws ClassCastException {
      return getNumber(key, Number::doubleValue);
   }

   @Override
   public double getDouble(final String key, final double defaultValue) throws ClassCastException {
      return getNumber(key, defaultValue, Number::doubleValue);
   }
   @Override
   public String getString(String key) {
      return get(key, String.class);
   }

   @Override
   public String getString(String key, String defaultValue) {
      return get(key, defaultValue);
   }

   @Override
   public Boolean getBoolean(String key) {
      return get(key, Boolean.class);
   }

   @Override
   public boolean getBoolean(String key, boolean defaultValue) {
      return get(key, defaultValue);
   }

   @Override
   public Date getDate(String key) {
      if (isType(key, Date.class)) {
         return get(key, Date.class);
      } else {
         return new Date(getLong(key));
      }
   }

   @Override
   public Date getDate(String  key, Date defaultValue) {
      return containsKey(key) ? getDate(key) : defaultValue;
   }

   @SuppressWarnings("unchecked")
   private <T> List<T> constructList(String key, Class<T> clazz, List<T> defaultValue) {
      List<T> value = get(key, List.class);

      if (value == null) return defaultValue;

      for (Object item : value) {
         if (item != null && !clazz.isAssignableFrom(item.getClass())) {
            throw new ClassCastException(format("List element cannot be cast to %s", clazz.getName()));
         }
      }
      return value;
   }

   @Override
   @SuppressWarnings("unchecked")
   public List<Object> getList(String key) {
      return get(key, List.class);
   }

   @Override
   public List<Object> getList(String key, List<Object> defaultValue) {
      List<Object> res = getList(key);

      return res == null ? defaultValue : res;
   }

   @Override
   public <T> List<T> getList(String key, Class<T> clazz) {
      return constructList(key, clazz, null);
   }

   @Override
   public <T> List<T> getList(String key, Class<T> clazz, List<T> defaultValue) {
      return constructList(key, clazz, defaultValue);
   }

   @Override
   public Toml getTable(String key) {
      return get(key, Toml.class);
   }

   @Override
   public Toml getTable(String key, Toml defaultValue) {
      return get(key, Toml.class, defaultValue);
   }

   private TomlWriter build() {
      return new TomlWriter.Builder()
              .build();
   }

   @Override
   public String write() {
      return build().write(this);
   }

   @Override
   public void write(File target) throws IOException {
      build().write(this, target);
   }

   @Override
   public void write(OutputStream target) throws IOException {
      build().write(this, target);
   }

   @Override
   public void write(Writer target) throws IOException {
      build().write(target);
   }

   @Override
   public String toString() {
      return write();
   }

   static <T> Toml from(T value,
                        BiFunction<me.shedaniel.cloth.clothconfig.shadowed.com.moandjiezana.toml.Toml, T, me.shedaniel.cloth.clothconfig.shadowed.com.moandjiezana.toml.Toml> func) {
      return convert(func.apply(new me.shedaniel.cloth.clothconfig.shadowed.com.moandjiezana.toml.Toml(), value));
   }


   static Toml convert(me.shedaniel.cloth.clothconfig.shadowed.com.moandjiezana.toml.Toml toml) {
      return toml != null ? from(toml.toMap()) : null;
   }

   static Toml from(Map<String, Object> map) {
      return new TomlObject(fixMap(map));
   }

   @SuppressWarnings("unchecked")
   private static <K> Map<K, Object> fixMap(Map<K, Object> map) {
      return map.entrySet().stream()
              .map(entry -> {
                 if (entry.getValue() instanceof me.shedaniel.cloth.clothconfig.shadowed.com.moandjiezana.toml.Toml toml) {
                    entry.setValue(from(toml.toMap()));
                 } else if (entry.getValue() instanceof List<?> list) {
                    fixList(list);
                 } else if (entry.getValue() instanceof Map<?,?> newMap) {
                    entry.setValue(from((Map<String, Object>) newMap));
                 }

                 if (entry.getKey() instanceof String string) {
                    return new Pair<>((K) string.replaceAll("\"", ""), entry.getValue());
                 } else return new Pair<>(entry.getKey(), entry.getValue());
              }).collect(Collectors.toMap(
                      Pair::one,
                      Pair::two
              ));
   }

   @SuppressWarnings("unchecked")
   private static void fixList(List<?> list) {
      ListIterator<Object> iterator = (ListIterator<Object>) list.listIterator();

      while(iterator.hasNext()) {
         Object object = iterator.next();

         if (object instanceof me.shedaniel.cloth.clothconfig.shadowed.com.moandjiezana.toml.Toml toml) {
            iterator.set(from(toml.toMap()));
         } else if (object instanceof List<?> newList) {
            fixList(newList);
         } else if (object instanceof Map<?,?> map) {
            iterator.set(from((Map<String, Object>) map));
         }
      }
   }
}