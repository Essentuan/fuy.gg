package com.busted_moments;

public interface FuyExtension {
   String getPackage();

   //This is mainly for SoundProvider
   default String[] getSounds() {
      return new String[0];
   }
}
