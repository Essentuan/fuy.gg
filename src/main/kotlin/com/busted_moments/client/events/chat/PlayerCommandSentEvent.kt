package com.busted_moments.client.events.chat

import com.busted_moments.client.framework.events.Cancellable
import net.neoforged.bus.api.Event

class PlayerCommandSentEvent(val command: String) : Event(), Cancellable