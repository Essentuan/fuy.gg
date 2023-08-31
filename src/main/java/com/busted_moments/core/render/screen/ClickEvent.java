package com.busted_moments.core.render.screen;

public interface ClickEvent {
   interface Handler<T> {
      boolean accept(double mouseX, double mouseY, int button, T widget);
   }
}
