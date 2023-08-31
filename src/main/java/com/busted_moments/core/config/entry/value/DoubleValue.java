package com.busted_moments.core.config.entry.value;

import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.config.Buildable;
import com.busted_moments.core.config.entry.ConfigEntry;
import com.essentuan.acf.core.command.arguments.builtin.primitaves.Double.DoubleMax;
import com.essentuan.acf.core.command.arguments.builtin.primitaves.Double.DoubleMin;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.minecraft.network.chat.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class DoubleValue extends ConfigEntry<Double> {
   public record Min() implements DoubleMin {
      @Override
      public double value() {
         return Double.MIN_VALUE;
      }

      @Override
      public Class<? extends Annotation> annotationType() {
         return DoubleMin.class;
      }
   }

   public record Max() implements DoubleMax {
      @Override
      public double value() {
         return Double.MAX_VALUE;
      }

      @Override
      public Class<? extends Annotation> annotationType() {
         return DoubleMax.class;
      }
   }


   public DoubleValue(Component title, Object ref, Field field, Buildable<?, ?> parent) {
      super(title, ref, field, parent,
              Annotated.Optional(new Max()),
              Annotated.Optional(new Min())
      );
   }

   @Override
   protected AbstractFieldBuilder<Double, ?, ?> create(ConfigEntryBuilder builder) {
      return create(builder::startDoubleField)
              .setMax(getAnnotation(DoubleMax.class, DoubleMax::value))
              .setMin(getAnnotation(DoubleMin.class, DoubleMin::value));
   }
}
