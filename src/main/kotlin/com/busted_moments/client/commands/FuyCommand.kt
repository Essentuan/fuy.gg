@file:Command("fuy")
@file:Alias("fy")

package com.busted_moments.client.commands

import com.busted_moments.buster.api.Guild
import com.busted_moments.buster.api.Profile
import com.busted_moments.buster.api.World
import com.busted_moments.buster.protocol.requests.GuildRequest
import com.busted_moments.buster.protocol.requests.MemberRequest
import com.busted_moments.buster.protocol.requests.PingRequest
import com.busted_moments.client.Client
import com.busted_moments.client.buster.BusterService.execute
import com.busted_moments.client.buster.WorldList.world
import com.busted_moments.client.commands.args.GuildArgument
import com.busted_moments.client.commands.args.war.WarFilter
import com.busted_moments.client.framework.wynntils.Ticks
import com.busted_moments.client.framework.config.Config
import com.busted_moments.client.framework.render.TextRenderer
import com.busted_moments.client.framework.text.FUY_PREFIX
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.text.Text.invoke
import com.busted_moments.client.framework.text.Text.send
import com.busted_moments.client.framework.util.Numbers.escapeCommas
import com.busted_moments.client.framework.util.Numbers.toCommaString
import com.busted_moments.client.inline
import com.busted_moments.client.models.territories.war.WarModel
import com.essentuan.acf.core.annotations.Alias
import com.essentuan.acf.core.annotations.Argument
import com.essentuan.acf.core.annotations.Command
import com.essentuan.acf.core.annotations.Subcommand
import com.mojang.brigadier.context.CommandContext
import com.wynntils.utils.mc.McUtils.mc
import net.essentuan.esl.collections.synchronized
import net.essentuan.esl.format.truncate
import net.essentuan.esl.future.api.Future
import net.essentuan.esl.iteration.extensions.iterate
import net.essentuan.esl.orElse
import net.essentuan.esl.string.extensions.isUUID
import net.essentuan.esl.time.TimeUnit
import net.essentuan.esl.time.duration.FormatFlag
import net.essentuan.esl.time.duration.days
import net.essentuan.esl.time.duration.hours
import net.essentuan.esl.time.duration.minutes
import net.essentuan.esl.time.extensions.timeSince
import net.essentuan.esl.unsafe
import net.minecraft.client.gui.components.ChatComponent
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.HoverEvent
import java.util.*
import kotlin.math.ceil


private const val WARS_PER_PAGE = 3

@Alias("cf")
@Subcommand("config")
private fun CommandContext<*>.config() {
    Ticks.schedule {
        mc().setScreen(Config.open(mc().screen).build())
    }
}

@Alias("g")
@Subcommand("guild")
private fun CommandContext<*>.guild(
    @Argument("Guild", type = GuildArgument::class) string: String
) {
    if (!string.isUUID() && !Guild.Names.isValid(string) && !Guild.Tags.isValid(string)) {
        FUY_PREFIX {
            +"$string is not a valid guild!".red
        }.send()

        return
    }

    FUY_PREFIX {
        +"Finding guild $string!".green
    }.send()

    Future {
        val guild = GuildRequest(string).execute()
        if (guild == null)
            FUY_PREFIX {
                +"$string is not a valid guild!".red
            }.send()
        else
            Text {
                val online = guild.asSequence()
                    .map { it to it.world }
                    .filter { (_, world) -> world != null }
                    .toList()

                line {
                    center {
                        +guild.name.aqua
                        +" [".darkAqua
                        +guild.tag.aqua
                        +"]".darkAqua
                        +" (".gray

                        +"${online.size}".aqua
                        +"/".gray
                        +"${guild.size}".aqua
                        +")".gray
                    }
                }

                line {
                    center {
                        +"Level ".gray
                        +"${guild.level}".aqua

                        +" | ".gray
                        +guild.xp.truncate().aqua
                        +"/".gray
                        +guild.required.truncate().aqua

                        +" (".gray
                        +"${guild.progress}%".aqua
                        +")".gray
                    }
                }

                center {
                    +"Owned".aqua
                    +" by ".gray

                    guild.owner.also { it.toText(this, it.world, true) }
                }

                val maxWidth = ChatComponent.getWidth(mc().options.chatWidth().get()) * 0.75

                online.asSequence()
                    .filter { (it, _) -> it.rank != Guild.Rank.OWNER }
                    .sortedByDescending { (it, _) -> it.rank!! }
                    .groupBy { (it, _) -> it.rank!! }
                    .forEach { (rank, members) ->
                        newLine()
                        newLine()

                        center {
                            +rank.print().aqua

                            if (rank != Guild.Rank.RECRUIT) {
                                +" (".gray
                                +"\u2605".repeat(rank.stars).aqua
                                +")".gray
                            }
                        }

                        var i = 0
                        while (i < members.size) {
                            newLine()
                            var width = 0f

                            center {
                                while (i < members.size) {
                                    val (next, world) = members[i]
                                    val member = Text { next.toText(this, world) }

                                    val memberWidth = TextRenderer.split(member).width

                                    if (width + memberWidth > maxWidth)
                                        break

                                    if (width != 0f)
                                        +", ".gray

                                    +member

                                    width += memberWidth
                                    i++
                                }
                            }
                        }
                    }
            }.send()
    }.except {
        FUY_PREFIX {
            +"Failed to fetch guild $string!".red
        }.send()

        Client.error("Error in /fuy g!", it)
    }
}

@Alias("om")
@Subcommand("onlinemembers")
private fun CommandContext<*>.onlinemembers(
    @Argument("Guild", type = GuildArgument::class) string: String
) {
    if (!string.isUUID() && !Guild.Names.isValid(string) && !Guild.Tags.isValid(string)) {
        FUY_PREFIX {
            +"$string is not a valid guild!".red
        }.send()

        return
    }

    FUY_PREFIX {
        +"Finding guild $string!".green
    }.send()

    Future {
        val guild = GuildRequest(string).execute()
        if (guild == null)
            FUY_PREFIX {
                +"$string is not a valid guild!".red
            }.send()
        else
            FUY_PREFIX {
                +guild.name.aqua
                +" [".darkAqua
                +guild.tag.aqua
                +"]".darkAqua

                +" has ".gray

                val online = guild.asSequence()
                    .map { it to it.world }
                    .filter { (_, world) -> world != null }
                    .sortedByDescending { (it, _) -> it.rank }
                    .toList()

                +online.size.toString().aqua
                +" of ".gray
                +guild.size.toString().aqua
                +" members online".gray

                if (online.isNotEmpty()) {
                    +": ".gray

                    online.iterate { (member, world) ->
                        member.toText(
                            this@FUY_PREFIX,
                            world,
                            true
                        )

                        if (hasNext())
                            +", ".gray
                    }
                }
            }.send()
    }
}

@Alias("pg")
@Subcommand("playerguild")
private fun CommandContext<*>.playerguild(
    @Argument("Player") string: String
) {
    if (!string.isUUID() && !Profile.isValid(string)) {
        FUY_PREFIX {
            +"$string is not a valid player!".red
        }.send()

        return
    }

    FUY_PREFIX {
        +"Finding player $string!".green
    }.send()

    inline {
        val member = unsafe {
            MemberRequest(string).execute()
        }.orElse(null)

        if (member == null)
            FUY_PREFIX {
                +"$string is not a valid player!".red
            }.send()
        else
            FUY_PREFIX {
                member.toText(
                    this,
                    null,
                    stars = true,
                    showWorld = false
                )

                if (member.guild == null)
                    +" is not in a guild!".gray
                else {
                    +" has been in ".gray
                    +member.guild!!.name.aqua
                    +" [".darkAqua
                    +member.guild!!.tag.aqua
                    +"]".darkAqua
                    +" for ".gray

                    val joined = member.joinedAt!!.timeSince()

                    if (joined < 1.minutes)
                        +joined.print(FormatFlag.COMPACT, TimeUnit.SECONDS).aqua
                    else
                        +joined.print(FormatFlag.COMPACT, TimeUnit.MINUTES).aqua

                    +".".gray
                }
            }.send()
    }
}

@Subcommand("ping")
private fun CommandContext<*>.ping() {
    inline {
        val start = Date()

        PingRequest().execute()

        FUY_PREFIX {
            +"Your ".gray
            +"ping".aqua
            +" to Buster is ".gray
            +start.timeSince().print(FormatFlag.COMPACT).aqua
            +"!".gray
        }.send()
    }
}

@Subcommand("wars")
private fun CommandContext<*>.wars(
    @Argument("Filter") filter: WarFilter
) {
    FUY_PREFIX {
        +"You have done ".gray

        val wars = WarModel.asSequence().filter { filter.test(it) }.count()

        +wars.toCommaString().escapeCommas().aqua

        +" war".gray

        if (wars != 1)
            +"s".gray

        +" matching the given ".gray
        +"filter".aqua
        +"!".gray
    }.send()
}

@Subcommand("wars details")
private fun CommandContext<*>.warDetails(
    @Argument("Filter") filter: WarFilter
) = warDetails(0, filter)

@Subcommand("wars details page")
private fun CommandContext<*>.warDetails(
    @Argument("Page") page: Int,
    @Argument("Filter") filter: WarFilter
) {
    val total = WarModel.filter { filter.test(it) }
    val wars = total.asReversed().asSequence().drop(page * WARS_PER_PAGE).take(WARS_PER_PAGE).toList()

    FUY_PREFIX {
        newLine()
        newLine()

        if (wars.isEmpty()) {
            line {
                center {
                    +"There is nothing to display!".white.underline
                }
            }
        } else {
            for (war in wars) {
                val damaged = war.initial != war.final

                center {
                    +war.territory.aqua.underline
                }

                newLine()

                center {
                    +"Controlled by ".gray
                    +war.owner.name.aqua
                    +" [".gray
                    +war.owner.tag.aqua
                    +"]".gray
                }

                newLine()

                center {
                    if (damaged) {
                        +"DPS: ".gray

                        +war.dps.toCommaString().aqua

                        +" | ".gray
                    }

                    +"Duration: ".gray

                    if (war.duration.toSeconds() < 1)
                        +war.duration.print(FormatFlag.COMPACT, TimeUnit.MILLISECONDS).aqua
                    else
                        +war.duration.print(FormatFlag.COMPACT, TimeUnit.SECONDS).aqua
                }

                newLine()
                newLine()

                center {
                    +"${if (damaged) "Initial " else ""}Tower Stats: ".reset.lightPurple
                        .onClick(
                            ClickEvent.Action.COPY_TO_CLIPBOARD,
                            Text { war.initial.appendTo(this) }.stringWithoutFormatting
                        )
                        .onHover(
                            HoverEvent.Action.SHOW_TEXT,
                            Text.component("Click to copy ${if (damaged) "initial " else ""}tower stats")
                        )

                    war.initial.appendTo(this)
                }

                if (damaged) {
                    newLine()

                    val final = war.final

                    center {
                        +"Final Tower Stats: ".reset.lightPurple
                            .onClick(
                                ClickEvent.Action.COPY_TO_CLIPBOARD,
                                Text { final.appendTo(this) }.stringWithoutFormatting
                            )
                            .onHover(
                                HoverEvent.Action.SHOW_TEXT,
                                Text.component("Click to copy final tower stats")
                            )

                        final.appendTo(this)
                    }
                }

                newLine()
            }
        }

        newLine()

        center {
            val maxPages = ceil(total.size / WARS_PER_PAGE.toDouble()).toInt()
            val hasNext = page < maxPages - 1
            val hasPrevious = page > 0


            if (hasPrevious) {
                +"⋘".white.onClick(ClickEvent.Action.RUN_COMMAND, "/fuy wars details page ${page - 1} $filter")
            } else {
                +"⋘".darkGray.strikethrough
            }

            +"   ".reset

            +(page + 1).toString().white
            +"/".gray
            +maxPages.coerceAtLeast(1).toString().white
            +"   ".reset

            if (hasNext) {
                +"⋙".white.onClick(ClickEvent.Action.RUN_COMMAND, "/fuy wars details page ${page + 1} $filter")
            } else {
                +"⋙".darkGray.strikethrough
            }
        }
    }.send()
}

private val worlds = mutableMapOf<UUID, World?>().synchronized()

private fun Guild.Member.toText(
    builder: Text.Builder,
    world: World?,
    stars: Boolean = false,
    showWorld: Boolean = true
) {
    builder.run {
        if (stars)
            rank?.also {
                +"\u2605".repeat(it.stars).aqua
            }
        val previous: World? = if (showWorld) {
            worlds[uuid].also {
                worlds[uuid] = world
            }
        } else null

        +name.aqua.onHover(HoverEvent.Action.SHOW_TEXT, Text.component {
            +name.gray

            if (previous != null || world != null) {
                +" (".gray

                if (previous?.name != world?.name) {
                    +(previous?.name ?: "None").darkGray
                    +" -> ".gray
                }

                +(world?.name ?: "None").white

                +")".gray
            }

            newLine()
            newLine()

            val playtime = profile.playtime.past(30.days)

            +playtime.print(
                FormatFlag.COMPACT, when {
                    playtime >= 1.hours -> TimeUnit.HOURS
                    playtime >= 1.minutes -> TimeUnit.MINUTES
                    else -> TimeUnit.SECONDS
                }
            ).aqua

            +" in the past".gray
            +" 30d".aqua

            if (size > 1) {
                newLine()
                newLine()

                +"Previously in".gray

                for (i in (lastIndex - 1) downTo 0) {
                    newLine()
                    val entry = this@toText[i]

                    +"  - ".darkGray
                    +"${entry.name} [${entry.tag}]".aqua
                }
            }

            if (profile.size > 1) {
                newLine()
                newLine()

                +"Also known as".gray

                for (i in (profile.size - 1) downTo 0) {
                    newLine()
                    val entry = this@toText.profile[i]

                    +"  - ".darkGray
                    +entry.name.aqua
                }
            }

            newLine()
            newLine()

            if (world != null) {
                +"Click to switch to ".gray
                +world.name.white

                newLine()

                +"(Requires ".darkPurple
                +"HERO".lightPurple
                +" rank)".darkPurple

                newLine()
                newLine()
            }

            +"$uuid".darkGray
        }).run {
            if (world != null)
                onClick(ClickEvent.Action.RUN_COMMAND, "/switch ${world.name}")
            else
                this
        }

        if (world != null) {
            +" (".gray

            if (world.name != previous?.name)
                +world.name.yellow
            else
                +world.name.gray

            +")".gray
        }
    }
}