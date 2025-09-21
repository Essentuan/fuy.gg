package com.busted_moments.mixin.extensions;

import com.wynntils.utils.colors.CustomColor;
import kotlin.Triple;
import net.essentuan.esl.color.Color;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = CustomColor.class, remap = false)
public abstract class CustomColorExtension implements Color {
   @Shadow
   @Final
   public int r;

   @Shadow
   @Final
   public int g;

   @Shadow
   @Final
   public int b;

   @Shadow
   @Final
   public int a;

   @Shadow public abstract String toHexString();

   @NotNull
   @Override
   public java.awt.Color asAwt() {
      return Color.DefaultImpls.asAwt(this);
   }

   @Override
   public int getRed() {
      return this.r;
   }

   @Override
   public int getGreen() {
      return this.g;
   }

   @Override
   public int getBlue() {
      return this.b;
   }

   @Override
   public int getAlpha() {
      return this.a;
   }

   @NotNull
   @Override
   public Color brighten(float factor) {
      int i = (int) (1.0 / (1.0 - factor));

      if (r == 0 && g == 0 && b == 0)
         return with(i, i, i, i);

      return with(
              (int) Math.min(Math.max(r, i) / factor, 255.0f),
              (int) Math.min(Math.max(g, i) / factor, 255.0f),
              (int) Math.min(Math.max(b, i) / factor, 255.0f),
              a
      );
   }

   @NotNull
   @Override
   public Color darken(float factor) {
      return with(
              (int) Math.max(r / factor, 0f),
              (int) Math.max(g / factor, 0f),
              (int) Math.max(b / factor, 0f),
              a
      );
   }

   @NotNull
   @Override
   public Color with(int i, int i1, int i2, int i3) {
      return (Color) (Object) new CustomColor(i, i1, i2, i3);
   }

   @NotNull
   @Override
   public Color with(float v, float v1, float v2, float v3) {
      return (Color) (Object) new CustomColor(v, v1, v2, v3);
   }

   @Override
   public int asOpaque() {
      return r << 16 | g << 8 | b;
   }

   @Override
   public float @NotNull [] asFloatArray() {
      return new float[]{r / 255f, g / 255f, b / 255f};
   }

   @NotNull
   @Override
   public Triple<Float, Float, Float> asHsl() {
      return Color.DefaultImpls.asHsl(this);
   }

   @NotNull
   @Override
   public Triple<Float, Float, Float> asHSV() {
      return Color.DefaultImpls.asHSV(this);
   }

   @NotNull
   @Override
   public String asHex() {
      return this.toHexString();
   }
}
