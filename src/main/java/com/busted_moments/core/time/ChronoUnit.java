package com.busted_moments.core.time;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ChronoUnit implements TemporalUnit, FormatFlag {
   NANOSECONDS((double) 1 / 1_000_000_000, "ns"),
   MICROSECONDS((double) 1 / 1_000_000, "us"),
   MILLISECONDS((double) 1 / 1_000, "ms"),
   SECONDS(1, "s"),
   MINUTES(SECONDS.toSeconds() * 60, "m"),
   HOURS(MINUTES.toSeconds() * 60, "h"),
   DAYS(HOURS.toSeconds() * 24, "d"),
   WEEKS(DAYS.toSeconds() * 7, "w"),
   MONTHS(WEEKS.toSeconds() * 4, "mo"),
   YEARS(MONTHS.toSeconds() * 12, "y");


   private static final List<ChronoUnit> SORTED = Stream.of(values())
           .sorted(Comparator.<ChronoUnit, Integer>comparing(unit -> unit.getSuffix() == null ? 0 : unit.getSuffix().length()).reversed())
           .toList();

   private final double seconds;
   private final String suffix;

   ChronoUnit(double seconds, String suffix) {
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

   public static List<ChronoUnit> sorted() {
      return SORTED;
   }

   public static java.util.concurrent.TimeUnit toNative(ChronoUnit unit) {
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

   public static ChronoUnit from(java.util.concurrent.TimeUnit unit) {
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
