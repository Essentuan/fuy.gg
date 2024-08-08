package com.busted_moments.client.buster

import com.busted_moments.buster.api.Guild
import com.busted_moments.buster.api.GuildType
import com.busted_moments.buster.protocol.clientbound.ClientboundGuildListPacket
import com.busted_moments.buster.protocol.clientbound.ClientboundWorldListPacket
import com.busted_moments.client.framework.artemis.esl
import com.wynntils.core.components.Models
import com.wynntils.utils.colors.CommonColors
import com.wynntils.utils.colors.CustomColor
import net.essentuan.esl.color.Color
import net.essentuan.esl.iteration.Iterators
import java.util.UUID

private val NONE_UUID = UUID(0L, 0L)



object GuildList : Guild.List {
    private var guilds: Guild.List? = null

    override val size: Int
        get() = guilds?.size ?: 0

    override fun contains(element: GuildType): Boolean =
        guilds?.contains(element) == true

    override fun containsAll(elements: Collection<GuildType>): Boolean =
        guilds?.containsAll(elements) ?: elements.isEmpty()

    override fun get(uuid: UUID): GuildType? =
        guilds?.get(uuid)

    override fun get(type: GuildType): GuildType? =
        guilds?.get(type)

    override fun isEmpty(): Boolean =
        guilds?.isEmpty() ?: true

    override fun iterator(): Iterator<GuildType> =
        guilds?.iterator() ?: Iterators.empty()

    @Listener
    private fun Socket.on(packet: ClientboundGuildListPacket) {
        guilds = packet.wrap()
    }

    val NONE: GuildType = object : GuildType {
        override val name: String
            get() = "Nobody"
        override val tag: String
            get() = "NONE"
        override val uuid: UUID = UUID(0L, 0L)
    }
}

val GuildType.color: Color
    get() = if (tag != "NONE")
        Models.Guild.getColor(name).esl
    else
        CommonColors.WHITE.esl