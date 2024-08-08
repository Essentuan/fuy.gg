package com.busted_moments.client.framework

import com.wynntils.core.components.Handlers

object Commands {
    fun execute(command: String) {
        Handlers.Command.sendCommandImmediately(command)
    }

    fun queue(command: String) {
        Handlers.Command.queueCommand(command)
    }
}