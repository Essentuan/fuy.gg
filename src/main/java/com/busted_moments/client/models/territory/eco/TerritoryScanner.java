package com.busted_moments.client.models.territory.eco;

import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.client.util.ContainerHelper;
import com.busted_moments.core.events.EventListener;
import com.busted_moments.core.heartbeat.Scheduler;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.MenuEvent;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public abstract class TerritoryScanner implements Scheduler, EventListener, Closeable {
   private static final int PREVIOUS_PAGE = 9;
   private static final int NEXT_PAGE = 27;

   protected final int containerId;
   private List<ItemStack> contents = new ArrayList<>();
   private int page = 0;

   private final List<List<Entry>> pages = new CopyOnWriteArrayList<>();
   private Consumer<GuildEco> UPDATE_CONSUMER = e -> {
   };

   public TerritoryScanner(int containerId) {
      REGISTER_EVENTS();
         REGISTER_TASKS();
      this.containerId = containerId;
   }

   public boolean SCANNING = true;

   private Direction direction = Direction.DOWN;

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public void onMenuSetContents(ContainerSetContentEvent.Pre event) {
      if (event.getContainerId() != containerId || event.getItems() == null) return;

      contents = event.getItems();

      List<Entry> page = new ArrayList<>();

      for (int slot = 0; slot < Math.min(45, contents.size()); slot++) {
         var stack = contents.get(slot);
         if (!TerritoryEco.isTerritory(stack)) continue;

         page.add(new Entry(TerritoryEco.getTerritory(stack), stack, slot));
      }

      if (!hasPreviousPage() || this.page < 0) this.page = 0;

      if (pages.size() <= this.page) pages.add(page);
      else pages.set(this.page, page);
      UPDATE_CONSUMER.accept(new GuildEco(
              pages.stream()
                      .flatMap(List::stream)
                      .map(Entry::stack)
                      .toList()
      ));


      for (Entry entry : page) {
         if (process(
                 entry.territory(),
                 entry.stack(),
                 entry.slot()
         )) return;
      }

      if (!SCANNING) return;

      if (direction == Direction.DOWN) {
         if (!hasNextPage()) {
            direction = Direction.UP;
            if (pages.size() > this.page + 1) pages.subList(this.page + 1, pages.size()).clear();

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
      close();
   }

   public List<List<Entry>> getPages() {
      return pages;
   }

   protected abstract boolean process(String territory, ItemStack stack, int slot);

   public void rescan() {
      onMenuSetContents(new ContainerSetContentEvent.Pre(
              contents,
              null,
              containerId,
              0
      ));
   }

   private void nextPage() {
      ContainerHelper.Click(
              NEXT_PAGE,
              0,
              containerId
      );

      page++;
   }

   protected boolean hasNextPage() {
      var next = contents.get(NEXT_PAGE);

      return !next.isEmpty() && ChatUtil.strip(next.getDisplayName().getString()).contains("Next Page");
   }

   private void previousPage() {
      ContainerHelper.Click(
              PREVIOUS_PAGE,
              0,
              containerId
      );

      page--;
   }

   protected boolean hasPreviousPage() {
      var previous = contents.get(PREVIOUS_PAGE);

      return !previous.isEmpty() && ChatUtil.strip(previous.getDisplayName().getString()).contains("Previous Page");
   }

   public List<ItemStack> getContents() {
      return contents;
   }

   public void onUpdate(Consumer<GuildEco> consumer) {
      this.UPDATE_CONSUMER = consumer;
   }

   @Override
   public void close() {
      SCANNING = false;
      UNREGISTER_EVENTS();
      REGISTER_TASKS();
   }

   public record Entry(String territory, ItemStack stack, int slot) {}
}