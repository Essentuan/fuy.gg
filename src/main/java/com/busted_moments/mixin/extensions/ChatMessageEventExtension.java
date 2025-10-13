package com.busted_moments.mixin.extensions;

import com.busted_moments.client.events.chat.IChatMessageEvent;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ChatMessageEvent.class, remap = false)
public class ChatMessageEventExtension implements IChatMessageEvent {
    @Shadow
    @Final
    protected StyledText message;

    @Override
    public @NotNull StyledText getOriginalMessage() {
        return message;
    }
}
