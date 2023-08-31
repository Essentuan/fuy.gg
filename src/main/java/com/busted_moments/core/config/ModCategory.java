package com.busted_moments.core.config;

import com.busted_moments.core.config.entry.ConfigEntry;
import com.busted_moments.core.config.entry.HiddenEntry;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.busted_moments.client.FuyMain.LOGGER;

public class ModCategory extends ArrayList<ConfigEntry<?>> implements Buildable<ConfigBuilder, ConfigCategory> {
   private final String title;

   public ModCategory(String title) {
      this.title = title;
   }

   @Override
   public String getKey() {
      return null;
   }

   @Override
   public ConfigCategory build(ConfigBuilder builder) {
      ConfigCategory category = builder.getOrCreateCategory(Component.literal(title));

      ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();

      List<Runnable> entries = new ArrayList<>();

      Map<String, SubCategoryBuilder> sections = new HashMap<>();

      Function<String, SubCategoryBuilder> SECTION_FACTORY = (title) -> {
         if (!sections.containsKey(title)) {
            var section = entryBuilder.startSubCategory(
                    Component.literal(title)
            );

            entries.add(() -> category.addEntry(section.build()));
            sections.put(title, section);

            return section;
         }

         return sections.get(title);
      };

      sort((entry1, entry2) -> {
         if (entry1.getSection() != null  && entry2.getSection() == null) return -1;
         else if (entry1.getSection() == null && entry2.getSection() != null) return 1;
         else return 0;
      });

      for (int i = 0; i < size(); i++) {
         ConfigEntry<?> entry = get(i);

         if (entry instanceof HiddenEntry) continue;

         try {
            AbstractConfigListEntry<?> built = entry.build(entryBuilder);

            if (entry.getSection() != null) SECTION_FACTORY.apply(entry.getSection()).add(built);
            else entries.add(() -> category.addEntry(built));
         } catch (Exception e) {
            LOGGER.info("Error while building {} in field {} ({})", entry.getClass(), entry.getField().getName(), entry.getParent().getClass(), e);
         }
      }

      entries.forEach(Runnable::run);

      return category;
   }
}
