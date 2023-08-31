package com.busted_moments.core.config.entry;

import com.busted_moments.core.config.Buildable;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.config.entry.sliders.IntegerSlider;
import com.busted_moments.core.config.entry.sliders.LongSlider;
import com.google.common.primitives.Primitives;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

enum SliderType {
   INTEGER(IntegerSlider::new, Integer.class),
   LONG(LongSlider::new, Long.class);

   private final Class<?> type;
   private final ConfigConstructor constructor;

   SliderType(ConfigConstructor constructor, Class<?> type) {
      this.constructor = constructor;
      this.type = type;
   }

   public static ConfigEntry<?> create(Object ref, Field field, Buildable<?, ?> parent) {
      Component title = Component.literal(field.getAnnotation(Config.Value.class).value());

      Class<?> type = Primitives.wrap(field.getType());

      for (SliderType e : values()) {
         if (e.type.isAssignableFrom(type)) {
            return e.constructor.create(title, ref, field, parent);
         }
      }

      throw new IllegalArgumentException("Could not find value for type %s".formatted(type));
   }
}
