package com.busted_moments.client.models.content.triggers

import com.busted_moments.client.models.content.Trigger

class CountingTrigger(
    builder: Trigger.Builder,
    private val max: Int,
    private val handler: () -> Unit,
) : Trigger {
    private val trigger = builder.build(this::increment)
    private var count: Int = 0

    private fun increment() {
        count++

        if (count >= max) {
            handler()
            close()
        }
    }

    override fun close() {
        trigger.close()
    }
}