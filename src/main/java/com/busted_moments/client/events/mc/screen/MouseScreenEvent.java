package com.busted_moments.client.events.mc.screen;

import com.busted_moments.core.events.BaseEvent;
import net.minecraft.client.gui.screens.Screen;

public abstract class MouseScreenEvent extends BaseEvent {
   private double mouseX;
   private double mouseY;
   private int button;
   private Screen screen;

   public MouseScreenEvent(double mouseX, double mouseY, int button, Screen screen) {
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      this.button = button;
      this.screen = screen;
   }

   public double getMouseX() {
      return mouseX;
   }

   public double getMouseY() {
      return mouseY;
   }

   public int getMouseButton() {
      return button;
   }

   public Screen getScreen() {
      return screen;
   }

   public MouseScreenEvent() {}

   public interface Factory<T extends MouseScreenEvent> {
      T create(double mouseX, double mouseY, int button, Screen screen);
   }
}
