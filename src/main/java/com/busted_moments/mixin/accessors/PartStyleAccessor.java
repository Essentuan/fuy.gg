package com.busted_moments.mixin.accessors;

import com.wynntils.core.text.PartStyle;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = PartStyle.class, remap = false)
public interface PartStyleAccessor  {
   @Accessor
   CustomColor getColor();

   @Accessor
   ClickEvent getClickEvent();

   @Accessor
   HoverEvent getHoverEvent();
}
