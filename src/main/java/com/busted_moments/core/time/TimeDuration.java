package com.busted_moments.core.time;

import com.busted_moments.core.util.NumUtil;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.stream.Stream;

class TimeDuration implements Duration {
   private final double seconds;

   protected TimeDuration(double seconds) {
      this.seconds = seconds;
   }

   @Override
   public Duration plus(Duration duration) {
      return new TimeDuration(seconds + duration.toSeconds());
   }

   @Override
   public Duration minus(Duration duration) {
      return new TimeDuration(seconds - duration.toSeconds());
   }

   @Override
   public Duration abs() {
      return new TimeDuration(Math.abs(seconds));
   }

   @Override
   public double to(TimeUnit unit) {
      return seconds / unit.toSeconds();
   }

   @Override
   public double getPart(TimeUnit unit) {
      return Math.floor(switch (unit) {
         case NANOSECONDS -> toNanos() % 1000;
         case MICROSECONDS -> toMicros() % 1000;
         case MILLISECONDS -> toMills() % 1000;
         case SECONDS -> toSeconds() % 60;
         case MINUTES -> toMinutes() % 60;
         case HOURS -> toHours() % 24;
         case DAYS -> toDays() % 7;
         case WEEKS -> toWeeks() % 5;
         case MONTHS -> toMonths() % 12;
         case YEARS -> toYears();
      });
   }

   @Override
   public boolean greaterThan(Duration duration) {
      return seconds > duration.toSeconds();
   }

   @Override
   public boolean greaterThanOrEqual(Duration duration) {
      return seconds >= duration.toSeconds();
   }

   @Override
   public boolean lessThan(Duration duration) {
      return seconds < duration.toSeconds();
   }

   @Override
   public boolean lessThanOrEqual(Duration duration) {
      return seconds <= duration.toSeconds();
   }

   @Override
   public boolean isForever() {
      return NumUtil.isForever(seconds);
   }

   @Override
   public java.time.Duration toNative() {
      long seconds = (long) Math.min(toSeconds(), Long.MAX_VALUE);
      int nanos = Math.min((int) ((this.seconds - seconds) / TimeUnit.NANOSECONDS.toSeconds()), 999_999_999);

      return java.time.Duration.ofSeconds(seconds, nanos);
   }

   @Override
   public long get(TemporalUnit unit) {
      return (long) (seconds / unit.getDuration().toSeconds());
   }

   @Override
   public List<TemporalUnit> getUnits() {
      return Stream.of(TimeUnit.values())
              .filter(unit -> getPart(unit) != 0 && !NumUtil.isForever(getPart(unit)))
              .map(unit -> (TemporalUnit) unit)
              .toList();
   }

   @Override
   public Temporal addTo(Temporal temporal) {
      return temporal.plus(this);
   }

   @Override
   public Temporal subtractFrom(Temporal temporal) {
      return temporal.minus(this);
   }

   @Override
   public String toString() {
      return toString(TimeUnit.MILLISECONDS);
   }

   @Override
   public int compareTo(@NotNull Duration duration) {
      return Double.compare(seconds, duration.toSeconds());
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;

      return (obj instanceof Duration duration) && seconds == duration.toSeconds();
   }

   @Override
   public int hashCode() {
      return Double.hashCode(seconds);
   }
}
