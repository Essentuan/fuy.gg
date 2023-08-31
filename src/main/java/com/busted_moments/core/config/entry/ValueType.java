package com.busted_moments.core.config.entry;

import com.busted_moments.core.config.Buildable;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.config.entry.value.*;
import com.google.common.primitives.Primitives;
import me.shedaniel.math.Color;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

public enum ValueType {
   BOOLEAN(BooleanValue::new, Boolean.class),
   COLOR(ColorValue::new, Color.class),
   DOUBLE(DoubleValue::new, Double.class),
   ENUM(EnumValue::new, Enum.class),
   FLOAT(FloatValue::new, Float.class),
   INTEGER(IntegerValue::new, Integer.class),
   LONG(LongValue::new, Long.class),
   STRING(StringValue::new, String.class);

   private final Class<?> type;
   private final ConfigConstructor constructor;

   ValueType(ConfigConstructor constructor, Class<?> type) {
      this.constructor = constructor;
      this.type = type;
   }

   public static ConfigEntry<?> create(Object ref, Field field, Buildable<?, ?> parent) {
      Component title = Component.literal(field.getAnnotation(Config.Value.class).value());

      Class<?> type = Primitives.wrap(field.getType());

      for (ValueType e : values()) {
         if (e.type.isAssignableFrom(type)) {
            return e.constructor.create(title, ref, field, parent);
         }
      }

      throw new IllegalArgumentException("Could not find value for type %s".formatted(type));
   }
}
