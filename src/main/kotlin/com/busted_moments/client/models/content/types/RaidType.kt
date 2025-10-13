package com.busted_moments.client.models.content.types

import com.busted_moments.buster.protocol.serverbound.ContentModifier
import com.busted_moments.client.Client
import com.busted_moments.client.Patterns
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.events.events
import com.busted_moments.client.framework.render.screen.elements.HoverElement
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.text.Text.matches
import com.busted_moments.client.framework.text.Text.send
import com.busted_moments.client.framework.text.Text.unwrap
import com.busted_moments.client.framework.text.get
import com.busted_moments.client.framework.text.getValue
import com.busted_moments.client.framework.text.text
import com.busted_moments.client.framework.util.self
import com.busted_moments.client.models.content.ContentTimer
import com.busted_moments.client.models.content.ContentType
import com.busted_moments.client.models.content.Stage
import com.busted_moments.client.models.content.stages.Stages
import com.busted_moments.client.models.content.triggers.Triggers
import com.wynntils.core.text.StyledText
import com.wynntils.core.text.StyledTextPart
import com.wynntils.handlers.chat.event.ChatMessageEvent
import com.wynntils.utils.mc.StyledTextUtils
import com.wynntils.utils.type.IterationDecision
import kotlinx.coroutines.DelicateCoroutinesApi
import net.essentuan.esl.coroutines.delay
import net.essentuan.esl.coroutines.launch
import net.essentuan.esl.coroutines.timeout
import net.essentuan.esl.future.AbstractFuture
import net.essentuan.esl.future.api.Future
import net.essentuan.esl.rx.Generator
import net.essentuan.esl.rx.Producer
import net.essentuan.esl.rx.publisher
import net.essentuan.esl.time.duration.seconds
import net.minecraft.network.chat.HoverEvent
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import java.util.concurrent.TimeoutException

enum class RaidType(
    private val pretty: String,
    spawn: Vec3,
    vararg stages: Stage.Builder,
) : ContentType, List<Stage.Builder> by stages.toList(), ContentTimer.Modifiers {
    NEST_OF_THE_GROOTSLANGS(
        "Nest of The Grootslangs",
        Vec3(-1977.0, 60.0, -5599.0),
        Stages.multi(
            Stages.entersSphere(
                "Pillar Havoc",
                Vec3(9700.5, 112.0, 3253.0),
                15.0
            ),
            Stages.entersSphere(
                "Slimey Disc",
                Vec3(9717.5, 103.0, 3628.5),
                15.0
            )
        ),
        Stages.multi(
            Stages.entersSphere(
                "Obelisk Construction",
                Vec3(9548.5, 138.0, 3486.5),
                15.0
            )
        ),
        Stages.multi(
            Stages.entersSphere(
                "Hammer Time!",
                Vec3(9904.5, 169.0, 3212.5),
                15.0
            ),
            Stages.entersSphere(
                "Grootslangs Gauntlet",
                Vec3(9941.5, 80.0, 3666.5),
                15.0
            )
        ),
        Stages.text
        {
            +"Boss".gold.underline
        },
        Stages.enters(
            "The Grootslang",
            Vec2(9211f, 3142f),
            Vec2(9268f, 3247f)
        )
    ),
    THE_NEXUS_OF_LIGHT(
        "Orphion's Nexus of Light",
        Vec3(-731.0, 100.0, -6405.0),
        Stages.generic(
            "The Lobby",
            Triggers.immediate()
        ),
        Stages.multi(
            Stages.entersSphere(
                "Parasite Interference",
                Vec3(11309.5, 75.0, 2125.5),
                15.0
            )
        ),
        Stages.multi(
            Stages.entersSphere(
                "Crystalline Decay",
                Vec3(11684.5, 91.0, 1472.5),
                15.0
            ),
            Stages.entersSphere(
                "Nexus' Grand Hall",
                Vec3(11044.5, 96.0, 1311.5),
                15.0
            )
        ),
        Stages.multi(
            Stages.entersSphere(
                "Tower Purification",
                Vec3(11312.5, 196.0, 1585.5),
                15.0
            ),
            Stages.entersSphere(
                "Overseers Maze",
                Vec3(11815.5, 67.0, 2442.5),
                15.0
            )
        ),
        Stages.text
        {
            +"Boss".gold.underline
        },
        Stages.enters(
            "Orphion",
            Vec2(10687f, 2439f),
            Vec2(10592f, 2543f)
        )
    ),
    THE_CANYON_COLOSSUS(
        "The Canyon Colossus",
        Vec3(665.0, 49.0, -4448.0),
        Stages.multi(
            Stages.entersSphere(
                "Colossal Solidification",
                Vec3(11662.5, 149.0, 3045.5),
                15.0
            ),
            Stages.entersSphere(
                "Defense Systems",
                Vec3(11796.5, 155.0, 3437.5),
                15.0
            )
        ),
        Stages.multi(
            Stages.entersSphere(
                "Canyon Labyrinth",
                Vec3(11015.5, 48.0, 3384.5),
                15.0
            ),
            Stages.entersSphere(
                "Canyon Guides",
                Vec3(11514.0, 54.0, 3118.0),
                15.0
            )
        ),
        Stages.multi(
            Stages.entersSphere(
                "Sealing Ritual",
                Vec3(10748.5, 139.0, 183.5),
                15.0
            )
        ),
        Stages.text
        {
            +"Boss".gold.underline
        },
        Stages.enters(
            "The Colossus",
            Vec2(11752f, 4365f),
            Vec2(11716f, 4388f)
        )
    ),
    THE_NAMELESS_ANOMALY(
        "The Nameless Anomaly",
        Vec3(1120.0, 85.0, -853.0),
        Stages.multi(
            Stages.entersSphere(
                "Flooding Canyon",
                Vec3(29470.0, 6.0, -26773.0),
                15.0
            ),
            Stages.entersSphere(
                "Sunken Grotto",
                Vec3(29536.0, 7.0, -26686.0),
                15.0
            ),
        ),
        Stages.multi(
            Stages.entersSphere(
                "Nameless Cave",
                Vec3(-12717.0, 77.0, 8374.0),
                15.0
            ),
            Stages.entersSphere(
                "Weeping Soulroot",
                Vec3(29399.0, 7.0, -26915.0),
                15.0
            )
        ),
        Stages.multi(
            Stages.entersSphere(
                "Blueshift Wilds",
                Vec3(29523.0, 7.0, -26878.0),
                15.0
            ),
            Stages.entersSphere(
                "Twisted Jungle",
                Vec3(29456.0, 7.0, -26987.0),
                15.0
            )
        ),
        Stages.text
        {
            +"Boss".gold.underline
        },
        Stages.enters(
            "???",
            Vec2(27824f, -21294f),
            Vec2(27967f, -21152f)
        )
    );

    private val start =
        Triggers.title(pretty).build {
            ContentTimer(
                this,
                this,
                Triggers.title(
                    "Raid Completed!"
                ),
                listOf(
                    Triggers.title("Raid Failed!"),
                    Triggers.entersSphere(spawn, 25.0)
                ),
                this
            ).start()
        }

    override val id: String
        get() = name

    override fun invoke(): Future<Set<ContentModifier>> =
        Modifiers(this)

    override fun print(): String =
        pretty

    @OptIn(DelicateCoroutinesApi::class)
    private class Modifiers(
        val type: RaidType,
    ) : AbstractFuture<Set<ContentModifier>>() {
        init {
            events.register()

            handlers.add(launch {
                delay(20.seconds)

                Client.info("Raid expired")

                complete(emptySet())
            })

            push { events.unregister() }
        }

        @Subscribe
        private fun ChatMessageEvent.Match.on() {
            message.unwrap().iterate { part, out ->
                val hoverEvent = part.partStyle.hoverEvent ?: return@iterate IterationDecision.CONTINUE

                if (hoverEvent.action != HoverEvent.Action.SHOW_TEXT)
                    return@iterate IterationDecision.CONTINUE

                val matcher = StyledText.fromComponent(hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT))
                    .split("\n")
                    .asSequence()
                    .map { it.getMatcher(StyledTextUtils.NICKNAME_PATTERN) }
                    .firstOrNull { it.matches() } ?: return@iterate IterationDecision.CONTINUE

                out.clear()

                out += StyledTextPart(
                    part.text.replace(matcher["nick"]!!, matcher["username"]!!),
                    part.partStyle.style,
                    null,
                    null
                )

                IterationDecision.CONTINUE
            } matches {
                Patterns.GUILD_RAID {
                    if (group("players").split(Patterns.PLAYER_LIST_DELIMITER).none { it.trim() == self.name })
                        return

                    complete(setOf(ContentModifier.GUILD_RAID))
                }
            }
        }
    }


    companion object
}
