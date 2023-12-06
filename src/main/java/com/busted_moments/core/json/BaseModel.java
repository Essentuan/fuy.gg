package com.busted_moments.core.json;

import com.busted_moments.core.UnexpectedException;
import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.time.ChronoUnit;
import com.busted_moments.core.util.Reflection;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static com.busted_moments.client.FuyMain.LOGGER;

public abstract class BaseModel extends Annotated {
   private final Map<String, Entry> entries = new LinkedHashMap<>();

   protected BaseModel() {
      this(new Validator[0]);
   }

   protected BaseModel(Annotated.Validator<?>... annotations) {
      super(annotations);

      register();
   }

   public BaseModel load(Json json) {
      onPreLoad(json);

      for (Entry entry : entries.values()) {
         try {
            entry.load(json);

            if (entry.get() == null && !entry.getField().isAnnotationPresent(Null.class)) {
               throw new UnexpectedException("NonNull value %s in %s was null!", entry.getKey(), Reflection.toSimpleString(getClass()));
            }
         } catch (Exception e) {
            throw UnexpectedException.propagate(e);
         }
      }

      onLoad();

      return this;
   }

   protected void onSave(Json json) {

   }

   protected void onLoad() {

   }

   protected void onPreLoad(Json json) {

   }

   public Json toJson() {
      Json json = Json.empty();

      for (Entry entry : entries.values()) {
         try {
            entry.save(json);
         } catch (Exception e) {
            LOGGER.error(String.format("Failed to save value for '%s'", entry.getKey()), e);
         }
      }

      onSave(json);

      return json;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder(Reflection.toSimpleString(getClass()))
              .append("{");

      for (var iter = entries.values().iterator(); iter.hasNext(); ) {
         try {
            var wrapper = iter.next();
            Object value = wrapper.get();

            builder.append("%s=%s%s".formatted(wrapper.getKey(), value == null ? "null" : value.toString(), iter.hasNext() ? ", " : ""));
         } catch (IllegalAccessException e) {
            throw UnexpectedException.propagate(e);
         }
      }

      return builder.append("}").toString();
   }

   @Override
   public int hashCode() {
      return Arrays.deepHashCode(
              entries.values().stream()
                      .filter(entry -> !entry.field.isAnnotationPresent(NoHash.class))
                      .map(e -> {
                         try {
                            return e.get();
                         } catch (IllegalAccessException ex) {
                            throw UnexpectedException.propagate(ex);
                         }
                      }).toArray()
      );
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;

      if (obj == null || obj.getClass() != getClass())
         return false;

      BaseModel dc = (BaseModel) obj;

      try {
         for (Map.Entry<String, Entry> entry : entries.entrySet()) {
            String key = entry.getKey();
            Entry other = dc.entries.get(key);

            if (!Objects.deepEquals(other.get(), entry.getValue().get()))
               return false;
         }
      } catch (Exception e) {
         throw UnexpectedException.propagate(e);
      }

      return true;
   }

   public Entry getEntry(Field field) {
      for (Entry entry : entries.values()) {
         if (entry.getField().equals(field)) {
            return entry;
         }
      }

      return null;
   }

   public Entry getEntry(String key) {
      return entries.get(key);
   }

   public Collection<Entry> getFields() {
      return entries.values();
   }

   private void register() {
      getFields(getClass()).forEach(field -> {
         Entry entry = new Entry(field);

         entries.put(entry.getKey(), entry);
      });
   }

   public static <T extends BaseModel> Factory<T> constructor(Class<T> clazz) {
      return () -> BaseModel.construct(clazz);
   }

   public static <T extends BaseModel> T construct(Class<T> clazz) {
      try {
         var c = clazz.getConstructor();
         c.setAccessible(true);

         return c.newInstance();
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
         throw UnexpectedException.propagate(e);
      } catch (NoSuchMethodException e) {
         throw new UnexpectedException("%s is missing default constructor!", Reflection.toString(clazz));
      }
   }

   public static <T extends BaseModel> Class<T> classOf(Factory<T> constructor) {
      return (Class<T>) constructor.get().getClass();
   }

   private static final ListMultimap<Class<? extends BaseModel>, Field> LOADED_FIELDS = MultimapBuilder.hashKeys().linkedListValues().build();

   public static List<Field> getFields(Class<? extends BaseModel> clazz) {
      if (!LOADED_FIELDS.containsKey(clazz))
         Reflection.visit(clazz)
                 .flatMap(Reflection::getFields)
                 .filter((field) -> !Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(Key.class))
                 .forEach(field -> LOADED_FIELDS.put(clazz, field));

      return LOADED_FIELDS.get(clazz);
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.FIELD)
   public @interface Key {
      String value() default "";

      boolean isRaw() default false;
   }

   @Target(ElementType.FIELD)
   @Retention(RetentionPolicy.RUNTIME)
   public @interface Null {
   }

   @Target(ElementType.FIELD)
   @Retention(RetentionPolicy.RUNTIME)
   public @interface NoHash {
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.FIELD, ElementType.TYPE_PARAMETER})
   public @interface Final {
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.FIELD, ElementType.TYPE_PARAMETER})
   public @interface Unit {
      ChronoUnit value();
   }

   public interface Factory<T extends BaseModel> extends Supplier<T> {
   }

   @SuppressWarnings("rawtypes")
   public class Entry {
      private final String key;
      private final Field field;

      private final AbstractCodec codec;

      public Entry(Field field) {
         this.field = field;

         codec = AbstractCodec.get(field.getType());

         Key entry = field.getAnnotation(Key.class);

         key = getKey(field, entry);
      }

      public String getKey() {
         return key;
      }

      public Field getField() {
         return field;
      }

      public <T> T get() throws IllegalAccessException {
         try {
            if (!field.canAccess(BaseModel.this))
               field.setAccessible(true);

            Object value = field.get(BaseModel.this);

            return (T) value;
         } catch (IllegalAccessException exception) {
            LOGGER.error(String.format("Error while accessing field '%s' in class '%s'", getKey(), Reflection.toSimpleString(BaseModel.this.getClass())));

            throw exception;
         }
      }

      public boolean isNull() throws IllegalAccessException {
         return get() == null;
      }

      public void set(Object value) throws IllegalAccessException {
         try {
            if (!field.canAccess(BaseModel.this))
               field.setAccessible(true);

            field.set(BaseModel.this, value);
         } catch (IllegalAccessException exception) {
            LOGGER.error(String.format("Error while setting field '%s' in class '%s'", getKey(), Reflection.toSimpleString(BaseModel.this.getClass())));

            throw exception;
         }
      }

      public void load(Json json) throws Exception {
         if (!json.isNull(getKey())) {
            set(codec.read(json.get(getKey()), this));
         }
      }

      public void save(Json json) throws Exception {
         if (!isNull()) {
            json.set(getKey(), codec.write(this));
         }
      }

      public Object serialize() {
         try {
            return codec.write(this);
         } catch (Exception e) {
            throw UnexpectedException.propagate(e);
         }
      }

      private static String getKey(Field field, Key key) {
         return key.value().isEmpty() ? field.getName() : key.value();

      }

      public static String getKey(Field field) {
         return getKey(field, field.getAnnotation(Key.class));
      }
   }
}
