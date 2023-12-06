package com.busted_moments.core.http.requests.mapstate.version.template;

import com.busted_moments.core.http.AbstractRequest;
import com.busted_moments.core.http.GetRequest;
import com.busted_moments.core.http.RateLimit;
import com.busted_moments.core.http.requests.mapstate.Territory;
import com.busted_moments.core.json.BaseModel;
import com.busted_moments.core.json.Json;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class MapTemplate extends BaseModel implements Collection<TerritoryTemplate> {
   @Key("templateUUID")
   private UUID uuid;
   @Key
   private int version;

   @Key
   private Map<String, TerritoryTemplate> territoryList;

   public UUID getUuid() {
      return uuid;
   }

   public int getVersion() {
      return version;
   }

   public TerritoryTemplate get(String territory) {
      return territoryList.get(territory);
   }

   public TerritoryTemplate get(Territory territory) {
      return get(territory.getName());
   }

   public boolean contains(String territory) {
      return territoryList.containsKey(territory);
   }

   public boolean contains(Territory territory) {
      return contains(territory.getName());
   }

   @Override
   public boolean contains(Object o) {
      if (o instanceof String string) return contains(string);
      else if (o instanceof Territory territory) return contains(territory.getName());

      return false;
   }

   @Override
   public int size() {
      return territoryList.size();
   }

   @Override
   public boolean isEmpty() {
      return territoryList.isEmpty();
   }

   @NotNull
   @Override
   public Iterator<TerritoryTemplate> iterator() {
      return territoryList.values().iterator();
   }

   @NotNull
   @Override
   public Object @NotNull [] toArray() {
      return territoryList.values().toArray();
   }

   @NotNull
   @Override
   public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
      return territoryList.values().toArray(a);
   }

   @Override
   public boolean add(TerritoryTemplate territoryTemplate) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean remove(Object o) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean containsAll(@NotNull Collection<?> c) {
      return territoryList.values().containsAll(c);
   }

   @Override
   public boolean addAll(@NotNull Collection<? extends TerritoryTemplate> c) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean removeAll(@NotNull Collection<?> c) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean retainAll(@NotNull Collection<?> c) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void clear() {
      throw new UnsupportedOperationException();
   }

   @Override
   public BaseModel load(Json json) {
      super.load(json);

      territoryList.forEach((territory, template) -> {
         for (String c: template.getConnections()) get(c).getConnections().add(territory);
      });

      return this;
   }

   @AbstractRequest.Definition(route = "https://thesimpleones.net/api/mapTemplates/%s", ratelimit = RateLimit.NONE, cache_length = 15)
   public static class Request extends GetRequest<MapTemplate> {
      public Request(UUID uuid) {
         super(uuid);
      }

      @org.jetbrains.annotations.Nullable
      @Override
      protected MapTemplate get(Json json) {
         return json.wrap(MapTemplate::new);
      }
   }
}
