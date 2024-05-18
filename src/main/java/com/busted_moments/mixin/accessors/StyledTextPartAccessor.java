package com.busted_moments.mixin.accessors;

import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledTextPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = StyledTextPart.class, remap = false)
public interface StyledTextPartAccessor {
   @Accessor
   String getText();

   @Accessor
   PartStyle getStyle();
}
