package com.busted_moments.core.http.requests.mapstate.version.template;

import com.busted_moments.client.models.territory.eco.types.ResourceType;
import com.busted_moments.core.http.requests.mapstate.MapState;
import com.busted_moments.core.http.requests.mapstate.Territory;
import com.busted_moments.core.json.BaseModel;

import java.util.Map;
import java.util.Set;

public class TerritoryTemplate extends BaseModel {
   @Key
   private String territory;
   @Key
   private Map<ResourceType, Long> production;
   @Key
   private MapState.Location location;
   @Key
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
