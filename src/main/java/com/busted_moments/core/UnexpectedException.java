package com.busted_moments.core;

public class UnexpectedException extends RuntimeException {
   public UnexpectedException() {}

   public UnexpectedException(Throwable cause) {
      super(cause);
   }

   public UnexpectedException(String message, Object... args) {
      super(message.formatted(args));
   }

   public UnexpectedException(String message, Throwable cause, Object... args) {
      super(message.formatted(args), cause);
   }

   public static RuntimeException propagate(Throwable e) {
      if (e instanceof ReflectiveOperationException roe)
         return propagate(roe.getCause());
      else if (e instanceof RuntimeException re)
         return re;
      else
         return new UnexpectedException(e);
   }
}
