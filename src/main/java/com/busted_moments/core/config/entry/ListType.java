package com.busted_moments.core.config.entry;

import com.busted_moments.core.config.Buildable;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.config.entry.list.*;
import com.google.common.primitives.Primitives;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

enum ListType {
   DOUBLE(DoubleList::new, Double.class),
   FLOAT(FloatList::new, Float.class),
   INTEGER(IntegerList::new, Integer.class),
   LONG(LongList::new, Long.class),
   STRING(StringList::new, String.class);

   private final Class<?> type;
   private final ConfigConstructor constructor;

   ListType(ConfigConstructor constructor, Class<?> type) {
      this.constructor = constructor;
      this.type = type;
   }

   public static ConfigEntry<?> create(Object ref, Field field, Buildable<?, ?> parent) {
      Component title = Component.literal(field.getAnnotation(Config.Array.class).value());

      Class<?> type = Primitives.wrap(field.getType());

      for (ListType e : values()) {
         if (e.type.isAssignableFrom(type)) {
            return e.constructor.create(title, ref, field, parent);
         }
      }

      throw new IllegalArgumentException("Could not find list for type %s".formatted(type));
   }
}
