package com.busted_moments.client.models.territory.eco;

import com.busted_moments.client.models.territory.eco.TerritoryEco;
import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.events.EventListener;
import com.wynntils.core.components.Managers;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.utils.wynn.ContainerUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class TerritoryScanner implements EventListener, Closeable {
   private static final int PREVIOUS_PAGE = 9;
   private static final int NEXT_PAGE = 27;

   private final int containerId;
   private List<ItemStack> contents = new ArrayList<>();
   private int page = 0;

   private final List<List<ItemStack>> territories = new CopyOnWriteArrayList<>();
   private Consumer<GuildEco> UPDATE_CONSUMER = e -> {};

   public TerritoryScanner(int containerId) {
      REGISTER_EVENTS();
      this.containerId = containerId;
   }

   private Direction direction = Direction.DOWN;
   private String SELECTING_TERRITORY = null;

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public void onMenuSetContents(ContainerSetContentEvent.Pre event) {
      if (event.getContainerId() != containerId || event.getItems() == null) return;
      contents = event.getItems();

      List<ItemStack> page = new ArrayList<>();

      for (int slot = 0; slot < Math.min(45, contents.size()); slot++) {
         var stack = contents.get(slot);
         if (!TerritoryEco.isTerritory(stack)) continue;

         page.add(stack);

         if (SELECTING_TERRITORY != null && SELECTING_TERRITORY.equals(TerritoryEco.getTerritory(stack)))
            ContainerUtils.clickOnSlot(
                    slot,
                    containerId,
                    0,
                    contents
            );
      }

      if (!hasPreviousPage()) this.page = 0;

      if (territories.size() <= this.page) territories.add(page);
      else territories.set(this.page, page);
      UPDATE_CONSUMER.accept(new GuildEco(
              territories.stream()
              .flatMap(List::stream)
              .toList()
      ));

      if (!hasNextPage() && !hasPreviousPage()) Managers.TickScheduler.scheduleLater(() -> onMenuSetContents(event), 10);

      if (direction == Direction.DOWN) {
         if (!hasNextPage()) {
            direction = Direction.UP;
            if (territories.size() > this.page + 1) territories.subList(this.page + 1, territories.size()).clear();

            previousPage();
         } else nextPage();
      } else {
         if (!hasPreviousPage()) {
            direction = Direction.DOWN;

            nextPage();
         } else previousPage();
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public void onMenuOpen(MenuEvent.MenuOpenedEvent event) {
      UNREGISTER_EVENTS();
   }

   private void nextPage() {
      ContainerUtils.clickOnSlot(
              NEXT_PAGE,
              containerId,
              0,
              contents
      );

      page++;
   }

   private boolean hasNextPage() {
      var next = contents.get(NEXT_PAGE);

      return !next.isEmpty() && ChatUtil.strip(next.getDisplayName().getString()).contains("Next Page");
   }

   private void previousPage() {
      ContainerUtils.clickOnSlot(
              PREVIOUS_PAGE,
              containerId,
              0,
              contents
      );

      page--;
   }

   private boolean hasPreviousPage() {
      var previous = contents.get(PREVIOUS_PAGE);

      return !previous.isEmpty() && ChatUtil.strip(previous.getDisplayName().getString()).contains("Previous Page");
   }

   public List<ItemStack> getContents() {
      return contents;
   }

   public void onUpdate(Consumer<GuildEco> consumer) {
      this.UPDATE_CONSUMER = consumer;
   }

   public void select(ItemStack stack) {
      SELECTING_TERRITORY = TerritoryEco.getTerritory(stack);

      if (territories.size() == 1) {
         onMenuSetContents(new ContainerSetContentEvent.Pre(
                 contents,
                 null,
                 containerId,
                 0
         ));
      }
   }

   @Override
   public void close() {
      UNREGISTER_EVENTS();
   }
}