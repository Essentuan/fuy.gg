package com.busted_moments.core.json.template;

import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.json.Json;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static com.busted_moments.client.FuyMain.LOGGER;

public class JsonTemplate extends Annotated {
   private final Map<String, EntryWrapper> entries = new LinkedHashMap<>();

   public JsonTemplate() {
      super(new Validator[0]);

      register();
   }

   public JsonTemplate(Annotated.Validator<?>... annotations) {
      super(annotations);

      register();
   }

   public JsonTemplate load(Json json) {
      onPreLoad(json);

      for (EntryWrapper entry : entries.values()) {
         try {
            entry.load(json);

            if (entry.get() == null && !entry.getField().isAnnotationPresent(Nullable.class)) {
               throw new RuntimeException("NonNull value %s (%s) was null!".formatted(entry.getKey(), getClass().getSimpleName()));
            }
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }

      return this;
   }

   protected void onSave(Json json) {

   }

   protected void onPreLoad(Json json) {

   }

   public Json toJson() {
      Json json = Json.empty();

      for (EntryWrapper entry : entries.values()) {
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
      StringBuilder builder = new StringBuilder("%s{".formatted(getClass().getSimpleName()));

      Iterator<EntryWrapper> iter = entries.values().iterator();

      while(iter.hasNext()) {
         EntryWrapper wrapper = iter.next();

         try {
            Object value = wrapper.get();

            builder.append("%s=%s%s".formatted(wrapper.getKey(), value == null ? "null" : value.toString(), iter.hasNext() ? ", " : ""));
         } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
         }
      }

      return builder.append("}").toString();
   }

   @Override
   public int hashCode() {
      return toString().hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) return false;
      else if (obj.getClass() != getClass()) return false;
      else return obj.hashCode() == hashCode();
   }

   public EntryWrapper getEntry(Field field) {
      for (EntryWrapper entry : entries.values()) {
         if (entry.getField().equals(field)) {
            return entry;
         }
      }

      return null;
   }

   public EntryWrapper getEntry(String key) {
      return entries.get(key);
   }

   public Collection<EntryWrapper> getFields() {
      return entries.values();
   }

   private void register() {
      getFields(getClass()).forEach(field -> {
         EntryWrapper entry = new EntryWrapper(field, this);

         entries.put(entry.getKey(), entry);
      });
   }

   private static final ListMultimap<Class<? extends JsonTemplate>, Field> LOADED_FIELDS = MultimapBuilder.hashKeys().linkedListValues().build();

   private static List<Field> getFields(Class<? extends JsonTemplate> clazz) {
      if (!LOADED_FIELDS.containsKey(clazz)) {
         traverse(clazz);
      }

      return LOADED_FIELDS.get(clazz);
   }

   private static void traverse(Class<? extends JsonTemplate> clazz) {
      traverse(clazz, clazz);
   }

   private static void traverse(Class<?> clazz, Class<? extends JsonTemplate> fields) {
      Arrays.stream(clazz.getDeclaredFields())
              .filter((field) -> !Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(Entry.class))
              .forEach(field -> LOADED_FIELDS.put(fields, field));

      if (clazz.getSuperclass() != null) {
         traverse(clazz.getSuperclass(), fields);
      }
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.FIELD)
   public @interface Entry {
      String value() default "";
      boolean isRaw() default false;
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.FIELD)
   public @interface Nullable {}
}
