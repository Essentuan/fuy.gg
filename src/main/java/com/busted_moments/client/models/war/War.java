package com.busted_moments.client.models.war;

import com.busted_moments.core.api.requests.mapstate.Territory;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.ChronoUnit;

import java.util.*;

public final class War {
   private final Territory territory;
   private final Date enteredAt;
   Date startedAt;
   Tower tower;

   private Date endedAt = null;

   public War(Territory territory, Date enteredAt) {
      this.territory = territory;
      this.enteredAt = enteredAt;
   }

   public Territory getTerritory() {
      return territory;
   }

   public Date getEnteredAt() {
      return enteredAt;
   }

   public Date getStartedAt() {
      return startedAt;
   }

   public Date getEndedAt() {
      return endedAt;
   }

   public Tower getTower() {
      return tower;
   }

   public double getDPS(Duration duration) {
      if (tower == null || tower.size() < 2) return 0;

      if (duration.isForever()) return (tower.getInitialStats().ehp() - tower.getStats().ehp()) / Duration.since(getStartedAt()).toSeconds();

      final Tower.Stats[] points = {null, null};

      tower.stream()
              .filter(update -> Duration.since(update.date()).lessThanOrEqual(duration))
              .forEach(update -> {
                 if (points[0] == null) points[0] = update.before();
                 points[1] = update.after();
              });

      if (points[0] == null || points[1] == null) return 0;
      return (points[0].ehp() - points[1].ehp()) / duration.toSeconds();
   }

   public double getDPS(double length, ChronoUnit unit) {
      return getDPS(Duration.of(length, unit));
   }

   public Duration getDuration() {
      if (startedAt == null) return Duration.of(0, ChronoUnit.SECONDS);

      return Duration.since(startedAt);
   }

   public boolean hasStarted() {
      return startedAt != null;
   }

   public boolean hasEnded() {
      return endedAt != null;
   }

   void end() {
      if (!hasEnded()) endedAt = new Date();
   }

   @Override
   public String toString() {
      return "War{" +
              "territory=" + territory +
              ", enteredAt=" + enteredAt +
              ", startedAt=" + startedAt +
              ", tower=" + tower +
              '}';
   }
}
