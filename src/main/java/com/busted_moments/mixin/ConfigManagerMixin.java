package com.busted_moments.mixin;

import com.busted_moments.client.framework.artemis.config.LinkedConfig;
import com.busted_moments.client.framework.config.Config;
import com.wynntils.core.persisted.config.ConfigManager;
import net.essentuan.esl.iteration.Iterators;
import net.essentuan.esl.iteration.extensions.IteratorExtensionsKt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

@Mixin(value = ConfigManager.class, remap = false)
public abstract class ConfigManagerMixin {
   @Redirect(method = "saveConfig", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
   private Iterator<Object> saveConfig(List<Object> instance) {
      return IteratorExtensionsKt.filter(
              instance.iterator(),
              it -> !(it instanceof LinkedConfig<?>)
      );
   }

   @Inject(method = "saveConfig", at = @At("TAIL"))
   private void saveConfig(CallbackInfo ci) {
      Config.INSTANCE.write();
   }

   @Redirect(method = "loadConfigOptions", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
   private Iterator<Object> loadConfig(List<Object> instance) {
      return IteratorExtensionsKt.filter(
              instance.iterator(),
              it -> !(it instanceof LinkedConfig<?>)
      );
   }

   @Redirect(method = "saveDefaultConfig", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
   private Iterator<Object> saveDefaultConfig(List<Object> instance) {
      return IteratorExtensionsKt.filter(
              instance.iterator(),
              it -> !(it instanceof LinkedConfig<?>)
      );
   }
}
