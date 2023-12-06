package com.busted_moments.core.json;

import com.busted_moments.core.collector.SimpleCollector;
import com.busted_moments.core.time.ChronoUnit;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.toml.Toml;
import com.busted_moments.core.util.UUIDUtil;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static com.wynntils.core.WynntilsMod.GSON;

public interface Json extends Map<String, Object>, Serializable {
    Json set(final String key, final Object value);

    Json copy(final String from, final String... to);

    Json cut(final String from, final String... to);

    default Json addAll(Map<String, Object> map) {
        putAll(map);

        return this;
    }

    Json delete(final String key);

    default Json removeAll(final String... keys) {
        for (String key : keys) {
            delete(key);
        }

        return this;
    }

    default Json removeAll(final Collection<String> c) {
        c.forEach(this::delete);

        return this;
    }

    default <T> Json removeAll(final Collection<T> c, Function<T, String> mapper) {
        c.forEach(obj -> delete(mapper.apply(obj)));

        return this;
    }


    <T> T get(final Object key, final Class<T> clazz) throws ClassCastException;

    <T> T get(final Object key, final T defaultValue) throws ClassCastException;

    default boolean has(final Object key) {
        return containsKey(key);
    }

    default boolean isNull(final Object key) {
        return !has(key) || get(key) == null;
    }

    Number getNumber(final Object key) throws ClassCastException;

    default <T extends Number> T getNumber(final Object key, Function<Number, T> getter) throws ClassCastException {
        Number number = getNumber(key);

        if (number == null) {
            return null;
        }

        return getter.apply(number);
    }

    Integer getInteger(final Object key) throws ClassCastException;

    int getInteger(final Object key, final int defaultValue) throws ClassCastException;

    Long getLong(final Object key) throws ClassCastException;
    long getLong(final Object key, final long defaultValue) throws ClassCastException;

    Float getFloat(final Object key) throws ClassCastException;

    float getFloat(final Object key, final float defaultValue) throws ClassCastException;

    Double getDouble(final Object key) throws ClassCastException;

    double getDouble(final Object key, final double defaultValue) throws ClassCastException;

    String getString(final Object key) throws ClassCastException;

    String getString(final Object key, final String defaultValue) throws ClassCastException;;

    Boolean getBoolean(final Object key) throws ClassCastException;

    boolean getBoolean(final Object key, final boolean defaultValue) throws ClassCastException;

    Date getDate(final Object key) throws ClassCastException;

    Date getDate(final Object key, final Date defaultValue) throws ClassCastException;

    default Date getDate(final Object key, final long defaultValue) throws ClassCastException {
        return getDate(key, new Date(defaultValue));
    }

    default Duration getDuration(final Object key) throws ClassCastException {
        Object obj = get(key);

        if (obj instanceof Duration duration)
            return duration;
        else if (obj instanceof Json json) {
            if (json.has("seconds") && json.has("nanos")) {
                return Duration.of(json.getDouble("seconds"), ChronoUnit.SECONDS)
                        .add(json.getDouble("nanos"), ChronoUnit.NANOSECONDS);
            } else
                return null;
        } else if (obj == null)
            return null;
        else
            throw new ClassCastException("(%s) cannot be cast to Duration".formatted(obj.getClass().getSimpleName()));
    }

    default Duration getDuration(final Object key, final Duration defaultValue) throws ClassCastException {
        return has(key) ? getDuration(key) : defaultValue;
    }

    default UUID getUUID(final Object key) throws IllegalArgumentException, ClassCastException {
        return getUUID(key, null);
    }

    default UUID getUUID(final Object key, final UUID defaultValue) throws IllegalArgumentException, ClassCastException {
        if (!isUUID(key)) {
            return defaultValue;
        }

        if (isString(key)) {
            return UUID.fromString(getString(key));
        }

        return get(key, UUID.class);
    }

    List<Object> getList(final Object key) throws ClassCastException;

    List<Object> getList(final Object key, final List<Object> defaultValue) throws ClassCastException;

    <T> List<T> getList(final Object key, final Class<T> clazz) throws ClassCastException;

    <T> List<T> getList(final Object key, final Class<T> clazz, final List<T> defaultValue) throws ClassCastException;

    Json getJson(final Object key) throws ClassCastException;

    Json getJson(final Object key, final Json defaultValue) throws ClassCastException;

    default @Nullable Class<?> getType(final Object key) {
        return containsKey(key) ? get(key).getClass() : null;
    }

    default boolean isType(final Object key, final Class<?> clazz) {
        Class<?> type = getType(key);

        return type != null && clazz.isAssignableFrom(type);
    }

    default boolean isPrimitive(final Object key) {
        Class<?> type = getType(key);

        return type != null && type.isPrimitive();
    }

    default boolean isInteger(final Object key) {
        return isType(key, Integer.class) || isType(key, int.class);
    }

    default boolean isLong(final Object key) {
        return isType(key, Long.class) || isType(key, long.class);
    }

    default boolean isDouble(final Object key) {
        return isType(key, Double.class) || isType(key, double.class);
    }

    default boolean isBoolean(final Object key) {
        return isType(key, Boolean.class) || isType(key, boolean.class);
    }

    default boolean isString(final Object key) {
        return isType(key, String.class);
    }

    default boolean isDate(final Object key) {
        return isType(key, Date.class);
    }

    default boolean isDuration(final Object key) {
        return isType(key, Duration.class);
    }

    default boolean isUUID(final Object key) {
        return isType(key, UUID.class) || (isString(key) && UUIDUtil.isUUID(getString(key)));
    }

    default boolean isList(final Object key) {
        return isType(key, List.class);
    }

    default boolean isJson(final Object key) {
        Class<?> clazz = getType(key);

        return clazz != null && Json.class.isAssignableFrom(clazz);
    }

    String toString();

    @SuppressWarnings("unchecked")
    default <T extends BaseModel> T wrap(BaseModel.Factory<T> constructor) {
        return (T) constructor.get().load(this);
    }

    default <T extends BaseModel> T wrap(Class<T> clazz) {
        return wrap(BaseModel.constructor(clazz));
    }

    static Json parse(@Nullable String string) {
        if (string == null || string.isBlank())
            return Json.empty();
        else if (string.trim().charAt(0) == '[')
            string = "{\"root\": " + string + "}";

        return JsonImpl.convert(GSON.fromJson(string, JsonObject.class));
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

    static Optional<Json> tryParse(String string) {
        try {
            return Optional.of(parse(string));
        } catch(Exception e) {
            return Optional.empty();
        }
    }

    static <T extends BaseModel> T wrap(Json json, BaseModel.Factory<T> constructor) {
        return json.wrap(constructor);
    }

    static Json empty() {
        return new JsonImpl();
    }

    static Json of(String key, Object value) {
        return empty().set(key, value);
    }

    static <T extends BaseModel> Function<Json, T> map(BaseModel.Factory<T> factory) {
        return json -> json.wrap(factory);
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
