package com.busted_moments.core.http.models.wynncraft.guild.banner;

import com.busted_moments.core.http.api.guild.Guild;
import com.busted_moments.core.json.BaseModel;
import com.busted_moments.core.json.Json;

public class BaseLayer extends BaseModel implements Guild.Banner.Layer {
   @Key("colour") private Guild.Banner.Color color;
   @Key private Guild.Banner.Pattern pattern;

   @Override
   protected void onPreLoad(Json json) {
      if (json.has("color"))
         json.set("colour", json.getString("color"));
   }

   @Override
   public Guild.Banner.Color color() {
      return color;
   }

   @Override
   public Guild.Banner.Pattern pattern() {
      return pattern;
   }
}
