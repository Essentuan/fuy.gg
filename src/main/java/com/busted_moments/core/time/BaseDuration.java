package com.busted_moments.core.time;

import com.busted_moments.core.util.NumUtil;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.stream.Stream;

public class BaseDuration implements Duration {
   private final double seconds;

   protected BaseDuration(double seconds) {
      this.seconds = seconds;
   }

   private Duration operator(Duration other, DoubleBinaryOperator operator) {
      return new BaseDuration(operator.applyAsDouble(seconds, seconds(other)));
   }

   @Override
   public Duration plus(Duration duration) {
      return operator(duration, Double::sum);
   }

   @Override
   public Duration minus(Duration duration) {
      return operator(duration, (left, right) -> left - right);
   }

   @Override
   public Duration multiply(Duration other) {
      return operator(other, (left, right) -> left * right);
   }

   @Override
   public Duration divide(Duration other) {
      return operator(other, (left, right) -> left / right);
   }

   @Override
   public Duration mod(Duration other) {
      return operator(other, (left, right) -> left % right);
   }

   @Override
   public Duration pow(Duration exponent) {
      return operator(exponent, Math::pow);
   }

   @Override
   public Duration min(Duration other) {
      return lessThan(other) ? this : other;
   }

   @Override
   public Duration max(Duration other) {
      return greaterThan(other) ? this : other;
   }

   @Override
   public Duration abs() {
      return new BaseDuration(Math.abs(seconds));
   }

   @Override
   public double to(ChronoUnit unit) {
      return seconds / unit.toSeconds();
   }

   @Override
   public double getPart(ChronoUnit unit) {
      return Math.floor(switch (unit) {
         case NANOSECONDS -> toNanos() % 1000;
         case MICROSECONDS -> toMicros() % 1000;
         case MILLISECONDS -> toMills() % 1000;
         case SECONDS -> toSeconds() % 60;
         case MINUTES -> toMinutes() % 60;
         case HOURS -> toHours() % 24;
         case DAYS -> toDays() % 7;
         case WEEKS -> toWeeks() % 4;
         case MONTHS -> toMonths() % 12;
         case YEARS -> toYears();
      });
   }

   @Override
   public boolean greaterThan(Duration duration) {
      return seconds > seconds(duration);
   }

   @Override
   public boolean greaterThanOrEqual(Duration duration) {
      return seconds >= seconds(duration);
   }

   @Override
   public boolean lessThan(Duration duration) {
      return seconds < seconds(duration);
   }

   @Override
   public boolean lessThanOrEqual(Duration duration) {
      return seconds <= seconds(duration);
   }

   @Override
   public boolean isForever() {
      return NumUtil.isForever(seconds);
   }

   @Override
   public java.time.Duration toNative() {
      long seconds = (long) Math.min(toSeconds(), Long.MAX_VALUE);
      int nanos = Math.min((int) ((this.seconds - seconds) / ChronoUnit.NANOSECONDS.toSeconds()), 999_999_999);

      return java.time.Duration.ofSeconds(seconds, nanos);
   }

   @Override
   public long get(TemporalUnit unit) {
      return (long) (seconds / unit.getDuration().toSeconds());
   }

   @Override
   public List<TemporalUnit> getUnits() {
      return Stream.of(ChronoUnit.values())
              .filter(unit -> getPart(unit) != 0 && !NumUtil.isForever(getPart(unit)))
              .map(TemporalUnit.class::cast)
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
      return toString(ChronoUnit.MILLISECONDS);
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

   private static double seconds(Duration duration) {
      return duration instanceof BaseDuration obj ? obj.seconds : duration.toSeconds();
   }

   public double seconds() {
      return seconds;
   }
}
