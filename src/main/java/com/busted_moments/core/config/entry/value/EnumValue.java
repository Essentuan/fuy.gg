package com.busted_moments.core.config.entry.value;

import com.busted_moments.core.config.Buildable;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.config.entry.ConfigEntry;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("rawtypes")
public class EnumValue extends ConfigEntry<Enum> {
   public EnumValue(Component title, Object ref, Field field, Buildable<?, ?> parent) {
      super(title, ref, field, parent);
   }

   @SuppressWarnings("unchecked")
   public Class<Enum> getEnum() {
      return (Class<Enum>) getField().getType();
   }

   @Override
   protected AbstractFieldBuilder<Enum, ?, ?> create(ConfigEntryBuilder builder) {
      return null;
   }

   @Override
   @SuppressWarnings("unchecked")
   public AbstractConfigListEntry<?> build(ConfigEntryBuilder builder) {
      var res = builder.startDropdownMenu(getTitle(),
              DropdownMenuBuilder.TopCellElementBuilder.of(get(), string -> {
                 try {
                    return Enum.valueOf(getEnum(), string);
                 } catch (Throwable t) {
                    return null;
                 }
              }, e -> Component.literal(e.toString())),
              DropdownMenuBuilder.CellCreatorBuilder.of(e -> Component.literal(e.toString()))
      );

      res.setSelections(List.of(getEnum().getEnumConstants()));

      res.setSaveConsumer(this::set);

      res.setDefaultValue(this::getDefault);

      if (getTooltip() != null) {
         res.setTooltip(getTooltip());
      }

      var element = res.build();

      element.setEditable(!hasAnnotation(Config.Frozen.class));

      return element;
   }
}
