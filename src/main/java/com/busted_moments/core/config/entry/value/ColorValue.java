package com.busted_moments.core.config.entry.value;

import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.config.Buildable;
import com.busted_moments.core.config.entry.ConfigEntry;
import com.wynntils.utils.colors.CustomColor;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import me.shedaniel.math.Color;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

public class ColorValue extends ConfigEntry<Color> {
   public ColorValue(Component title, Object ref, Field field, Buildable<?, ?> parent) {
      super(title, ref, field, parent,
              Annotated.Placeholder(Config.Alpha.class)
      );
   }

   @Override
   protected AbstractFieldBuilder<Color, ?, ?> create(ConfigEntryBuilder builder) {return null;}

   @Override
   public AbstractConfigListEntry<?> build(ConfigEntryBuilder builder) {
      boolean useAlpha = hasAnnotation(Config.Alpha.class);

      var res = builder.startColorField(getTitle(), useAlpha ? get().getColor() : getRGB(get()))
                      .setAlphaMode(useAlpha);

      res.setSaveConsumer(useAlpha ? i -> set(Color.ofTransparent(i)) : i -> set(Color.ofOpaque(i)));
      res.setDefaultValue(useAlpha ? getDefault().getColor() : getRGB(getDefault()));

      if (hasAnnotation(Config.Tooltip.class)) {
         res.setTooltip(getTooltip());
      }

      var element = res.build();

      element.setEditable(!hasAnnotation(Config.Frozen.class));

      return element;
   }

   private static int getRGB(Color color) {
      return color.getRed()<<16 | color.getGreen()<<8 | color.getBlue();
   }
}
