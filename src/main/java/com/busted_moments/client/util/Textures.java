package com.busted_moments.client.util;

import com.busted_moments.core.render.TextureInfo;

public interface Textures {
   interface TerritoryMenu {
      TextureInfo BACKGROUND = new TextureInfo("fuy", "textures/gui/territory_menu/background.png", 255, 190);
      TextureInfo FOREGROUND = new TextureInfo("fuy", "textures/gui/territory_menu/foreground.png", 226, 160);
      TextureInfo MASK = new TextureInfo("fuy", "textures/gui/territory_menu/mask.png", 226, 160);
      TextureInfo SCROLLBAR = new TextureInfo("fuy", "textures/gui/territory_menu/scrollbar.png", 9, 30);

   }
}
