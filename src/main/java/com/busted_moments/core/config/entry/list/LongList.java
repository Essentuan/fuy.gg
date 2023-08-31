package com.busted_moments.core.config.entry.list;

import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.config.Buildable;
import com.busted_moments.core.config.entry.ConfigEntry;
import com.busted_moments.core.config.entry.value.LongValue;
import com.busted_moments.core.toml.Toml;
import com.essentuan.acf.core.command.arguments.builtin.primitaves.Long.LongMax;
import com.essentuan.acf.core.command.arguments.builtin.primitaves.Long.LongMin;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public class LongList extends ConfigEntry<List<Long>> {
   public LongList(Component title, Object ref, Field field, Buildable<?, ?> parent) {
      super(title, ref, field, parent,
              Annotated.Optional(new LongValue.Max()),
              Annotated.Optional(new LongValue.Min())
      );
   }

   @Override
   protected AbstractFieldBuilder<List<Long>, ?, ?> create(ConfigEntryBuilder builder) {
      return create(builder::startLongList)
              .setMax(getAnnotation(LongMax.class, LongMax::value))
              .setMin(getAnnotation(LongMin.class, LongMin::value));
   }

   @Override
   public void save(Toml writer) {
      writer.put(getKey(), get());
   }

   @Override
   protected @Nullable List<Long> from(Toml object) {
      return object.getList(getKey(), Number::longValue);
   }
}
