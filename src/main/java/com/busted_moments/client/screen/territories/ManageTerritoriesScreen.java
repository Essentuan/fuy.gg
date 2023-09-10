package com.busted_moments.client.screen.territories;

import com.busted_moments.client.models.territory.eco.TerritoryEco;
import com.busted_moments.client.models.territory.eco.TerritoryScanner;
import com.busted_moments.client.models.war.WarModel;
import com.busted_moments.client.util.ContainerHelper;
import com.busted_moments.client.util.SoundUtil;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.util.regex.Pattern;

import static com.busted_moments.client.util.Textures.TerritoryMenu.BACKGROUND;

public class ManageTerritoriesScreen extends TerritoryScreen<ManageTerritoriesScreen.Scanner> {
   public static final Pattern TERRITORY_MENU_PATTERN = Pattern.compile("^(?<guild>.+): Territories$");
   public static final int LOADOUTS_SLOT = 36;

   public ManageTerritoriesScreen(int id, boolean showProduction, boolean showPercents) {
      super(id, showProduction, true, showPercents);
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
      return TERRITORY_MENU_PATTERN;
   }

   @Override
   protected void build() {
      item(LOADOUTS_SLOT, false, true)
              .setScale(1.05F)
              .perform(item -> {
                 item.setX(2.75F + (this.width / 2F) - BACKGROUND.width() / 2F);
                 item.setY(((this.height / 2F) + BACKGROUND.height() / 2F) - item.getHeight() - 3);
              }).build();
   }

   public class Entry extends AbstractEntry {
      public Entry(TerritoryEco territory) {
         super(territory);
      }

      @Override
      protected void click() {
         BUSY = true;
         scanner.select(getItemSupplier().get());
         SoundUtil.play(SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundSource.MASTER, 1, 1);
      }
   }

   public static class Scanner extends TerritoryScanner {
      private String TO_SELECT = null;

      public Scanner(int containerId) {
         super(containerId);
      }

      public void select(ItemStack stack) {
         TO_SELECT = TerritoryEco.getTerritory(stack);

         if (WarModel.current().isEmpty()) {
            close();
            McUtils.sendCommand("gu territory %s".formatted(TO_SELECT));
         }

         if (getPages().size() == 1) rescan();
      }

      @Override
      protected boolean process(String territory, ItemStack stack, int slot) {
         if (TO_SELECT != null && TO_SELECT.equals(territory)) {
            ContainerHelper.Click(slot, 0, containerId);
            return true;
         }

         return false;
      }
   }
}
