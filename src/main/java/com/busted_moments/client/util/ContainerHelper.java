package com.busted_moments.client.util;

import com.wynntils.handlers.container.type.ContainerContent;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.utils.wynn.ContainerUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class ContainerHelper {
   private static int ID = -1;
   private static Component TITLE = null;

   private static List<ItemStack> ITEMS = null;
   private static MenuType<?> MENU_TYPE = null;


   @SubscribeEvent(priority = EventPriority.HIGHEST)
   private static void onContainerOpen(MenuEvent.MenuOpenedEvent event) {
      ID = event.getContainerId();
      TITLE = event.getTitle();
      MENU_TYPE = event.getMenuType();
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   private static void onContainerSetContents(ContainerSetContentEvent.Pre event) {
      if (ID != event.getContainerId()) clear();
      else ITEMS = event.getItems();
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   private static void onClientCloseContainer(ContainerCloseEvent.Pre event) {
      clear();
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   private static void onServerCloseContainer(MenuEvent.MenuClosedEvent event) {
      if (ID == event.getContainerId()) clear();
   }

   private static void clear() {
      ID = -1;
      TITLE = null;
      ITEMS = null;
   }

   public static Optional<ContainerContent> getOpened() {
      if (!hasOpened()) return Optional.empty();

      return Optional.of(new ContainerContent(
              ITEMS,
              TITLE,
              MENU_TYPE,
              ID
      ));
   }

   public static boolean isOpen(int id) {
      return hasOpened() && getOpened().map(contents -> contents.containerId() == id).orElse(false);
   }

   public static boolean hasOpened() {
      return ID != -1 && ITEMS != null;
   }

   public static boolean Click(int slot, int button) {
      return Click(slot, button, ID);
   }

   public static boolean Click(int slot, int button, Pattern title) {
      if (!hasOpened() || !title.matcher(TITLE.getString()).matches()) return false;

      return Click(slot, button, ID);
   }

   public static boolean Click(int slot, int button, int container) {
      if (!hasOpened() || ID != container) return false;

      ContainerUtils.clickOnSlot(slot, container, button, ITEMS);

      return true;
   }
}
