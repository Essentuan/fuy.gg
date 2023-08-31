package com.busted_moments.client.mixin;

import com.busted_moments.client.events.mc.screen.MouseScreenEvent;
import com.busted_moments.client.events.mc.screen.ScreenClickedEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.wynntils.utils.mc.McUtils.mc;

@Mixin(Screen.class)
public abstract class ScreenMixin {
   @Inject(method = "wrapScreenError", at = @At("TAIL"))
   //We suppress this for future events
   @SuppressWarnings("SwitchStatementWithTooFewBranches")
   private static void onScreenAction(Runnable action, String errorDesc, String screenName, CallbackInfo ci) {
      switch (errorDesc) {
         case "mouseClicked event handler" -> create(ScreenClickedEvent::new).post();
      }
   }

   @Unique
   private static <T extends MouseScreenEvent> T create(MouseScreenEvent.Factory<T> factory) {
      Minecraft mc = mc();
      MouseHandler mouse = mc.mouseHandler;

      double x = mouse.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
      double y = mouse.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
      int button = ((MouseHandlerAccessor) mouse).getActiveButton();

      return factory.create(x, y, button, mc.screen);
   }
}
