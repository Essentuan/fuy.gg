package com.busted_moments.client.models.party.events

import com.busted_moments.buster.api.Party
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.text.Text.send
import net.essentuan.esl.iteration.extensions.iterate
import net.neoforged.bus.api.Event

abstract class PartyEvent(party: Party) : Event() {
    val before: Party = party.copy()

    class Created(before: Party) : PartyEvent(before)

    class Join(before: Party) : PartyEvent(before) {
        companion object {
            @Subscribe
            private fun Join.on() {
                Text {
                    +"You have joined a party!"
                }.send()
            }
        }
    }

    class MemberJoined(val member: Party.Member, before: Party) : PartyEvent(before)

    class MemberPromoted(val member: Party.Member, before: Party) : PartyEvent(before)

    class MemberLeft(val member: Party.Member, before: Party) : PartyEvent(before)

    class Refreshed(before: Party, val after: Party) : PartyEvent(before)

    class Leave(before: Party) : PartyEvent(before)
}