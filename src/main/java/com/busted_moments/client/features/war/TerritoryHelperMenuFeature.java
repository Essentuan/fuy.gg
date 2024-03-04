package com.busted_moments.client.features.war;

import com.busted_moments.client.screen.territories.ManageTerritoriesScreen;
import com.busted_moments.client.screen.territories.SelectTerritoriesScreen;
import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.client.util.ContainerHelper;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.heartbeat.Heartbeat;
import com.busted_moments.core.time.ChronoUnit;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.busted_moments.client.models.territory.eco.Patterns.GUILD_MANAGE_MENU;
import static com.busted_moments.client.screen.territories.ManageTerritoriesScreen.TERRITORY_MENU_PATTERN;
import static com.busted_moments.client.screen.territories.SelectTerritoriesScreen.SELECT_TERRITORIES_MENU;
import static com.wynntils.utils.mc.McUtils.mc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Config.Category("War")
@Default(State.ENABLED)
@Feature.Definition(name = "Territory Helper Menu")
public class TerritoryHelperMenuFeature extends Feature {
   @Value("Ignore No Route Resources")
   static boolean ignoreNoRoute = false;

   @Value("Reset Filters On Menu Exit")
   static boolean resetFiltersOnMenuExit = false;

   @Value("Use blacklist")
	static boolean useBlacklist = false;

   public static boolean getResetFiltersOnMenuExit() {
      return resetFiltersOnMenuExit;
   }
   public static boolean getIgnoreNoRoute() {
      return ignoreNoRoute;
   }
   public static boolean getUseBlacklist() {
      return useBlacklist;
   }

   @Value("Ignore resources from")
   static String ignoredTerritories = "Light Forest West Upper,Light Forest West Mid,Light Forest East Lower,Light Forest East Mid,Light Forest Canyon,Aldorei Valley South Entrance,Aldorei's North Exit,Cinfras County Lower,Path To The Arch,Ghostly Path,Aldorei's Arch,Burning Farm,Heavenly Ingress,Primal Fen,Luminous Plateau,Field of Life,Path to Light,Otherwordly Monolith,Azure Frontier,Nexus of Light,Jungle Lake,Herb Cave,Great Bridge Jungle,Jungle Lower,Jungle Mid,Jungle Upper,Dernel Jungle Mid,Dernel Jungle Lower,Dernel Jungle Upper";

   public static Set<String> getIgnoredTerritories() {
      return new HashSet<>(Arrays.asList(ignoredTerritories.split(",")));
   }

   @Value("Display production")
   static boolean production = true;

   @Value("Display usage percents")
   static boolean percents = false;

   @Value("Replace loadouts menu")
   static boolean replaceLoadouts = true;

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void onMenuOpen(MenuEvent.MenuOpenedEvent event) {
      StyledText text = StyledText.fromComponent(event.getTitle());

      if (text.matches(TERRITORY_MENU_PATTERN)) {
         mc().setScreen(new ManageTerritoriesScreen(event.getContainerId(), production, percents));
         event.setCanceled(true);
      } else if (replaceLoadouts && text.matches(SELECT_TERRITORIES_MENU)) {
        mc().setScreen(new SelectTerritoriesScreen(event.getContainerId(), production, percents));
        event.setCanceled(true);
      } else if (text.matches(GUILD_MANAGE_MENU) && OPEN_TERRITORY_MENU) event.setCanceled(true);
   }

   public static boolean OPEN_TERRITORY_MENU = false;

   @SubscribeEvent(priority = EventPriority.HIGH)
   public void onMenuSetContents(ContainerSetContentEvent.Pre event) {
      if (OPEN_TERRITORY_MENU && ContainerHelper.Click(14, 0, GUILD_MANAGE_MENU)) {
         OPEN_TERRITORY_MENU = false;
      }
   }

   @SubscribeEvent
   public void onClick(ContainerClickEvent event) {
      if (event.getSlotNum() != 9) return;

      ItemStack stack = event.getContainerMenu().slots.get(11).getItem();
      OPEN_TERRITORY_MENU = stack.getItem() == Items.DISPENSER && ChatUtil.strip(stack.getDisplayName()).equals("[Guild Tower]");

      if (OPEN_TERRITORY_MENU) {
         Heartbeat.schedule(() -> {
            if (OPEN_TERRITORY_MENU) OPEN_TERRITORY_MENU = false;
         }, 500, ChronoUnit.MILLISECONDS);
      }
   }

   @SubscribeEvent
   public void onWorldSwap(WorldStateEvent event) {
      OPEN_TERRITORY_MENU = false;
   }
}
