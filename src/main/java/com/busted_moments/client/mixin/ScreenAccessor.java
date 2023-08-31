package com.busted_moments.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenAccessor {
   @Invoker
   void callRenderTooltip(PoseStack poseStack, ItemStack itemStack, int mouseX, int mouseY);
}
