package com.busted_moments.core.config.entry.list;

import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.config.Buildable;
import com.busted_moments.core.config.entry.ConfigEntry;
import com.busted_moments.core.config.entry.value.DoubleValue;
import com.busted_moments.core.toml.Toml;
import com.essentuan.acf.core.command.arguments.builtin.primitaves.Double.DoubleMax;
import com.essentuan.acf.core.command.arguments.builtin.primitaves.Double.DoubleMin;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public class DoubleList extends ConfigEntry<List<Double>> {
   public DoubleList(Component title, Object ref, Field field, Buildable<?, ?> parent) {
      super(title, ref, field, parent,
              Annotated.Optional(new DoubleValue.Max()),
              Annotated.Optional(new DoubleValue.Min())
      );
   }

   @Override
   protected AbstractFieldBuilder<List<Double>, ?, ?> create(ConfigEntryBuilder builder) {
      return create(builder::startDoubleList)
              .setMax(getAnnotation(DoubleMax.class, DoubleMax::value))
              .setMin(getAnnotation(DoubleMin.class, DoubleMin::value));
   }

   @Override
   public void save(Toml toml) {
      toml.put(getKey(), get());
   }

   @Override
   protected @Nullable List<Double> from(Toml object) {
      return object.getList(getKey(), Number::doubleValue);
   }
}
