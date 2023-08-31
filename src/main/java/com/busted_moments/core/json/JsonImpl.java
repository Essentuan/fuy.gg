package com.busted_moments.core.json;

import com.busted_moments.core.tuples.Pair;
import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.wynntils.core.json.JsonManager.GSON;
import static java.lang.String.format;

public class JsonImpl implements Json {
   private final Map<String, Object> map;

   JsonImpl() {
      this(new LinkedHashMap<>());
   }

   JsonImpl(final String key, final Object value) {
      this();

      set(key, value);
   }

   @SuppressWarnings("unchecked")
   JsonImpl(final Map<String, ?> map) {
      this.map = (Map<String, Object>) map;
   }

   private Optional<Pair<String, Json>> find(String key, boolean create) {
      if (key.contains(".")) {
         String[] keys = key.split("\\.");

         Json next = this;

         for (int i = 0; i < keys.length - 1; i++) {
            String part = keys[i];

            if (!next.has(part)) {
               if (create) {
                  next.put(part, Json.empty());
               } else return Optional.empty();
            }

            Object object = next.get(part);

            if (object instanceof Json Json) {
               next = Json;
            } else
               throw new ClassCastException("Key %s is %s not Json".formatted(part, object.getClass().getSimpleName()));
         }

         return Optional.of(new Pair<>(keys[keys.length - 1], next));
      } else {
         return Optional.of(new Pair<>(key, this));
      }
   }

   private <T> T operation(Object key, boolean create, Function<Optional<Pair<String, Json>>, T> operation, Supplier<T> base) {
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
   public Json set(String key, Object value) {
      put(key, value);

      return this;
   }

   @Override
   public Json remove(String key) {
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
   public Json getJson(String key) {
      return get(key, Json.class);
   }

   @Override
   public Json getJson(String key, Json defaultValue) throws ClassCastException {
      return get(key, defaultValue);
   }

   @Override
   public String toString() {
      return GSON.toJson(map);
   }


   static Json convert(JsonObject obj) {
      if (obj == null) {
         return null;
      }

      return get(obj);
   }

   private static @Nullable Object get(JsonElement element) {
      if (element instanceof JsonObject obj) return get(obj);
      else if (element instanceof JsonArray array) return get(array);
      else if (element instanceof JsonPrimitive primitive) return get(primitive);
      else return null;
   }

   private static Json get(JsonObject object) {
      Json json = Json.empty();

      object.asMap().forEach((key, element) -> json.set(key, get(element)));

      return json;
   }

   private static List<?> get(JsonArray element) {
      List<Object> list = new ArrayList<>();

      element.forEach(e -> list.add(get(e)));

      return list;
   }

   private static Object get(JsonPrimitive primitive) {
      if (primitive.isBoolean()) return primitive.getAsBoolean();
      else if (primitive.isNumber()) return primitive.getAsNumber();
      else if (primitive.isString()) return primitive.getAsString();
      else return null;
   }
}

