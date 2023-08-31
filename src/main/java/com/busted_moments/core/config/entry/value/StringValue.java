package com.busted_moments.core.config.entry.value;

import com.busted_moments.core.config.Buildable;
import com.busted_moments.core.config.entry.ConfigEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

public class StringValue extends ConfigEntry<String> {
   public StringValue(Component title, Object ref, Field field, Buildable<?, ?> parent) {
      super(title, ref, field, parent);
   }

   @Override
   protected AbstractFieldBuilder<String, ?, ?> create(ConfigEntryBuilder builder) {
      return create(builder::startStrField);
   }
}
