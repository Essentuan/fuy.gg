package com.busted_moments.core.config.entry.value;

import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.config.Buildable;
import com.busted_moments.core.config.entry.ConfigEntry;
import com.essentuan.acf.core.command.arguments.builtin.primitaves.Long.LongMax;
import com.essentuan.acf.core.command.arguments.builtin.primitaves.Long.LongMin;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.minecraft.network.chat.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class LongValue extends ConfigEntry<Long> {
   public record Min() implements LongMin {
      @Override
      public long value() {
         return Long.MIN_VALUE;
      }

      @Override
      public Class<? extends Annotation> annotationType() {
         return LongMin.class;
      }
   }

   public record Max() implements LongMax {
      @Override
      public long value() {
         return Long.MAX_VALUE;
      }

      @Override
      public Class<? extends Annotation> annotationType() {
         return LongMax.class;
      }
   }


   public LongValue(Component title, Object ref, Field field, Buildable<?, ?> parent) {
      super(title, ref, field, parent,
              Annotated.Optional(new Max()),
              Annotated.Optional(new Min())
      );
   }

   @Override
   protected AbstractFieldBuilder<Long, ?, ?> create(ConfigEntryBuilder builder) {
      return create(builder::startLongField)
              .setMax(getAnnotation(LongMax.class, LongMax::value))
              .setMin(getAnnotation(LongMin.class, LongMin::value));
   }
}
