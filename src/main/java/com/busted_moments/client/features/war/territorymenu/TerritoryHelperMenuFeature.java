package com.busted_moments.client.features.war.territorymenu;

import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.wynn.ContainerUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.wynntils.utils.mc.McUtils.mc;

@Config.Category("War")
@Default(State.ENABLED)
@Feature.Definition(name = "Territory Helper Menu")
public class TerritoryHelperMenuFeature extends Feature {
   static int GUILD_MENU_ID = -1;

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void onMenuOpen(MenuEvent.MenuOpenedEvent event) {
      StyledText text = StyledText.fromComponent(event.getTitle());

      if (text.matches(TerritoryMenuScreen.TERRITORY_MENU_PATTERN)) {
         mc().setScreen(new TerritoryMenuScreen(event.getContainerId()));
         event.setCanceled(true);
      } else if (text.matches(TerritoryMenuScreen.GUILD_MANAGE_MENU) && OPEN_TERRITORY_MENU) {
         GUILD_MENU_ID = event.getContainerId();
         event.setCanceled(true);
      } else GUILD_MENU_ID = -1;
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public void onMenuSetContents(ContainerSetContentEvent.Pre event) {
      if (event.getContainerId() == GUILD_MENU_ID && OPEN_TERRITORY_MENU) {
         ContainerUtils.clickOnSlot(14, GUILD_MENU_ID, 0, event.getItems());
         OPEN_TERRITORY_MENU = false;
      }
   }

   public static boolean OPEN_TERRITORY_MENU = false;

   @SubscribeEvent
   public void onClick(ContainerClickEvent event) {
      if (event.getSlotNum() != 9) return;

      ItemStack stack = event.getContainerMenu().slots.get(11).getItem();
      OPEN_TERRITORY_MENU = stack.getItem() == Items.DISPENSER && ChatUtil.strip(stack.getDisplayName()).equals("[Guild Tower]");
   }

   @SubscribeEvent
   public void onWorldSwap(WorldStateEvent event) {
      OPEN_TERRITORY_MENU = false;
      GUILD_MENU_ID = -1;
   }
}
