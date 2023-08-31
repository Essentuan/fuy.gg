package com.busted_moments.core.config.entry.sliders;

import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.config.entry.value.IntegerValue;
import com.busted_moments.core.config.Buildable;
import com.busted_moments.core.config.entry.ConfigEntry;
import com.essentuan.acf.core.command.arguments.builtin.primitaves.Integer.IntMax;
import com.essentuan.acf.core.command.arguments.builtin.primitaves.Integer.IntMin;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

public class IntegerSlider extends ConfigEntry<Integer> {
   public IntegerSlider(Component title, Object ref, Field field, Buildable<?, ?> parent) {
      super(title, ref, field, parent,
              Annotated.Optional(new IntegerValue.Max()),
              Annotated.Optional(new IntegerValue.Min())
      );
   }

   @Override
   protected AbstractFieldBuilder<Integer, ?, ?> create(ConfigEntryBuilder builder) {
      return builder.startIntSlider(getTitle(), get(),
              getAnnotation(IntMin.class, IntMin::value),
              getAnnotation(IntMax.class, IntMax::value)
      );
   }
}
