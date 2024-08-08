package com.busted_moments.mixin.accessors;

import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
   @Accessor("LOGGER")
   static Logger getLogger() {
      throw new AssertionError();
   }
}