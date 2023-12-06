package com.busted_moments.core;

public class UnexpectedException extends RuntimeException {
   public UnexpectedException() {}

   public UnexpectedException(Throwable cause) {
      super (cause);
   }

   public UnexpectedException(String message, Object... args) {
      super(message.formatted(args));
   }

   public UnexpectedException(String message, Throwable cause, Object... args) {
      super(message.formatted(args), cause);
   }

   public static RuntimeException propagate(Throwable e) {
      return switch (e) {
         case ReflectiveOperationException roe -> propagate(roe.getCause());
         case RuntimeException runtime -> runtime;
         default -> new UnexpectedException(e);
      };
   }
}
