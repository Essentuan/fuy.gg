package com.busted_moments.client.models.content.triggers

import com.busted_moments.client.models.content.Trigger

object NothingTrigger : Trigger, Trigger.Builder {
    override fun close() =
        Unit

    override fun build(handler: () -> Unit): Trigger =
        this
}