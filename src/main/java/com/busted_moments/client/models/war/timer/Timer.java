package com.busted_moments.client.models.war.timer;

import com.busted_moments.client.models.war.Defense;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.ChronoUnit;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public final class Timer implements Comparable<Timer> {
   private final String territory;
   private final Date start;
   private final Duration timer;

   boolean confident = false;

   boolean personal = false;

   Defense defense = Defense.UNKNOWN;

   Timer(String territory, Duration timer) {
      this.territory = territory;
      this.start = new Date();
      this.timer = timer;
   }

   public Timer(String territory, Duration timer, Defense defense) {
      this.territory = territory;
      this.start = new Date();
      this.timer = timer.add(499, ChronoUnit.MILLISECONDS);
      this.defense = defense;
      this.confident = true;
   }

   public String getTerritory() {
      return territory;
   }

   public Date getStart() {
      return start;
   }

   public Duration getDuration() {
      return timer;
   }

   public Defense getDefense() {
      return defense;
   }

   public boolean isConfident() {
      return confident;
   }

   public boolean isPersonal() {
      return personal;
   }

   public Duration getRemaining() {
      return timer.minus(Duration.since(start));
   }

   @Override
   public String toString() {
      return "Timer{" +
              "territory='" + territory + '\'' +
              ", start=" + start +
              ", timer=" + timer +
              ", confident=" + confident +
              ", defense=" + defense +
              '}';
   }

   @Override
   public int compareTo(@NotNull Timer o) {
      return compare(this, o);
   }

   public static int compare(Timer timer1, Timer timer2) {
      int result = Duration.compare(timer1.getRemaining(), timer2.getRemaining());
      if (result == 0) return timer1.getTerritory().compareTo(timer2.getTerritory());

      return result;
   }
}
