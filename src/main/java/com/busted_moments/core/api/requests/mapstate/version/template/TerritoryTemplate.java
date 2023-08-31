package com.busted_moments.core.api.requests.mapstate.version.template;

import com.busted_moments.client.models.territory.eco.types.ResourceType;
import com.busted_moments.core.api.requests.mapstate.MapState;
import com.busted_moments.core.api.requests.mapstate.Territory;
import com.busted_moments.core.json.template.JsonTemplate;

import java.util.Map;
import java.util.Set;

public class TerritoryTemplate extends JsonTemplate {
   @Entry
   private String territory;
   @Entry
   private Map<ResourceType, Long> production;
   @Entry
   private MapState.Location location;
   @Entry
   private Set<String> connections;

   public String getName() {
      return territory;
   }

   public Map<ResourceType, Long> getProduction() {
      return production;
   }

   public Territory.Location getLocation() {
      return location;
   }

   public Set<String> getConnections() {
      return connections;
   }
}
