package com.busted_moments.client.models.content.triggers

import com.busted_moments.client.events.EntityEvent
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.events.events
import com.wynntils.core.text.type.StyleType
import com.busted_moments.client.framework.text.Text
import net.minecraft.world.entity.EntityType

class EntitySpawnTrigger(
    predicate: Any,
    style: StyleType,
    val type: EntityType<*>?,
    handler: () -> Unit
) : TextTrigger(predicate, style, handler) {
    init {
        events.register()
    }

    @Subscribe
    private fun EntityEvent.Spawn.on() {
        if (type != null && type != entity.type)
            return

        test(Text(entity.customName ?: return))
    }

    override fun close() =
        events.unregister()
}
