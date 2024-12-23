package com.busted_moments.client.models.party

import com.busted_moments.buster.api.Party
import com.busted_moments.buster.api.PlayerType
import com.busted_moments.client.Client
import com.busted_moments.client.Patterns
import com.busted_moments.client.events.EntityEvent
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.events.post
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.text.Text.matches
import com.busted_moments.client.framework.util.mc
import com.busted_moments.client.framework.util.self
import com.busted_moments.client.models.party.events.PartyEvent
import com.wynntils.core.components.Models
import com.wynntils.core.text.StyledText
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent
import com.wynntils.models.worlds.event.WorldStateEvent
import com.wynntils.models.worlds.type.WorldState
import com.wynntils.utils.mc.StyledTextUtils
import net.essentuan.esl.scheduling.api.schedule
import net.essentuan.esl.time.duration.ms
import net.minecraft.network.chat.HoverEvent
import net.minecraft.world.entity.player.Player
import java.util.UUID

typealias AParty = com.wynntils.models.players.PartyModel

private fun Text.Builder.append(member: Party.Member) {
    +member.name

    if (member.hasUUID) {
        +"["
        +member.uuid.toString()
        "]"
    }
}

object PartyModel : Party {
    private var _leader: Member? = null

    val leader: Party.Member?
        get() = _leader

    private val members = mutableMapOf<String, Member>()
    private val uuids = mutableMapOf<UUID, Member>()

    override val size: Int
        get() = members.size

    @Subscribe(receiveCanceled = true)
    private fun ChatMessageReceivedEvent.on() {
        originalStyledText matches {
            unwrapped {
                Patterns.PARTY_LIST_ALL {
                    val leader: String?
                    val party: Set<String>

                    Text.strip(group("players")).trim().split(Patterns.PLAYER_LIST_DELIMITER).also {
                        leader = it.getOrNull(0)
                        party = it.toSet()
                    }

                    val partyBefore = copy()

                    party.plus(
                        members.values.asSequence()
                            .map {
                                it.name
                            }).distinct().forEach { name ->
                        val before = members[name]
                        val after = name in party

                        when {
                            before != null && after -> return@forEach

                            //Member Left
                            before != null -> {
                                PartyEvent.MemberLeft(before, partyBefore).post()
                                uuids.remove(members.remove(before.name)!!.uuid)
                            }

                            //Member Joined
                            else -> {
                                val member = Member(name)

                                PartyEvent.MemberJoined(member, partyBefore).post()

                                members[name] = member
                            }
                        }
                    }

                    if (_leader?.name != leader) {
                        val member = members[leader]

                        if (member == null)
                            Client.error("Party leader was null. How did this happen?")
                        else {
                            PartyEvent.MemberPromoted(member, this@PartyModel).post()

                            _leader = member
                        }
                    }

                    PartyEvent.Refreshed(partyBefore, this@PartyModel).post()

                    findPlayers()

                    return
                }

                Patterns.PARTY_CREATE_SELF {
                    PartyEvent.Created(this@PartyModel).post()
                    _leader = Member(self.name)
                    _leader!!.uuid = self.profileId

                    return
                }

                Patterns.PARTY_JOIN_SELF {
                    Models.Party.requestData()

                    return
                }

                any(Patterns.PARTY_JOIN_OTHER, Patterns.PARTY_JOIN_OTHER_SWITCH) {
                    val member = Member(username(it, group("player")))
                    members[member.name] = member

                    PartyEvent.MemberJoined(member, this@PartyModel).post()

                    findPlayers()

                    return
                }

                Patterns.PARTY_PROMOTE_OTHER {
                    val player = members.computeIfAbsent(username(it, group("player")), ::Member)

                    PartyEvent.MemberPromoted(player, this@PartyModel).post()
                    _leader = player

                    findPlayers()

                    return
                }

                Patterns.PARTY_PROMOTE_SELF {
                    val player = uuids.computeIfAbsent(self.profileId) {
                        val member = Member(self.name)
                        member.uuid = self.profileId

                        members[self.name] = member

                        member
                    }

                    PartyEvent.MemberPromoted(player, this@PartyModel).post()

                    _leader = player

                    return
                }

                any(Patterns.PARTY_LEAVE_OTHER, Patterns.PARTY_LEAVE_KICK_OTHER) {
                    val member = members[username(it, group("player"))] ?: return

                    PartyEvent.MemberLeft(member, this@PartyModel).post()
                    return
                }


                any(
                    Patterns.PARTY_LIST_FAILED,
                    Patterns.PARTY_DISBAND_ALL,
                    Patterns.PARTY_LEAVE_SELF,
                    Patterns.PARTY_LEAVE_SELF_ALREADY_LEFT,
                    Patterns.PARTY_LEAVE_KICK,
                ) {
                    if (isNotEmpty()) {
                        PartyEvent.Leave(this@PartyModel).post()

                        members.clear()
                        uuids.clear()

                        _leader == null
                    }

                    return
                }
            }
        }
    }

    @Subscribe
    private fun WorldStateEvent.on() {
        if (newState == WorldState.WORLD)
            schedule {
                Models.Party.requestData()
            } after 300.ms
    }

    @Subscribe
    private fun EntityEvent.Spawn.on() {
        if (entity is Player)
            assignPlayer(entity)
    }

    private fun username(text: StyledText, default: String): String =
        text.asSequence()
            .map { it.partStyle.hoverEvent }
            .filter { it != null && it.action == HoverEvent.Action.SHOW_TEXT }
            .flatMap { StyledText.fromComponent(it.getValue(HoverEvent.Action.SHOW_TEXT)).split("\n").asSequence() }
            .map {
                val matcher = it.getMatcher(StyledTextUtils.NICKNAME_PATTERN)
                if (matcher.matches()) matcher.group("username") else null
            }
            .filterNotNull()
            .firstOrNull() ?: Text.strip(default).trim()

    private fun assignPlayer(player: Player) {
        val member = members[player.gameProfile.name] ?: return

        if (!member.hasUUID)
            member.uuid = player.gameProfile.id
    }


    private fun findPlayers() {
        for (player in mc.level?.players() ?: return)
            assignPlayer(player)
    }

    override fun get(name: String): Party.Member? =
        members[name]

    override fun get(uuid: UUID): Party.Member? =
        uuids[uuid]

    override fun containsAll(elements: Collection<Party.Member>): Boolean {
        for (member in elements)
            if (member !in this)
                return false

        return true
    }

    override fun isEmpty(): Boolean =
        members.isEmpty()

    override fun iterator(): Iterator<Party.Member> =
        members.values.iterator()

    private data class Member(override val name: String) : Party.Member {
        override var uuid: UUID = UUID(0L, 0L)
            set(value) {
                check(uuid == UUID(0L, 0L)) {
                    "$this already has a uuid!"
                }

                uuids[value] = this
                field = value
            }

        override fun equals(other: Any?): Boolean {
            return this === other || (other is PlayerType && name == other.name && uuid == other.uuid)
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + uuid.hashCode()
            return result
        }

        override fun toString(): String {
            return "PartyModel.Member(name='$name', uuid=$uuid)"
        }
    }
}