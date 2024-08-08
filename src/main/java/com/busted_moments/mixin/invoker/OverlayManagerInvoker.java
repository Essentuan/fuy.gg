package com.busted_moments.mixin.invoker;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayManager;
import com.wynntils.core.consumers.overlays.RenderState;
import com.wynntils.mc.event.RenderEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = OverlayManager.class, remap = false)
public interface OverlayManagerInvoker {
   @Invoker()
   void invokeRegisterOverlay(
           Overlay overlay,
           Feature parent,
           RenderEvent.ElementType elementType,
           RenderState renderAt,
           boolean enabledByDefault
   );
}
