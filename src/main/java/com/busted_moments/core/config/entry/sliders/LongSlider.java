package com.busted_moments.core.config.entry.sliders;

import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.config.Buildable;
import com.busted_moments.core.config.entry.ConfigEntry;
import com.busted_moments.core.config.entry.value.LongValue;
import com.essentuan.acf.core.command.arguments.builtin.primitaves.Long.LongMax;
import com.essentuan.acf.core.command.arguments.builtin.primitaves.Long.LongMin;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

public class LongSlider extends ConfigEntry<Long> {
   public LongSlider(Component title, Object ref, Field field, Buildable<?, ?> parent) {
      super(title, ref, field, parent,
              Annotated.Optional(new LongValue.Max()),
              Annotated.Optional(new LongValue.Min())
      );
   }

   @Override
   protected AbstractFieldBuilder<Long, ?, ?> create(ConfigEntryBuilder builder) {
      return builder.startLongSlider(getTitle(), get(),
              getAnnotation(LongMin.class, LongMin::value),
              getAnnotation(LongMax.class, LongMax::value)
      );
   }
}
