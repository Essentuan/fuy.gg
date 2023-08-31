package com.busted_moments.core.render.overlay.elements.textbox;

import java.util.Objects;

public class Padding {
   public float left;
   public float top;
   public float right;
   public float bottom;

   public Padding(float left, float top, float right, float bottom) {
      this.left = left;
      this.top = top;
      this.right = right;
      this.bottom = bottom;
   }

   public float left() {
      return left;
   }

   public float top() {
      return top;
   }

   public float right() {
      return right;
   }

   public float bottom() {
      return bottom;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) return true;
      if (obj == null || obj.getClass() != this.getClass()) return false;
      var that = (Padding) obj;
      return Float.floatToIntBits(this.left) == Float.floatToIntBits(that.left) &&
              Float.floatToIntBits(this.top) == Float.floatToIntBits(that.top) &&
              Float.floatToIntBits(this.right) == Float.floatToIntBits(that.right) &&
              Float.floatToIntBits(this.bottom) == Float.floatToIntBits(that.bottom);
   }

   @Override
   public int hashCode() {
      return Objects.hash(left, top, right, bottom);
   }

   @Override
   public String toString() {
      return "Padding[" +
              "left=" + left + ", " +
              "top=" + top + ", " +
              "right=" + right + ", " +
              "bottom=" + bottom + ']';
   }
}
