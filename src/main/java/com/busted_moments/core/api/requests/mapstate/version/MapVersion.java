package com.busted_moments.core.api.requests.mapstate.version;

import com.busted_moments.core.Promise;
import com.busted_moments.core.api.internal.GetRequest;
import com.busted_moments.core.api.internal.RateLimit;
import com.busted_moments.core.api.requests.mapstate.version.template.MapTemplate;
import com.busted_moments.core.json.Json;
import com.busted_moments.core.json.template.JsonTemplate;

import java.util.UUID;

public class MapVersion extends JsonTemplate {
   @Entry
   private int revision;
   @Entry
   private String tileSet;
   @Entry("template")
   private UUID templateUUID;

   private final Promise.Getter<Tiles> tiles = new Promise.Getter<>(
           () -> new Tiles.Request(tileSet).thenApply(optional -> optional.orElse(null))
   );
   private final Promise.Getter<MapTemplate> template = new Promise.Getter<>(
           () -> new MapTemplate.Request(templateUUID).thenApply(optional -> optional.orElse(null))
   );

   public Promise<Tiles> getTileSet() {
      return tiles.get();
   }

   public Promise<MapTemplate> getTemplate() {
      return template.get();
   }

   public static class Tiles extends JsonTemplate {
      @Entry("tileHash")
      String hash;
      @Entry("offsets.x")
      long offsetX;
      @Entry("offsets.y")
      long offsetY;
      @Entry
      int nativeZoom;

      public String getHash() {
         return hash;
      }

      public long getOffsetX() {
         return offsetX;
      }

      public long getOffsetY() {
         return offsetY;
      }

      public int getNativeZoom() {
         return nativeZoom;
      }

      @com.busted_moments.core.api.internal.Request.Definition(route = "https://thesimpleones.net/api/mapTiles/%s", ratelimit = RateLimit.NONE, cache_length = 15)
      public static class Request extends GetRequest<Tiles> {
         public Request(String hash) {
            super(hash);
         }

         @org.jetbrains.annotations.Nullable
         @Override
         protected Tiles get(Json json) {
            return json.wrap(Tiles::new);
         }
      }
   }


   @com.busted_moments.core.api.internal.Request.Definition(route = "https://thesimpleones.net/api/mapVersions/version%s", ratelimit = RateLimit.NONE, cache_length = 15)
   public static class Request extends GetRequest<MapVersion> {
      public Request(int version) {
         super(Integer.toString(version));
      }

      @org.jetbrains.annotations.Nullable
      @Override
      protected MapVersion get(Json json) {
         var version = json.wrap(MapVersion::new);
         if (version != null) {
            version.getTileSet();
            version.getTemplate();
         }

         return version;
      }
   }
}
