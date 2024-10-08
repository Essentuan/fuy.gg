package com.busted_moments.client.models

import com.wynntils.core.components.Models
import com.wynntils.utils.mc.McUtils.mc
import com.wynntils.utils.mc.McUtils.player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.essentuan.esl.scheduling.annotations.Every
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

object GroundModel {
    private const val RADIUS = 32

    var height: Int = 63
        private set

    private val BlockState.isIgnored: Boolean
        get() {
            val key = block.descriptionId

            @Suppress("DEPRECATION")
            return when {
                isAir -> true
                block == Blocks.BARRIER -> true
                !liquid() && !isSolid -> true
                "leaves" in key || "log" in key -> true

                else -> false
            }
        }


    @Every(ms = 500.0)
    private suspend fun update() {
        if (!Models.WorldState.onWorld())
            return

        val player = player() ?: return
        val origin = player.blockPosition()

        if (origin.y < 0)
            return

        val level = mc().level ?: return

        val counts = ConcurrentHashMap<Int, AtomicInteger>()

        withContext(Dispatchers.IO) {
            for (x in -RADIUS..RADIUS) {
                for (z in -RADIUS..RADIUS) {
                    launch {
                        val start = BlockPos(origin.x + x, origin.y, origin.z + z)
                        val chunk = level.getChunk(start)

                        if (!chunk.getBlockState(start).isIgnored)
                            return@launch

                        for (y in start.y downTo 0) {
                            val state = chunk.getBlockState(BlockPos(start.x, y, start.z))

                            if (state.isIgnored)
                                continue

                            counts.getOrPut(y, ::AtomicInteger).incrementAndGet()
                            return@launch
                        }
                    }
                }
            }
        }

        height = counts.entries
            .asSequence()
            .sortedByDescending { (_, int) ->
                int.get()
            }.map { it.key }
            .firstOrNull() ?: return
    }
}