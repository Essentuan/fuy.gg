package com.busted_moments.client.keybinds;

import com.busted_moments.client.features.keybinds.Keybind;
import com.busted_moments.client.features.war.TerritoryHelperMenuFeature;
import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.components.Handlers;

@Keybind.Definition(name = "Territory Menu", defaultKey = InputConstants.KEY_U)
public class TerritoryMenuKeybind extends Keybind {
   public TerritoryMenuKeybind() {
      super(TerritoryMenuKeybind.class);
   }

   @Override
   protected void onKeyDown() {
      TerritoryHelperMenuFeature.OPEN_TERRITORY_MENU = true;
      Handlers.Command.sendCommand("gu manage");
   }
}
