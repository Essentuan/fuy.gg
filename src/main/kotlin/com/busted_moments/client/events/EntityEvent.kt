package com.busted_moments.client.events

import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.events.post
import com.wynntils.mc.event.AddEntityEvent
import com.wynntils.mc.event.TickEvent
import net.essentuan.esl.collections.maps.expireAfter
import net.essentuan.esl.time.duration.ms
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.neoforged.bus.api.Event
import java.util.UUID
import kotlin.collections.set

abstract class EntityEvent(
    val entity: Entity
) : Event() {
    val xa: Double
    val ya: Double
    val za: Double

    init {
        val vec3 = entity.getDeltaMovement()

        this.xa = (Mth.clamp(vec3.x, -3.9, 3.9) * 8000.0)
        this.ya = (Mth.clamp(vec3.y, -3.9, 3.9) * 8000.0)
        this.za = (Mth.clamp(vec3.z, -3.9, 3.9) * 8000.0)
    }

    class Spawn(entity: Entity) : EntityEvent(entity) {
        companion object {
            private val pending = mutableMapOf<UUID, Entity>().expireAfter(50.ms)

            @Subscribe
            private fun AddEntityEvent.on() {
                pending[uuid] = entity
            }

            @Subscribe
            private fun SetData.on() {
                Spawn(pending.remove(entity.uuid) ?: return).post()
            }

            @Subscribe
            private fun Remove.on() {
                pending.remove(entity.uuid)
            }

            @Subscribe
            private fun TickEvent.on() {
                pending.cleanse()
            }
        }
    }

    class Remove(entity: Entity, val removalReason: Entity.RemovalReason) : EntityEvent(entity)
    class SetData(entity: Entity) : EntityEvent(entity)
}