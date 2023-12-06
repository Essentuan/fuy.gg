package com.busted_moments.core.json;

import com.busted_moments.core.tuples.Pair;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.wynntils.core.WynntilsMod.GSON;

public class JsonImpl implements Json {
    private final Map<String, Object> map;

    public JsonImpl() {
        this(new LinkedHashMap<>());
    }

    public JsonImpl(final String key, final Object value) {
        this();

        set(key, value);
    }

    @SuppressWarnings("unchecked")
    public JsonImpl(final Map<String, ?> map) {
        this.map = (Map<String, Object>) map;
    }

    private Optional<Pair<String, Json>> find(String key, boolean create) {
        if (key.contains(".")) {
            String[] keys = key.split("\\.");

            Json next = this;

            for (int i = 0; i < keys.length - 1; i++) {
                String part = keys[i];

                Object object = next.computeIfAbsent(part, k -> {
                    if (create)
                        return Json.empty();
                    else
                        return null;
                });

                if (object == null)
                    return Optional.empty();
                else if (object instanceof Json json) {
                    next = json;
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
    public boolean containsKey(final Object key) {
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
    public Object get(final Object key) {
        return operation(key, false,
                result -> result.map(pair -> pair.two().get(pair.one())).orElse(null),
                () -> map.get(key)
        );
    }
    @Override
    public Object put(final String key, final Object value) {
        return operation(key, true,
                result -> result.map(pair -> pair.two().put(pair.one(), value)).orElse(null),
                () -> map.put(key, value)
        );
    }

    @Override
    public void putAll(Map<? extends String, ?> map) {
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
    public Object getOrDefault(Object key, Object defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super Object> action) {
        map.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super Object, ?> function) {
        map.replaceAll(function);
    }

    @Nullable
    @Override
    public Object putIfAbsent(String key, Object value) {
        return map.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return map.remove(key, value);
    }

    @Override
    public boolean replace(String key, Object oldValue, Object newValue) {
        return map.replace(key, oldValue, newValue);
    }

    @Nullable
    @Override
    public Object replace(String key, Object value) {
        return map.replace(key, value);
    }

    @Override
    public Object computeIfAbsent(String key, @NotNull Function<? super String, ?> mappingFunction) {
        return map.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public Object computeIfPresent(String key, @NotNull BiFunction<? super String, ? super Object, ?> remappingFunction) {
        return map.computeIfPresent(key, remappingFunction);
    }

    @Override
    public Object compute(String key, @NotNull BiFunction<? super String, ? super Object, ?> remappingFunction) {
        return map.compute(key, remappingFunction);
    }

    @Override
    public Object merge(String key, @NotNull Object value, @NotNull BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return map.merge(key, value, remappingFunction);
    }

    @Override
    public Object remove(final Object key) {
        return operation(key, false,
                result -> result.map(pair -> pair.two().remove(pair.one())).orElse(null),
                () -> map.remove(key)
        );
    }

    @Override
    public <T> T get(final Object key, final Class<T> clazz) throws ClassCastException {
        return clazz.cast(get(key));
    }

    @Override
    public <T> T get(final Object key, final T defaultValue) throws ClassCastException {
        Object value = get(key);
        return value == null ? defaultValue : (T) value;
    }

    @Override
    public Number getNumber(Object key) throws ClassCastException {
        return get(key, Number.class);
    }

    @Override
    public Json set(String key, Object value) {
        put(key, value);

        return this;
    }

    @Override
    public Json copy(String key, String... to) {
        Object value = get(key);

        for (String k : to)
            set(k, value);

        return this;
    }

    @Override
    public Json cut(String key, String... to) {
        Object value = remove(key);

        for (String k : to)
            set(k, value);

        return this;
    }

    @Override
    public Json delete(String key) {
        remove(key);

        return this;
    }

    @Override
    public List<Object> getList(Object key) throws ClassCastException {
        return getList(key, Object.class);
    }

    @Override
    public List<Object> getList(Object key, List<Object> defaultValue) throws ClassCastException {
        List<Object> value = getList(key);
        return value == null ? defaultValue : value;
    }

    @Override
    public <T> List<T> getList(Object key, Class<T> clazz) throws ClassCastException {
        return null;
    }

    @Override
    public <T> List<T> getList(Object key, Class<T> clazz, List<T> defaultValue) throws ClassCastException {
        return null;
    }

    @Override
    public Integer getInteger(final Object key) throws ClassCastException {
        return getNumber(key, Number::intValue);
    }

    @Override
    public int getInteger(final Object key, final int defaultValue) throws ClassCastException {
        return Optional.of(getInteger(key)).orElse(defaultValue);
    }

    @Override
    public Long getLong(final Object key) throws ClassCastException {
        return getNumber(key, Number::longValue);
    }

    @Override
    public long getLong(final Object key, final long defaultValue) throws ClassCastException {
        return Optional.of(getLong(key)).orElse(defaultValue);
    }

    @Override
    public Float getFloat(final Object key) throws ClassCastException {
        return getNumber(key, Number::floatValue);
    }

    @Override
    public float getFloat(final Object key, final float defaultValue) throws ClassCastException {
        return Optional.of(getFloat(key)).orElse(defaultValue);
    }

    @Override
    public Double getDouble(final Object key) throws ClassCastException {
        return getNumber(key, Number::doubleValue);
    }

    @Override
    public double getDouble(final Object key, final double defaultValue) throws ClassCastException {
        return Optional.of(getDouble(key)).orElse(defaultValue);
    }

    @Override
    public String getString(Object key) throws ClassCastException {
        return null;
    }

    @Override
    public String getString(Object key, String defaultValue) throws ClassCastException {
         return containsKey(key) ? getString(key) : defaultValue;
    }

    @Override
    public Boolean getBoolean(Object key) throws ClassCastException {
        return null;
    }

    @Override
    public boolean getBoolean(Object key, boolean defaultValue) throws ClassCastException {
        return false;
    }

    @Override
    public Date getDate(Object key) throws ClassCastException {
        if (isType(key, Date.class)) {
            return get(key, Date.class);
        } else {
            return new Date(getLong(key));
        }
    }

    @Override
    public Date getDate(Object key, Date defaultValue) throws ClassCastException {
        return containsKey(key) ? getDate(key) : defaultValue;
    }

    @Override
    public Json getJson(Object key) throws ClassCastException {
        return (Json) get(key);
    }

    @Override
    public Json getJson(Object key, Json defaultValue) throws ClassCastException {
        return containsKey(key) ? getJson(key) : defaultValue;
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
        if (element instanceof JsonObject obj)
            return get(obj);
        else if (element instanceof JsonArray array)
            return get(array);
        else if (element instanceof JsonPrimitive primitive)
            return get(primitive);
        else
            return null;
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
        if (primitive.isBoolean())
            return primitive.getAsBoolean();
        else if (primitive.isNumber())
            return primitive.getAsNumber();
        else if (primitive.isString())
            return primitive.getAsString();
        else
            return null;
    }
}
