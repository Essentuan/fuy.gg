package com.busted_moments.client.features.war.territorymenu;

import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.client.util.ContainerHelper;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.heartbeat.Heartbeat;
import com.busted_moments.core.time.TimeUnit;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.wynntils.utils.mc.McUtils.mc;

@Config.Category("War")
@Default(State.ENABLED)
@Feature.Definition(name = "Territory Helper Menu")
public class TerritoryHelperMenuFeature extends Feature {
   @Value("Display production")
   static boolean production = true;

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void onMenuOpen(MenuEvent.MenuOpenedEvent event) {
      StyledText text = StyledText.fromComponent(event.getTitle());

      if (text.matches(TerritoryMenuScreen.TERRITORY_MENU_PATTERN)) {
         mc().setScreen(new TerritoryMenuScreen(event.getContainerId()));
         event.setCanceled(true);
      } else if (text.matches(TerritoryMenuScreen.GUILD_MANAGE_MENU) && OPEN_TERRITORY_MENU) event.setCanceled(true);
   }

   public static boolean OPEN_TERRITORY_MENU = false;

   @SubscribeEvent(priority = EventPriority.HIGH)
   public void onMenuSetContents(ContainerSetContentEvent.Pre event) {
      if (OPEN_TERRITORY_MENU && ContainerHelper.Click(14, 0, TerritoryMenuScreen.GUILD_MANAGE_MENU)) {
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
         }, 500, TimeUnit.MILLISECONDS);
      }
   }

   @SubscribeEvent
   public void onWorldSwap(WorldStateEvent event) {
      OPEN_TERRITORY_MENU = false;
   }
}
