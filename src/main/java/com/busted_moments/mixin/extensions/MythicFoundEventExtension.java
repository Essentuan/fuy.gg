package com.busted_moments.mixin.extensions;

import com.wynntils.models.containers.event.MythicFoundEvent;
import net.neoforged.bus.api.ICancellableEvent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = MythicFoundEvent.class, remap = false)
public abstract class MythicFoundEventExtension implements ICancellableEvent {

}
