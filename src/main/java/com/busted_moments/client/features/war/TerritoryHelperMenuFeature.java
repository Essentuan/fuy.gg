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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.busted_moments.client.models.territory.eco.Patterns.GUILD_MANAGE_MENU;
import static com.busted_moments.client.screen.territories.ManageTerritoriesScreen.TERRITORY_MENU_PATTERN;
import static com.busted_moments.client.screen.territories.SelectTerritoriesScreen.SELECT_TERRITORIES_MENU;
import static com.wynntils.utils.mc.McUtils.mc;

@Config.Category("War")
@Default(State.ENABLED)
@Feature.Definition(name = "Territory Helper Menu")
public class TerritoryHelperMenuFeature extends Feature {
   @Value("Display production")
   static boolean production = true;

   @Value("Display usage percents")
   static boolean percents = false;

   @Value("Replace loadouts menu")
   static boolean replaceLoadouts = true;

   @Value("Hide ignored territories")
   @Tooltip({
           "Will hide ignored territories",
           "",
           "Does not apply to the loadouts menu"
   })
   private static boolean hideIgnoredTerritories = false;

   @Value("Ignore cut off resources")
   private static boolean ignoreCutOffResources = true;

   @Value("Ignore resources from blacklist")
   @Tooltip("When enabled, will ignore resources from territories on the blacklist")
   private static boolean ignoreBlacklistedResources = true;

   @Array("Blacklist")
   private static List<String> blacklist = List.of(
           "Light Forest West Upper",
           "Light Forest West Mid",
           "Light Forest East Lower",
           "Light Forest East Mid",
           "Light Forest Canyon",
           "Aldorei Valley South Entrance",
           "Aldorei's North Exit",
           "Cinfras County Lower",
           "Path To The Arch,Ghostly Path",
           "Aldorei's Arch",
           "Burning Farm",
           "Heavenly Ingress",
           "Primal Fen",
           "Luminous Plateau",
           "Field of Life",
           "Path to Light",
           "Otherwordly Monolith",
           "Azure Frontier",
           "Nexus of Light",
           "Jungle Lake",
           "Herb Cave",
           "Great Bridge Jungle",
           "Jungle Lower",
           "Jungle Mid",
           "Jungle Upper",
           "Dernel Jungle Mid",
           "Dernel Jungle Lower",
           "Dernel Jungle Upper"
   );

   public static boolean hideIgnoredTerritories() {
      return hideIgnoredTerritories;
   }

   public static boolean ignoreCutOffResources() {
      return ignoreCutOffResources;
   }

   public static boolean ignoreBlacklistedTerritories() {
      return ignoreBlacklistedResources;
   }

   public static Set<String> blacklist() {
      return new HashSet<>(blacklist);
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void onMenuOpen(MenuEvent.MenuOpenedEvent event) {
      StyledText text = StyledText.fromComponent(event.getTitle());

      if (text.matches(TERRITORY_MENU_PATTERN)) {
         var screen = new ManageTerritoriesScreen(event.getContainerId(), production, percents);

         mc().setScreen(screen);

         if (!NO_RESET)
            screen.reset();

         NO_RESET = false;

         event.setCanceled(true);
      } else if (replaceLoadouts && text.matches(SELECT_TERRITORIES_MENU)) {
         mc().setScreen(new SelectTerritoriesScreen(event.getContainerId(), production, percents));
         event.setCanceled(true);
      } else if (text.matches(GUILD_MANAGE_MENU) && OPEN_TERRITORY_MENU) event.setCanceled(true);
   }

   public static boolean OPEN_TERRITORY_MENU = false;
   private static boolean NO_RESET = false;

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
         NO_RESET = true;

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
