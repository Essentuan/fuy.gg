package com.busted_moments.client.framework

import net.minecraft.core.Position
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.levelgen.structure.BoundingBox
import kotlin.math.min
import kotlin.math.max

object Entities {
    fun Entity.isNear(pos: Position, radius: Double) =
        position().closerThan(pos, radius)
    
    fun Entity.isInside(po1: Position, po2: Position): Boolean {
        val pos = position()
        
        return pos.x in min(po1.x, po2.x)..max(po1.x, po2.x) &&
               pos.y in min(po1.y, po2.y)..max(po1.y, po2.y) &&
               pos.z in min(po1.z, po2.z)..max(po1.z, po2.z)
    }
}

val Position.x: Double
    get() = x()

val Position.y: Double
    get() = y()

val Position.z: Double
    get() = z()