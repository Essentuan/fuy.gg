package com.busted_moments.core.json.template;

import com.busted_moments.core.json.Codec;
import com.busted_moments.core.json.Json;

import java.lang.reflect.Field;

import static com.busted_moments.client.FuyMain.LOGGER;

@SuppressWarnings("rawtypes")
public class EntryWrapper {
   private final String key;
   private final Field field;
   private final boolean raw;

   private final JsonTemplate instance;

   private final Codec codec;

   public EntryWrapper(Field field, JsonTemplate instance) {
      this.field = field;
      this.instance = instance;

      codec = Codec.get(field.getType());

      JsonTemplate.Entry entry = field.getAnnotation(JsonTemplate.Entry.class);

      if (entry.value().isEmpty()) key = field.getName();
      else key = entry.value();

      raw = entry.isRaw();
   }

   public String getKey() {
      return key;
   }

   public Field getField() {
      return field;
   }

   public JsonTemplate getInstance() {
      return instance;
   }

   @SuppressWarnings("unchecked")
   public <T> T get() throws IllegalAccessException {
      try {
         if (!field.canAccess(instance)) {
            field.setAccessible(true);
         }

         Object value = field.get(instance);

         return (T) value;
      } catch(IllegalAccessException exception) {
         LOGGER.error(String.format("Error while accessing field '%s' in class '%s'", getKey(), instance.getClass().getSimpleName()));

         throw exception;
      }
   }

   public boolean isNull() throws IllegalAccessException {
      return get() == null;
   }

   public void set(Object value) throws IllegalAccessException {
      try {
         if (!field.canAccess(instance)) {
            field.setAccessible(true);
         }

         field.set(instance, value);
      } catch(IllegalAccessException exception) {
         LOGGER.error(String.format("Error while setting field '%s' in class '%s'", getKey(), instance.getClass().getSimpleName()));

         throw exception;
      }
   }

   @SuppressWarnings("unchecked")
   public void load(Json json) throws Exception {
      if (json.has(getKey())) {
         set(codec.read(json.get(getKey()), this));
      }
   }

   public void save(Json json) throws Exception{
      if (!isNull()) {
         json.set(getKey(), codec.write(this));
      }
   }
}
