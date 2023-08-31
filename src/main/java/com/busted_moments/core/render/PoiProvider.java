package com.busted_moments.core.render;

import com.wynntils.models.marker.type.MarkerInfo;
import com.wynntils.models.marker.type.MarkerProvider;
import com.wynntils.services.map.pois.MarkerPoi;

import java.util.stream.Stream;

record PoiProvider(Beacon.Provider original) implements MarkerProvider<MarkerPoi> {
   @Override
   public Stream<MarkerInfo> getMarkerInfos() {
      return Stream.empty();
   }

   @Override
   public Stream<MarkerPoi> getPois() {
      return original.getPois();
   }

   @Override
   public boolean isEnabled() {
      return original.isEnabled();
   }
}
