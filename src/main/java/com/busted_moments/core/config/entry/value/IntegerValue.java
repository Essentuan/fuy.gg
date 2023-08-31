package com.busted_moments.core.config.entry.value;

import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.config.Buildable;
import com.busted_moments.core.config.entry.ConfigEntry;
import com.essentuan.acf.core.command.arguments.builtin.primitaves.Integer.IntMax;
import com.essentuan.acf.core.command.arguments.builtin.primitaves.Integer.IntMin;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.minecraft.network.chat.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

@SuppressWarnings("ALL")
public class IntegerValue extends ConfigEntry<Integer> {
   public record Min() implements IntMin {
      @Override
      public int value() {
         return Integer.MIN_VALUE;
      }

      @Override
      public Class<? extends Annotation> annotationType() {
         return IntMin.class;
      }
   }

   public record Max() implements IntMax {
      @Override
      public int value() {
         return Integer.MAX_VALUE;
      }

      @Override
      public Class<? extends Annotation> annotationType() {
         return IntMax.class;
      }
   }

   public IntegerValue(Component title, Object ref, Field field, Buildable<?, ?> parent) {
      super(title, ref, field, parent,
              Annotated.Optional(new Max()),
              Annotated.Optional(new Min())
      );
   }

   @Override
   public AbstractFieldBuilder<Integer, ?, ?> create(ConfigEntryBuilder obj) {
      return create(obj::startIntField)
              .setMax(getAnnotation(IntMax.class, IntMax::value))
              .setMin(getAnnotation(IntMin.class, IntMin::value));
   }
}
