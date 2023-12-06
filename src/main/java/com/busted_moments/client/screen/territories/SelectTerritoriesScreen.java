package com.busted_moments.client.screen.territories;

import com.busted_moments.client.models.territory.eco.TerritoryEco;
import com.busted_moments.client.models.territory.eco.TerritoryScanner;
import com.busted_moments.client.util.ContainerHelper;
import com.busted_moments.core.Promise;
import com.busted_moments.core.time.ChronoUnit;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static com.busted_moments.client.util.Textures.TerritoryMenu.BACKGROUND;

public class SelectTerritoriesScreen extends TerritoryScreen<SelectTerritoriesScreen.Scanner> {
   public static final Pattern SELECT_TERRITORIES_MENU = Pattern.compile("Select Territories");

   public SelectTerritoriesScreen(int id, boolean showProduction, boolean showPercents) {
      super(id, showProduction, false, showPercents);
   }

   @Override
   protected Scanner scanner(int container) {
      return new Scanner(container);
   }

   @Override
   protected Entry entry(TerritoryEco territory) {
      return new Entry(territory);
   }

   @Override
   protected Pattern title() {
      return SELECT_TERRITORIES_MENU;
   }

   @Override
   protected void build() {
      var handler = getClickHandler(0, true, false);

      item(0, (mouseX, mouseY, button, widget) -> {
         if (handler.accept(mouseX, mouseY, button, widget)) {
            if (!scanner.SCANNING) BUSY = true;

            scanner.SCANNING = false;

            Promise.sleep((long) (Services.Ping.getPing() * 1.2D), ChronoUnit.MILLISECONDS).thenAccept(v -> {
               if (ContainerHelper.isOpen(container()) && WAITING) {
                  BUSY = false;
                  WAITING = false;
               }
            });

            return true;
         } else return false;
      }).setScale(1.2F)
              .perform(item -> {
                 item.setX(2.75F + (this.width / 2F) - BACKGROUND.width() / 2F);
                 item.setY(((this.height / 2F) - BACKGROUND.height() / 2F) + 2);
              }).build();
   }

   public class Entry extends AbstractEntry {

      public Entry(TerritoryEco territory) {
         super(territory);
      }

      @Override
      protected void click() {
         scanner.select(getItemSupplier().get());
      }
   }

   public class Scanner extends TerritoryScanner {
      private final Set<String> TO_SELECT = new HashSet<>();

      public Scanner(int containerId) {
         super(containerId);
      }

      @Override
      protected boolean process(String territory, ItemStack stack, int slot) {
         if (TO_SELECT.contains(territory)) {
            if (click(slot, 0)) TO_SELECT.remove(territory);
            else Managers.TickScheduler.scheduleNextTick(() -> process(territory, stack, slot));

            return true;
         }

         return false;
      }

      public void select(ItemStack stack) {
         String territory = TerritoryEco.getTerritory(stack);

         if (!TO_SELECT.contains(territory)) {
            TO_SELECT.add(TerritoryEco.getTerritory(stack));
         } else TO_SELECT.remove(territory);

         if (getPages().size() == 1 || getContents().get(0).getDamageValue() == 20) {
            SCANNING = true;
            rescan();
         }
      }
   }
}
