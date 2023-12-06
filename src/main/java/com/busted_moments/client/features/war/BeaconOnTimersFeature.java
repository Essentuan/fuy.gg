package com.busted_moments.client.features.war;

import com.busted_moments.client.models.territory.TerritoryModel;
import com.busted_moments.client.models.war.timer.Timer;
import com.busted_moments.client.models.war.timer.TimerModel;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.http.requests.mapstate.Territory;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.render.Beacon;
import com.busted_moments.core.render.Renderer;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.marker.type.StaticLocationSupplier;
import com.wynntils.services.map.pois.MarkerPoi;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.TextShadow;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@Default(State.ENABLED)
@Config.Category("War")
@Feature.Definition(name = "Beacon on Timers", override = "Show beacon at timers")
public class BeaconOnTimersFeature extends Feature implements Beacon.Provider {
   private final List<Beacon> BEACONS = new ArrayList<>();
   private final List<MarkerPoi> POIS = new ArrayList<>();

   public BeaconOnTimersFeature() {
      Renderer.beacon(this);
   }

   @SubscribeEvent
   public void onTick(TickEvent event) {
      Territory.List territories = TerritoryModel.getTerritoryList();

      AtomicBoolean isFirst = new AtomicBoolean(true);

      BEACONS.clear();
      POIS.clear();

      TimerModel.getTimers()
              .stream()
              .filter(timer -> territories.contains(timer.getTerritory()))
              .sorted()
              .map(timer -> new Beacon(
                      timer.getTerritory(),
                      StyledText.fromString(timer.getTerritory()),
                      TextShadow.OUTLINE,
                      new StaticLocationSupplier(
                              Location.containing(territories.get(timer.getTerritory()).getLocation().getCenter())
                      ),
                      getTexture(timer, isFirst.get()),
                      getColor(timer, isFirst.getAndSet(false)),
                      CommonColors.WHITE,
                      CommonColors.WHITE
              )).peek(beacon -> POIS.add(beacon.toPoi()))
              .forEach(BEACONS::add);
   }

   @Override
   public Stream<Beacon> getBeacons() {
      return BEACONS.stream();
   }

   @Override
   public Stream<MarkerPoi> getPois() {
      return POIS.stream();
   }

   @Override
   public boolean isEnabled() {
      return super.isEnabled() && Models.WorldState.onWorld();
   }

   private static CustomColor getColor(Timer timer, boolean isFirst) {
      if (isFirst) return CommonColors.RED;
      else if (timer.isPersonal()) return new CustomColor(78, 245, 217);
      else return new CustomColor(23, 255, 144);
   }

   private static Texture getTexture(Timer timer, boolean isFirst) {
      if (isFirst) return Texture.DEFEND;
      else if (timer.isPersonal()) return Texture.DIAMOND;
      else return Texture.SLAY;
   }
}
