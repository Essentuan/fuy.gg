package com.busted_moments.core.render;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.marker.type.LocationSupplier;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.services.map.pois.MarkerPoi;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.mc.type.PoiLocation;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.TextShadow;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public record Beacon(String name, @Nullable StyledText label, TextShadow style, LocationSupplier locationSupplier, Texture texture, CustomColor beaconColor, CustomColor textureColor, CustomColor textColor) {
   public Location location() {
      return locationSupplier.getLocation();
   }

   public MarkerPoi toPoi() {
      return new MarkerPoi(
              PoiLocation.fromLocation(location()),
              name,
              texture
      );
   }


   public interface Provider {
      Stream<Beacon> getBeacons();

      Stream<MarkerPoi> getPois();

      default boolean isEnabled() {
         return Models.WorldState.onWorld();
      }
   }
}