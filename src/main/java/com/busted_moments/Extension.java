package com.busted_moments;

public interface Extension {
   String getPackage();

   //This is mainly for SoundProvider
   default String[] getSounds() {
      return new String[0];
   }
}
