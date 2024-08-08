package com.busted_moments.client.buster

import com.busted_moments.buster.protocol.clientbound.ClientboundFFAListPacket

object FFAList : Set<String> {
    private var ffas: Set<String> = emptySet()

    override val size: Int
        get() = ffas.size

    override fun contains(element: String): Boolean =
        ffas.contains(element)

    override fun containsAll(elements: Collection<String>): Boolean =
        ffas.containsAll(elements)

    override fun isEmpty(): Boolean =
        ffas.isEmpty()

    override fun iterator(): Iterator<String> =
        ffas.iterator()

    @Listener
    private fun Socket.on(packet: ClientboundFFAListPacket) {
        ffas = packet.ffas
    }
}