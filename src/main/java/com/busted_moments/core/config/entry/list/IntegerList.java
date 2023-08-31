package com.busted_moments.core.config.entry.list;

import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.config.Buildable;
import com.busted_moments.core.config.entry.ConfigEntry;
import com.busted_moments.core.config.entry.value.IntegerValue;
import com.busted_moments.core.toml.Toml;
import com.essentuan.acf.core.command.arguments.builtin.primitaves.Integer.IntMax;
import com.essentuan.acf.core.command.arguments.builtin.primitaves.Integer.IntMin;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public class IntegerList extends ConfigEntry<List<Integer>> {
   public IntegerList(Component title, Object ref, Field field, Buildable<?, ?> parent) {
      super(title, ref, field, parent,
              Annotated.Optional(new IntegerValue.Max()),
              Annotated.Optional(new IntegerValue.Min())
      );
   }

   @Override
   protected AbstractFieldBuilder<List<Integer>, ?, ?> create(ConfigEntryBuilder builder) {
      return create(builder::startIntList)
              .setMax(getAnnotation(IntMax.class, IntMax::value))
              .setMin(getAnnotation(IntMin.class, IntMin::value));
   }

   @Override
   public void save(Toml writer) {
      writer.put(getKey(), get());
   }

   @Nullable
   @Override
   protected List<Integer> from(Toml object) {
      return object.getList(getKey(), Number::intValue);
   }
}
