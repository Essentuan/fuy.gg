package com.busted_moments.core.api.requests.mapstate;

import com.busted_moments.core.Promise;
import com.busted_moments.core.api.requests.mapstate.version.MapVersion;
import com.busted_moments.core.json.template.JsonTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MapState extends JsonTemplate implements Territory.List<Territory> {
   @Entry("territoryList")
   private Map<String, Territory> state;
   @Entry("dateOfCapture")
   private Date timestamp;

   @Entry("mapVersion")
   private int version;
   private final Promise.Getter<Optional<MapVersion>> versionGetter = new Promise.Getter<>(
           () -> version != -1 ? new MapVersion.Request(version) : Promise.of(Optional.empty())
   );

   public MapState() {
   }

   MapState(Map<String, Territory> map) {
      this.state = map;
      this.timestamp = new Date();
      this.version = -1;
   }

   @Override
   public Territory get(String territory) {
      return state.get(territory);
   }

   @Override
   public boolean contains(String territory) {
      return state.containsKey(territory);
   }

   @Override
   public Date getTimestamp() {
      return timestamp;
   }

   @Override
   public int size() {
      return state.size();
   }

   @Override
   public boolean isEmpty() {
      return state.isEmpty();
   }

   public Promise<Optional<MapVersion>> getVersion() {
      return versionGetter.get();
   }

   @Override
   public Iterator<com.busted_moments.core.api.requests.mapstate.Territory> iterator() {
      var iter = state.values().iterator();

      return new Iterator<>() {
         @Override
         public boolean hasNext() {
            return iter.hasNext();
         }

         @Override
         public com.busted_moments.core.api.requests.mapstate.Territory next() {
            return iter.next();
         }
      };
   }

   @NotNull
   @Override
   public Object @NotNull [] toArray() {
      return state.values().toArray();
   }

   @NotNull
   @Override
   public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
      return state.values().toArray(a);
   }

   public static class Territory extends JsonTemplate implements com.busted_moments.core.api.requests.mapstate.Territory {
      @Entry
      private String territory;
      @Entry
      private MapState.Owner owner;
      @Entry
      private MapState.Location location;
      @Entry
      private Date acquired;


      @Override
      public String getName() {
         return territory;
      }

      @Override
      public Owner getOwner() {
         return owner;
      }

      @Override
      public Date getAcquired() {
         return acquired;
      }

      @Override
      public Location getLocation() {
         return location;
      }
   }

   public static class Owner extends JsonTemplate implements com.busted_moments.core.api.requests.mapstate.Territory.Owner {
      @Entry
      private String guild;
      @Entry
      private String prefix;

      private int owned = 0;

      @Override
      public String getGuild() {
         return guild;
      }

      @Override
      public String getPrefix() {
         return prefix;
      }

      @Override
      public int countOwned() {
         return owned;
      }

      @Override
      public void setOwned(int owned) {
         this.owned = owned;
      }
   }

   public static class Location extends JsonTemplate implements com.busted_moments.core.api.requests.mapstate.Territory.Location {
      @Entry
      private long startZ;
      @Entry
      private long startX;
      @Entry
      private long endZ;
      @Entry
      private long endX;

      @Override
      public long getStartX() {
         return startX;
      }

      @Override
      public long getStartZ() {
         return startZ;
      }

      @Override
      public long getEndX() {
         return endX;
      }

      @Override
      public long getEndZ() {
         return endZ;
      }
   }

   public static MapState empty() {
      return new MapState(new HashMap<>());
   }
}
