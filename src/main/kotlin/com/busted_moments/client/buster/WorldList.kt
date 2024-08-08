package com.busted_moments.client.buster

import com.busted_moments.buster.api.PlayerType
import com.busted_moments.buster.api.World
import com.busted_moments.buster.protocol.clientbound.ClientboundWorldListPacket
import net.essentuan.esl.iteration.Iterators
import java.util.UUID

object WorldList : World.List {
    private var worlds: World.List? = null

    override val size: Int
        get() = worlds?.size ?: 0

    override fun contains(element: World): Boolean =
        worlds?.contains(element) == true

    override fun containsAll(elements: Collection<World>): Boolean =
        worlds?.containsAll(elements) ?: elements.isEmpty()

    override fun get(world: String): World? =
        worlds?.get(world)

    override fun get(world: World): World? =
        worlds?.get(world)

    override fun get(uuid: UUID): World? =
        worlds?.get(uuid)

    override fun get(player: PlayerType): World? =
        worlds?.get(player)

    override fun isEmpty(): Boolean =
        worlds?.isEmpty() ?: true

    override fun iterator(): Iterator<World> =
        worlds?.iterator() ?: Iterators.empty()

    @Listener
    private fun Socket.on(packet: ClientboundWorldListPacket) {
        worlds = packet.wrap()
    }

    val PlayerType.world: World?
        get() = WorldList[this]
}