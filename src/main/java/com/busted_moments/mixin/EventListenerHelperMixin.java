package com.busted_moments.mixin;

import net.minecraftforge.eventbus.ListenerList;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventListenerHelper;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EventListenerHelper.class, remap = false)
public abstract class EventListenerHelperMixin {
   @Unique
   private static final Objenesis objenesis = new ObjenesisStd();

   @Inject(
           method = "computeListenerList",
           at = @At(
                   value = "INVOKE",
                   target = "Ljava/lang/Class;getConstructor([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;",
                   shift = At.Shift.BEFORE
           ),
           cancellable = true
   )
   private static void computeListenerList(
           Class<Event> eventClass,
           boolean fromInstanceCall,
           CallbackInfoReturnable<ListenerList> cir
   ) {
      cir.setReturnValue(objenesis.newInstance(eventClass).getListenerList());
   }
}
