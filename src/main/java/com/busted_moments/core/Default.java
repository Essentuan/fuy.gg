package com.busted_moments.core;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface Default {
   State value();

   record Impl(State state) implements Default {
      @Override
      public State value() {
         return state;
      }

      @Override
      public Class<? extends Annotation> annotationType() {
         return Default.class;
      }
   }
}
