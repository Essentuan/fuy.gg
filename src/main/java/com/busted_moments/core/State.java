package com.busted_moments.core;

public enum State {
   ENABLED(true),
   DISABLED(false);

   private final boolean state;

   State(boolean state) {
      this.state = state;
   }

   public boolean asBoolean() {
      return state;
   }
}
