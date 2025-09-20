package com.busted_moments.mixin.extensions;

import com.wynntils.models.containers.event.ValuableFoundEvent;
import net.neoforged.bus.api.ICancellableEvent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ValuableFoundEvent.class, remap = false)
public abstract class ValuableFoundEventExtension implements ICancellableEvent {

}
