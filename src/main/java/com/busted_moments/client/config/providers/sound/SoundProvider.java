package com.busted_moments.client.config.providers.sound;

import com.busted_moments.core.FuyExtension;
import com.busted_moments.core.config.Config;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SoundProvider implements Config.Dropdown.Provider<SoundEvent> {
   private static Map<String, SoundEvent> SOUNDS = null;

   @Override
   public Iterable<SoundEvent> getOptions() {
      return getSounds().values();
   }

   @Override
   public @Nullable SoundEvent get(String string) throws Throwable {
      return getSounds().get(string);
   }

   @Override
   public Component getName(SoundEvent value) {
      return Component.literal(value.getLocation().toString());
   }

   private static Map<String, SoundEvent> getSounds() {
      if (SOUNDS == null) {
         SOUNDS = Stream.concat(FabricLoader.getInstance().getEntrypointContainers("fuy_gg", FuyExtension.class).stream()
                         .flatMap(extension -> Stream.of(extension.getEntrypoint().getSounds()))
                         .map(location -> SoundEvent.createVariableRangeEvent(new ResourceLocation(location))), BuiltInRegistries.SOUND_EVENT.stream())
                 .collect(Collectors.toMap(event -> event.getLocation().toString(), event -> event));
      }

      return SOUNDS;
   }
}
