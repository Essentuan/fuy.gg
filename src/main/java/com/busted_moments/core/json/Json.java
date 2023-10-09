package com.busted_moments.core.json;


import com.busted_moments.core.collector.SimpleCollector;
import com.busted_moments.core.json.template.JsonTemplate;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.TimeUnit;
import com.busted_moments.core.toml.Toml;
import com.busted_moments.core.util.UUIDUtil;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.wynntils.core.json.JsonManager.GSON;

public interface Json extends Map<String, Object>, Serializable {
   Json set(final String key, final Object value);

   default Json addAll(Map<String, Object> map) {
      putAll(map);

      return this;
   }

   Json remove(final String key);

   default Json removeAll(final String... keys) {
      for (String key : keys) {
         remove(key);
      }

      return this;
   }

   default Json removeAll(final Collection<String> c) {
      c.forEach(this::remove);

      return this;
   }

   default <T> Json removeAll(final Collection<T> c, Function<T, String> mapper) {
      c.forEach(obj -> remove(mapper.apply(obj)));

      return this;
   }


   <T> T get(final String key, final Class<T> clazz) throws ClassCastException;

   <T> T get(final String key, final T defaultValue) throws ClassCastException;

   <T> T get(String key, Class<T> clazz, T defaultValue);

   default boolean has(final String key) {
      return containsKey(key);
   }

   Number getNumber(final String key) throws ClassCastException;

   Number getNumber(final String key, final Number defaultValue) throws ClassCastException;

   default <T extends Number> T getNumber(final String key, Function<Number, T> getter) throws ClassCastException {
      Number number = getNumber(key);

      if (number == null) {
         return null;
      }

      return getter.apply(number);
   }

   default <T extends Number> T getNumber(final String key, Number defaultValue, Function<Number, T> getter) {
      return getter.apply(getNumber(key, defaultValue));
   }

   Integer getInteger(final String key) throws ClassCastException;

   int getInteger(final String key, final int defaultValue) throws ClassCastException;

   Long getLong(final String key) throws ClassCastException;

   long getLong(final String key, final long defaultValue) throws ClassCastException;

   Float getFloat(final String key) throws ClassCastException;

   float getFloat(final String key, final float defaultValue) throws ClassCastException;

   Double getDouble(final String key) throws ClassCastException;

   double getDouble(final String key, final double defaultValue) throws ClassCastException;

   String getString(final String key) throws ClassCastException;

   String getString(final String key, final String defaultValue) throws ClassCastException;

   ;

   Boolean getBoolean(final String key) throws ClassCastException;

   boolean getBoolean(final String key, final boolean defaultValue) throws ClassCastException;

   Date getDate(final String key) throws ClassCastException;

   Date getDate(final String key, final Date defaultValue) throws ClassCastException;

   default Date getDate(final String key, final long defaultValue) throws ClassCastException {
      return getDate(key, new Date(defaultValue));
   }

   default Duration getDuration(final String key) throws ClassCastException {
      Object obj = get(key);

      if (obj == null) return null;
      else if (obj instanceof Duration duration) return duration;
      else if (obj instanceof Json json) {
         if (json.has("seconds") && json.has("nanos")) {
            return Duration.of(json.getDouble("seconds"), TimeUnit.SECONDS)
                    .add(json.getDouble("nanos"), TimeUnit.NANOSECONDS);
         } else return null;
      } else if (obj instanceof Number number) return Duration.of(number.doubleValue(), TimeUnit.MILLISECONDS);

      throw new ClassCastException("(%s) cannot be cast to Duration".formatted(obj.getClass().getSimpleName()));
   }

   default Duration getDuration(final String key, final Duration defaultValue) throws ClassCastException {
      return has(key) ? getDuration(key) : defaultValue;
   }

   default UUID getUUID(final String key) throws IllegalArgumentException, ClassCastException {
      return getUUID(key, null);
   }

   default UUID getUUID(final String key, final UUID defaultValue) throws IllegalArgumentException, ClassCastException {
      if (!isUUID(key)) {
         return defaultValue;
      }

      if (isString(key)) {
         return UUID.fromString(getString(key));
      }

      return get(key, UUID.class);
   }

   List<Object> getList(final String key) throws ClassCastException;

   List<Object> getList(final String key, final List<Object> defaultValue) throws ClassCastException;

   <T> List<T> getList(final String key, final Class<T> clazz) throws ClassCastException;

   <T> List<T> getList(final String key, final Class<T> clazz, final List<T> defaultValue) throws ClassCastException;

   Json getJson(final String key) throws ClassCastException;

   Json getJson(final String key, final Json defaultValue) throws ClassCastException;

   default @Nullable Class<?> getType(final String key) {
      return containsKey(key) ? get(key).getClass() : null;
   }

   default boolean isType(final String key, final Class<?> clazz) {
      Class<?> type = getType(key);

      return type != null && clazz.isAssignableFrom(type);
   }

   default boolean isPrimitive(final String key) {
      Class<?> type = getType(key);

      return type != null && type.isPrimitive();
   }

   default boolean isInteger(final String key) {
      return isType(key, Integer.class) || isType(key, int.class);
   }

   default boolean isLong(final String key) {
      return isType(key, Long.class) || isType(key, long.class);
   }

   default boolean isDouble(final String key) {
      return isType(key, Double.class) || isType(key, double.class);
   }

   default boolean isBoolean(final String key) {
      return isType(key, Boolean.class) || isType(key, boolean.class);
   }

   default boolean isString(final String key) {
      return isType(key, String.class);
   }

   default boolean isDate(final String key) {
      return isType(key, Date.class);
   }

   default boolean isDuration(final String key) {
      return isType(key, Duration.class);
   }

   default boolean isUUID(final String key) {
      return isType(key, UUID.class) || (isString(key) & UUIDUtil .isUUID(getString(key)));
   }

   default boolean isList(final String key) {
      return isType(key, List.class);
   }

   default boolean isJsonObject(final String key) {
      Class<?> clazz = getType(key);

      return clazz != null && Json.class.isAssignableFrom(clazz);
   }

   String toString();

   @SuppressWarnings("unchecked")
   default <T extends JsonTemplate> T wrap(Supplier<T> constructor) {
      return (T) constructor.get().load(this);
   }

   default <T extends JsonTemplate> T wrap(Class<T> clazz) {
      return wrap(() -> {
         try {
            return clazz.getConstructor().newInstance();
         } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
         }
      });
   }


   static Json empty() {
      return new JsonImpl();
   }

   static Json of(String key, Object value) {
      return empty().set(key, value);
   }

   static Json of(JsonObject object) {
      return JsonImpl.convert(object);
   }

   static Json of(Map<String, ?> map) {
      if (map instanceof Json json) return json;

      return new JsonImpl(map);
   }

   static Json of(Toml toml) {
      Json json = new JsonImpl(toml);

      for (var iter = json.entrySet().iterator(); iter.hasNext(); ) {
         var entry = iter.next();
         var value = entry.getValue();

         if (value instanceof Toml t) {
            entry.setValue(of(t));
         }
      }

      return json;
   }

   static Json parse(String string) {
      return JsonImpl.convert(GSON.fromJson(string, JsonObject.class));
   }

   static Optional<Json> tryParse(String string) {
      try {
         return Optional.of(parse(string));
      } catch(Exception e) {
         return Optional.empty();
      }
   }

   class Collector<T> extends SimpleCollector<T, Json, Json> {
      private final Function<T, String> keyMapper;
      private final Function<T, ?> valueMapper;

      @SuppressWarnings("unchecked")
      public Collector(Function<T, String> keyMapper) {
         this(keyMapper, value -> value);
      }

      @SuppressWarnings("unchecked")
      public Collector(Function<T, String> keyMapper, Function<T, ?> valueMapper) {
         this.keyMapper = keyMapper;
         this.valueMapper = valueMapper;
      }

      @Override
      protected Json supply() {
         return Json.empty();
      }

      @Override
      protected void accumulate(Json container, T value) {
         container.set(keyMapper.apply(value), valueMapper.apply(value));
      }

      @Override
      protected Json combine(Json left, Json right) {
         left.putAll(right);

         return left;
      }

      @Override
      protected Json finish(Json container) {
         return container;
      }
   }
}
