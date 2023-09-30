package com.busted_moments.core.time;

import com.busted_moments.core.util.NumUtil;
import com.busted_moments.core.util.StringUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public interface Duration extends TemporalAmount, Comparable<Duration> {
   Duration FOREVER = new TimeDuration(Double.MAX_VALUE);

   Duration plus(Duration duration);

   default Duration plus(double length, TimeUnit unit) {
      return plus(Duration.of(length, unit));
   }

   default Duration add(Duration duration) {
      return plus(duration);
   }

   default Duration add(double length, TimeUnit unit) {
      return plus(length, unit);
   }

   Duration minus(Duration duration);

   default Duration minus(double length, TimeUnit unit) {
      return minus(Duration.of(length, unit));
   }

   default Duration subtract(Duration duration) {
      return minus(duration);
   }

   default Duration subtract(double length, TimeUnit unit) {
      return minus(length, unit);
   }

   Duration abs();

   double to(TimeUnit unit);

   double getPart(TimeUnit unit);

   boolean greaterThan(Duration duration);

   default boolean greaterThan(double length, TimeUnit unit) {
      return greaterThan(Duration.of(length, unit));
   }

   boolean greaterThanOrEqual(Duration duration);

   default boolean greaterThanOrEqual(double length, TimeUnit unit) {
      return greaterThanOrEqual(Duration.of(length, unit));
   }

   boolean lessThan(Duration duration);

   default boolean lessThan(double length, TimeUnit unit) {
      return lessThan(Duration.of(length, unit));
   }

   boolean lessThanOrEqual(Duration duration);

   default boolean lessThanOrEqual(double length, TimeUnit unit) {
      return lessThanOrEqual(Duration.of(length, unit));
   }

   default boolean equals(double length, TimeUnit unit) {
      return equals(Duration.of(length, unit));
   }

   boolean isForever();

   default String toString(FormatFlag... flags) {
      return new Formatter(this, flags).toString();
   }

   java.time.Duration toNative();

   default double toNanos() {
      return to(TimeUnit.NANOSECONDS);
   }

   default double toMicros() {
      return to(TimeUnit.MICROSECONDS);
   }

   default double toMills() {
      return to(TimeUnit.MILLISECONDS);
   }

   default double toSeconds() {
      return to(TimeUnit.SECONDS);
   }

   default double toMinutes() {
      return to(TimeUnit.MINUTES);
   }

   default double toHours() {
      return to(TimeUnit.HOURS);
   }

   default double toDays() {
      return to(TimeUnit.DAYS);
   }

   default double toWeeks() {
      return to(TimeUnit.WEEKS);
   }

   default double toMonths() {
      return to(TimeUnit.MONTHS);
   }

   default double toYears() {
      return to(TimeUnit.YEARS);
   }

   static Duration of(double length, TimeUnit unit) {
      return new TimeDuration(length * unit.toSeconds());
   }

   static Duration of(Number length, TimeUnit unit) {
      return of(length.doubleValue(), unit);
   }


   static Duration of(double length, java.util.concurrent.TimeUnit unit) {
      return of(length, TimeUnit.from(unit));
   }

   static Duration of(Date start, Date end) {
      return of(end.getTime() - start.getTime(), TimeUnit.MILLISECONDS);
   }

   static Duration from(java.time.Duration duration) {
      return Duration.of(duration.getSeconds(), TimeUnit.SECONDS)
              .plus(duration.getNano(), TimeUnit.NANOSECONDS);
   }

   static Duration since(Date date) {
      return of(new Date().getTime() - date.getTime(), TimeUnit.MILLISECONDS);
   }

   static int compare(Duration duration1, Duration duration2) {
      return ObjectUtils.compare(duration1, duration2);
   }

   static Optional<Duration> parse(String string) {
      int index;

      string = string.replaceAll(" ", "").toLowerCase();

      Pattern pattern = TimeUnit.REGEX();

      Duration duration = Duration.of(0, TimeUnit.SECONDS);

      while ((index = StringUtil.indexOf(string, pattern)) != -1) {
         for (TimeUnit unit : TimeUnit.sorted()) {
            int length;

            if (string.startsWith(unit.toPlural().toLowerCase(), index)) length = unit.toPlural().length();
            else if (string.startsWith(unit.toSingular().toLowerCase(), index)) length = unit.toSingular().length();
            else if (string.startsWith(unit.getSuffix(), index)) length = unit.getSuffix().length();
            else continue;

            String number = string.substring(0, index);

            if (!NumberUtils.isParsable(number)) {
               return Optional.empty();
            }

            duration = duration.add(Double.parseDouble(number), unit);

            string = string.substring(index + length);

            break;
         }
      }

      return duration.lessThanOrEqual(0, TimeUnit.SECONDS) ? Optional.empty() : Optional.of(duration);
   }

   class Formatter {
      protected TimeUnit SMALLEST_UNIT = TimeUnit.MILLISECONDS;

      protected BiFunction<Double, TimeUnit, String> SUFFIX_GETTER = (length, unit) -> {
         String suffix = switch (unit) {
            case NANOSECONDS -> " nanosecond";
            case MICROSECONDS -> " microsecond";
            case MILLISECONDS -> " millisecond";
            case SECONDS -> " second";
            case MINUTES -> " minute";
            case HOURS -> " hour";
            case DAYS -> " day";
            case WEEKS -> " week";
            case MONTHS -> " month";
            case YEARS -> " year";
         };

         return (length > 1 ? suffix + "s" : suffix) + " ";
      };

      protected final Duration duration;

      private Formatter(Duration duration, FormatFlag... flags) {
            for (FormatFlag flag : flags) {
                flag.apply(this);
            }

            this.duration = duration;
      }

      @Override
      public String toString() {
         if (duration.isForever()) return "Forever";
         else if (duration.lessThan(1, SMALLEST_UNIT)) return SUFFIX_GETTER.apply(0D, SMALLEST_UNIT);

         List<TimeUnit> units = Lists.reverse(List.of(TimeUnit.values()));

         StringBuilder builder = new StringBuilder();

         for (TimeUnit unit : units) {
            double value = duration.getPart(unit);

            if (value != 0 && !NumUtil.isForever(value)) {
               builder.append((int) value)
                       .append(SUFFIX_GETTER.apply(value, unit));
            }

            if (unit.equals(SMALLEST_UNIT)) {
               return builder.toString().trim();
            }
         }

         return builder.toString().trim();
      }
   }
}
