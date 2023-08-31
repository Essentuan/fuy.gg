package com.busted_moments.client.models.territory.eco;

import com.busted_moments.core.api.requests.mapstate.MapState;
import com.busted_moments.core.api.requests.mapstate.Territory;
import com.busted_moments.core.api.requests.mapstate.version.template.MapTemplate;
import com.busted_moments.core.tuples.Pair;
import com.wynntils.core.components.Models;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Route implements Iterable<Territory> {
   private final List<Territory> route;
   private boolean isIdeal = true;

   private Route(List<Territory> list) {
      this.route = list;
   }

   public Route append(Territory territory) {
      List<Territory> route = new ArrayList<>(this.route);
      route.add(territory);

      return new Route(route);
   }

   @NotNull
   @Override
   public Iterator<Territory> iterator() {
      return route.iterator();
   }

   static void visit(MapState state, MapTemplate template, GuildEco eco, boolean ideal) {
      if (eco.HQ == null) return;

      eco.HQ.route = new Route(new ArrayList<>());
      eco.HQ.absolute = eco.HQ.route;

      String guild = Models.Guild.getGuildName();

      Set<String> visited = new HashSet<>();
      Set<String> completed = new HashSet<>();

      Queue<Pair<String, Route>> queue = new LinkedList<>();
      queue.add(new Pair<>(eco.HQ.getName(), eco.HQ.route));

      while (!queue.isEmpty() && eco.size() > completed.size()) {
         var next = queue.poll();

         if (visited.contains(next.one())) continue;

         var territory = state.get(next.one());

         if (!ideal && !Objects.equals(territory.getOwner().getGuild(), guild)) continue;

         if (eco.contains(territory)) {
            var terr = eco.get(territory);
            if (ideal) terr.absolute = next.two();
            else terr.route = next.two();

            completed.add(terr.getName());
         }

         visited.add(next.one());

         var route = next.two().append(territory);
         template.get(territory).getConnections().forEach(connection -> queue.add(new Pair<>(connection, route)));
      }
   }
}
