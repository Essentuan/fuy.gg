package com.busted_moments.core.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import static com.busted_moments.client.Client.CONFIG;

public class ConfigScreen implements ModMenuApi {
   @Override
   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return parent -> CONFIG.build(parent).build();
   }
}
