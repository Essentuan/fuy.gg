package com.busted_moments.core.time;

import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum TimeUnit implements TemporalUnit, FormatFlag {
   NANOSECONDS(ChronoUnit.NANOS, (double) 1 / 1_000_000_000, "ns"),
   MICROSECONDS(ChronoUnit.MICROS, (double) 1 / 1_000_000, "us"),
   MILLISECONDS(ChronoUnit.MILLIS, (double) 1 / 1_000, "ms"),
   SECONDS(ChronoUnit.SECONDS, 1, "s"),
   MINUTES(ChronoUnit.MINUTES, SECONDS.toSeconds() * 60, "m"),
   HOURS(ChronoUnit.HOURS, MINUTES.toSeconds() * 60, "h"),
   DAYS(ChronoUnit.DAYS, HOURS.toSeconds() * 24, "d"),
   WEEKS(ChronoUnit.WEEKS, DAYS.toSeconds() * 7, "w"),
   MONTHS(ChronoUnit.MONTHS, WEEKS.toSeconds() * 5, "mo"),
   YEARS(ChronoUnit.YEARS, MONTHS.toSeconds() * 12, "y");

   private static final List<TimeUnit> SORTED = Stream.of(values())
           .sorted(Comparator.<TimeUnit, Integer>comparing(unit -> unit.getSuffix() == null ? 0 : unit.getSuffix().length()).reversed())
           .toList();

   private final TemporalUnit base;
   private final double seconds;
   private final String suffix;

   TimeUnit(TemporalUnit base, double seconds, String suffix) {
      this.base = base;
      this.seconds = seconds;
      this.suffix = suffix;
   }

   @Override
   public java.time.Duration getDuration() {
      return Duration.of(1, this).toNative();
   }

   public double toSeconds() {
      return seconds;
   }

   public String getSuffix() {
      return suffix;
   }

   public String toPlural() {
      return toString();
   }

   public String toSingular() {
      return toString().substring(0, toString().length() - 1);
   }

   @Override
   public boolean isDurationEstimated() {
      return false;
   }

   @Override
   public boolean isDateBased() {
      return false;
   }

   @Override
   public boolean isTimeBased() {
      return true;
   }

   @Override
   @SuppressWarnings("unchecked")
   public <R extends Temporal> R addTo(R temporal, long amount) {
      return (R) Duration.of(toSeconds() * amount, SECONDS).addTo(temporal);
   }

   @Override
   public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
      return temporal1Inclusive.until(temporal2Exclusive, this);
   }

   @Override
   public void apply(Duration.Formatter formatter) {
      formatter.SMALLEST_UNIT = this;
   }

   public static Pattern REGEX() {
      return Pattern.compile(SORTED.stream()
              .filter(unit -> unit.getSuffix() != null)
              .map(unit -> "((%s)|(%s)|(%s))".formatted(unit.toPlural(), unit.toSingular(), unit.getSuffix()))
              .collect(Collectors.joining("|")));
   }

   public static List<TimeUnit> sorted() {
      return SORTED;
   }

   public static java.util.concurrent.TimeUnit toNative(TimeUnit unit) {
      return switch(unit) {
         case NANOSECONDS -> java.util.concurrent.TimeUnit.NANOSECONDS;
         case MICROSECONDS -> java.util.concurrent.TimeUnit.MICROSECONDS;
         case MILLISECONDS -> java.util.concurrent.TimeUnit.MILLISECONDS;
         case SECONDS -> java.util.concurrent.TimeUnit.SECONDS;
         case MINUTES -> java.util.concurrent.TimeUnit.MINUTES;
         case HOURS -> java.util.concurrent.TimeUnit.HOURS;
         case DAYS -> java.util.concurrent.TimeUnit.DAYS;
         default -> throw new IllegalStateException("Unexpected value: " + unit);
      };
   }

   public static TimeUnit from(java.util.concurrent.TimeUnit unit) {
      return switch(unit) {
         case NANOSECONDS -> NANOSECONDS;
         case MICROSECONDS -> MICROSECONDS;
         case MILLISECONDS -> MILLISECONDS;
         case SECONDS -> SECONDS;
         case MINUTES -> MINUTES;
         case HOURS -> HOURS;
         case DAYS -> DAYS;
      };
   }
}
