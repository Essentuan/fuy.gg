package com.busted_moments.client.events.mc.screen;

import net.minecraft.client.gui.screens.Screen;

public class ScreenClickedEvent extends MouseScreenEvent {
   public ScreenClickedEvent(double mouseX, double mouseY, int button, Screen screen) {
      super(mouseX, mouseY, button, screen);
   }

   public ScreenClickedEvent() {}
}
