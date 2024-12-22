package com.busted_moments.mixin;

import com.wynntils.core.persisted.config.Category;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Category.class, remap = false)
public abstract class CategoryMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/String;startsWith(Ljava/lang/String;)Z"))
    public boolean startsWith(String instance, String prefix) {
        return false;
    }
}
