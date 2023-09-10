package com.busted_moments.core.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class NumUtil {
   private static final BigDecimal THOUSAND = new BigDecimal("1000");
   private static final BigDecimal MILLION = THOUSAND.multiply(THOUSAND);
   private static final BigDecimal BILLION = MILLION.multiply(THOUSAND);
   private static final BigDecimal TRILLION = BILLION.multiply(THOUSAND);
   private static final BigDecimal QUADRILLION = TRILLION.multiply(THOUSAND);

   /**
    * Truncates a number to at most 6-length string, with possible unit at last.
    * <br>Example input: 1234567, Output: 1.235M
    * @param number Number to format.
    * @return Formatted string.
    */
   public static String truncate(BigDecimal number) {
      String prefix = "";

      if (number.signum() < 0) {
         prefix = "-";
         number = number.abs();
      }

      if (number.compareTo(THOUSAND) >= 0 && number.compareTo(MILLION) < 0) {
         BigDecimal answer = number.divide(THOUSAND, 3, RoundingMode.HALF_UP);
         int scale = (answer.precision() - answer.scale());
         return prefix + answer.setScale(4-scale, RoundingMode.HALF_UP) + "K";
      } else if (number.compareTo(MILLION) >= 0 && number.compareTo(BILLION) < 0) {
         BigDecimal answer = number.divide(MILLION, 3, RoundingMode.HALF_UP);
         int scale = (answer.precision() - answer.scale());
         return prefix + answer.setScale(4-scale, RoundingMode.HALF_UP) + "M";
      } else if (number.compareTo(BILLION) >= 0 && number.compareTo(TRILLION) < 0) {
         BigDecimal answer = number.divide(BILLION, 3, RoundingMode.HALF_UP);
         int scale = (answer.precision() - answer.scale());
         return prefix + answer.setScale(4-scale, RoundingMode.HALF_UP) + "B";
      } else if (number.compareTo(TRILLION) >= 0 && number.compareTo(QUADRILLION) < 0) {
         BigDecimal answer = number.divide(TRILLION, 3, RoundingMode.HALF_UP);
         int scale = (answer.precision() - answer.scale());
         return prefix + answer.setScale(4-scale, RoundingMode.HALF_UP) + "T";
      } else if (number.compareTo(QUADRILLION) >= 0) {
         BigDecimal answer = number.divide(QUADRILLION, 3, RoundingMode.HALF_UP);
         int scale = (answer.precision() - answer.scale());
         return prefix + answer.setScale(Math.max(0,4-scale), RoundingMode.HALF_UP) + "Q";
      }

      return prefix + number.divide(BigDecimal.ONE, 0, RoundingMode.HALF_UP);
   }

   public static String truncate(long longValue) {
      return truncate(new BigDecimal(longValue));
   }

   public static String truncate(double value) {
      return truncate(BigDecimal.valueOf(value));
   }


   public static String truncate(double number, int decimals) {
      return new BigDecimal(number).setScale(decimals, RoundingMode.FLOOR).toString();
   }

   private static final DecimalFormatSymbols SYMBOLS = DecimalFormatSymbols.getInstance(Locale.ENGLISH);
   private static final NumberFormat DOUBLE_FORMATTER = new DecimalFormat("#,###.00", SYMBOLS);
   private static final NumberFormat LONG_FORMATTER = new DecimalFormat("#,###", SYMBOLS);

   public static String format(double number) {
      if (number == 0) return "0";

      return DOUBLE_FORMATTER.format(number);
   }

   public static String format(long number) {
      if (number == 0) return "0";

      return LONG_FORMATTER.format(number);
   }

   public static boolean isForever(double number) {
      return number == Double.MAX_VALUE || number == Double.NEGATIVE_INFINITY || number == Double.POSITIVE_INFINITY;
   }
}
