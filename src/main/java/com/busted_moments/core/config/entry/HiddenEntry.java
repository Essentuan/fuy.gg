package com.busted_moments.core.config.entry;

import com.busted_moments.core.config.Buildable;
import com.busted_moments.core.config.Config;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

public class HiddenEntry extends ConfigEntry<Object> {
   public HiddenEntry(Config.Hidden config, Object ref, Field field, Buildable<?, ?> parent) {
      super(Component.literal(config.value()), ref, field, parent);
   }

   @Override
   protected AbstractFieldBuilder<Object, ?, ?> create(ConfigEntryBuilder builder) {
      throw new UnsupportedOperationException();
   }
}
