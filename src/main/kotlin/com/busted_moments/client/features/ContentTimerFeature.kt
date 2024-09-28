package com.busted_moments.client.features

import com.busted_moments.client.Patterns
import com.busted_moments.client.framework.wynntils.Ticks
import com.busted_moments.client.framework.wynntils.esl
import com.busted_moments.client.framework.config.annotations.Persistent
import com.busted_moments.client.framework.config.entries.value.Value
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.features.*
import com.busted_moments.client.framework.render.TextRenderer
import com.busted_moments.client.framework.render.elements.textbox
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.text.Text.matches
import com.busted_moments.client.framework.text.Text.send
import com.busted_moments.client.framework.text.esl
import com.busted_moments.client.framework.text.get
import com.busted_moments.client.framework.util.Numbers.escapeCommas
import com.busted_moments.client.framework.util.Numbers.toCommaString
import com.busted_moments.client.models.content.ContentModel
import com.busted_moments.client.models.content.ContentTimer
import com.busted_moments.client.models.content.Stage
import com.busted_moments.client.models.content.event.ContentEvent
import com.busted_moments.client.models.content.stages.TextStage
import com.busted_moments.client.models.content.triggers.Triggers
import com.busted_moments.client.models.content.types.DungeonType
import com.busted_moments.client.models.content.types.LegendaryIslandType
import com.busted_moments.client.models.content.types.RaidType
import com.wynntils.core.text.StyledText
import com.wynntils.features.overlays.RaidProgressFeature
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent
import com.wynntils.overlays.RaidProgressOverlay
import com.wynntils.utils.colors.CustomColor
import com.wynntils.utils.mc.McUtils.mc
import com.wynntils.utils.render.type.HorizontalAlignment
import com.wynntils.utils.render.type.TextShadow
import com.wynntils.utils.render.type.VerticalAlignment
import net.essentuan.esl.color.Color
import net.essentuan.esl.time.duration.Duration
import net.essentuan.esl.time.duration.ms
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.components.ChatComponent
import net.minecraft.network.chat.Style
import net.neoforged.bus.api.EventPriority
import kotlin.math.max
import kotlin.random.Random

private const val RAID_NOT_READING = 0
private const val RAID_READING_COMPLETION = 1
private const val RAID_READING_FAIL = 2

private const val RAID_MAX_LINES = 7

@Replaces(RaidProgressFeature::class)
@Overlays(ContentTimerFeature.Overlay::class)
object ContentTimerFeature : Feature() {
    @Value("Send attempt summary")
    private var summary: Boolean = true

    @Persistent
    private var pbs: MutableMap<String, Duration> = mutableMapOf()

    private var pending: ContentTimer? = null
    private var fail: Boolean = false

    @Subscribe
    private fun ContentEvent.Finish.on() =
        complete(timer, false)

    @Subscribe
    private fun ContentEvent.Fail.on() =
        complete(timer, true)

    private val ContentTimer.isPb: Boolean
        get() {
            if (this.last().duration < 2.ms)
                return false

            return pbs.compute(type.id) { _, it ->
                if (it == null || duration < it)
                    duration
                else
                    it
            } == duration
        }

    private fun Duration.stringify(): String {
        val minutes = (minutes() + (hours() * 60)).toInt()
        val seconds = seconds().toInt()
        val mills = mills().toInt() / 10

        if (minutes == 0 && seconds == 0 && mills() < 2)
            return "--:--"

        return "${if (minutes < 10) 0 else ""}$minutes:${if (seconds < 10) 0 else ""}$seconds.${if (mills < 10) "0" else ""}$mills"
    }

    private fun complete(timer: ContentTimer, fail: Boolean) {
        pending = timer
        this.fail = fail

        when (timer.type) {
            is DungeonType ->
                dungeon()

            is LegendaryIslandType ->
                legendaryIsland()
        }
    }

    private fun Text.Builder.scramble(name: String, color: Color, underline: Boolean, bold: Boolean) {
        val random = Random(name.hashCode())

        for (c in name) {
            if (random.nextDouble() > 0.65)
                +c.toString().darkRed.obfuscate.also {
                    if (underline)
                        it.underline

                    if (bold)
                        it.bold
                }
            else
                +c.toString().reset.color(color).also {
                    if (underline)
                        it.underline

                    if (bold)
                        it.bold
                }
        }
    }

    @Subscribe(priority = EventPriority.LOW)
    private fun ChatMessageReceivedEvent.on() {
        if (pending == null)
            return

        originalStyledText.matches {
            //Raids
            mutate(Text::normalized) {
                Patterns.RAID_COMPLETION { _, _ ->
                    raidState = RAID_READING_COMPLETION
                    raidRead = 0

                    isCanceled = true

                    return@on
                }

                Patterns.RAID_FAIL { _, _ ->
                    raidState = RAID_READING_FAIL
                    raidRead = 0

                    isCanceled = true

                    return@on
                }

                Patterns.RAID_STATISTICS_END { _, _ ->
                    raidState = RAID_NOT_READING

                    raidLines += originalStyledText
                    isCanceled = true

                    raid()

                    return@on
                }

                Patterns.TIME_ELAPSED { _, _ ->
                    isCanceled = true

                    if (raidState == RAID_READING_FAIL) {
                        raidState = RAID_NOT_READING

                        raid()
                    }

                    return@on
                }
            }

            //Dungeons
            Patterns.DUNGEON_COMPLETION { _, _ ->
                isCanceled = true

                return@on
            }

            Patterns.DUNGEON_REWARD { matcher, _ ->
                isCanceled = true

                val reward = matcher["reward"]!!
                if (reward != "0 XP")
                    dungeonRewards += reward

                return@on
            }

            //LI
            Patterns.LI_REWARD { _, text ->
                isCanceled = true

                liRewards += text.trim()

                return@on
            }
        }

        if (raidState != RAID_NOT_READING) {
            if (++raidRead > RAID_MAX_LINES) {
                raidState = RAID_NOT_READING
                return
            }

            isCanceled = true

            if (raidState == RAID_READING_COMPLETION)
                raidLines += originalStyledText
        }
    }

    //Raids
    private var raidState: Int = RAID_NOT_READING
    private var raidRead: Int = 0
    private val raidLines = mutableListOf<StyledText>()

    private fun raid() {
        val timer = pending!!

        Text {
            center {
                if (fail)
                    scramble(timer.type.print(), ChatFormatting.RED.esl, false, true)
                else
                    +timer.type.print().gold.bold
            }

            newLine()

            center {
                +"Time Elapsed: ".gray
                +timer.duration.stringify().white
            }

            if (!fail && timer.isPb) {
                newLine()

                center {
                    +"NEW PERSONAL BEST!".yellow.bold.underline
                }
            }

            newLine()

            val lines = timer.fold(mutableListOf<MutableList<Stage>>()) { list, it ->
                if (list.isEmpty() || it is TextStage || list.last().size >= 3)
                    list += mutableListOf<Stage>()

                list.last() += it

                list
            }

            val chatWidth = ChatComponent.getWidth(mc().options.chatWidth().get())

            val p1 = chatWidth / 2 - chatWidth / 4
            val p2 = chatWidth / 2 + chatWidth / 4

            val spaceWidth = TextRenderer.width(' '.code, Style.EMPTY)

            for (line in 0..<lines.size step 2) {
                newLine()

                val end = max(lines[line].size, lines.getOrNull(line + 1)?.size ?: 0) - 1
                for (i in 0..end)
                    line {
                        val first = Text first@{
                            val stage = lines[line].getOrNull(i) ?: return@first

                            if (stage is TextStage)
                                +stage.text
                            else {
                                +stage.name.reset.lightPurple
                                +": ".lightPurple
                                +stage.duration.stringify().aqua
                            }
                        }

                        val firstWidth = TextRenderer.split(first).width
                        var current = 0f

                        if (!first.isEmpty) {
                            if (lines.getOrNull(line + 1) == null) {
                                val spaces = ((chatWidth / 2 - firstWidth / 2f) / spaceWidth).toInt()
                                current += spaceWidth * spaces

                                +" ".repeat(spaces.coerceAtLeast(0)).reset
                            } else {
                                val spaces = ((p1 - firstWidth / 2) / spaceWidth).toInt()
                                current += spaceWidth * spaces

                                +" ".repeat(spaces.coerceAtLeast(0)).reset
                            }

                            +first
                            current += firstWidth
                        }

                        val stage = lines.getOrNull(line + 1)?.getOrNull(i) ?: return@line
                        val second = Text {
                            if (stage is TextStage)
                                +stage.text
                            else {
                                +stage.name.reset.lightPurple

                                +": ".lightPurple
                                +stage.duration.stringify().aqua
                            }
                        }

                        val secondWidth: Float = TextRenderer.split(second).width
                        val spaces = (p2 - current - (secondWidth / 2f)) / spaceWidth

                        +" ".repeat(spaces.toInt().coerceAtLeast(0)).reset
                        +second
                    }
            }

            if (raidLines.isNotEmpty()) {
                for (line in raidLines) {
                    +line
                    newLine()
                }

                raidLines.clear()
            }
        }.send()

        pending = null
    }

    //Dungeons
    private val dungeonRewards = mutableListOf<String>()

    private fun dungeon() {
        Ticks.schedule(1) {
            val timer = pending!!

            Text {
                center {
                    if (dungeonRewards.isEmpty() || fail)
                        scramble(timer.type.print(), ChatFormatting.RED.esl, false, true)
                    else
                        +timer.type.print().green.underline
                }

                val lines = timer.fold(mutableListOf<MutableList<Stage>>()) { list, it ->
                    if (list.isEmpty() || it is TextStage || list.last().size >= 3)
                        list += mutableListOf<Stage>()

                    list.last() += it

                    list
                }

                val chatWidth = ChatComponent.getWidth(mc().options.chatWidth().get())

                val p1 = chatWidth / 2 - chatWidth / 4
                val p2 = chatWidth / 2 + chatWidth / 4

                val spaceWidth = TextRenderer.width(' '.code, Style.EMPTY)

                newLine()

                for (line in 0..<lines.size step 2) {
                    newLine()

                    val end = max(lines[line].size, lines.getOrNull(line + 1)?.size ?: 0) - 1
                    for (i in 0..end)
                        line {
                            val first = Text first@{
                                val stage = lines[line].getOrNull(i) ?: return@first

                                if (stage is TextStage)
                                    +stage.text
                                else {
                                    +stage.name.reset.lightPurple
                                    +": ".lightPurple
                                    +stage.duration.stringify().aqua
                                }
                            }

                            val firstWidth = TextRenderer.split(first).width
                            var current = 0f

                            if (!first.isEmpty) {
                                if (lines.getOrNull(line + 1) == null) {
                                    val spaces = ((chatWidth / 2 - firstWidth / 2f) / spaceWidth).toInt()
                                    current += spaceWidth * spaces

                                    +" ".repeat(spaces.coerceAtLeast(0)).reset
                                } else {
                                    val spaces = ((p1 - firstWidth / 2) / spaceWidth).toInt()
                                    current += spaceWidth * spaces

                                    +" ".repeat(spaces.coerceAtLeast(0)).reset
                                }

                                +first
                                current += firstWidth
                            }

                            val stage = lines.getOrNull(line + 1)?.getOrNull(i) ?: return@line
                            val second = Text {
                                if (stage is TextStage)
                                    +stage.text
                                else {
                                    +stage.name.reset.lightPurple

                                    +": ".lightPurple
                                    +stage.duration.stringify().aqua
                                }
                            }

                            val secondWidth: Float = TextRenderer.split(second).width
                            val spaces = (p2 - current - (secondWidth / 2f)) / spaceWidth

                            +" ".repeat(spaces.toInt().coerceAtLeast(0)).reset
                            +second
                        }
                }

                newLine()

                center {
                    +"Total: ".lightPurple
                    +timer.duration.stringify().aqua
                }

                if (!fail && timer.isPb) {
                    newLine()

                    center {
                        +"NEW PERSONAL BEST!".yellow.bold.underline
                    }
                }

                if (dungeonRewards.isNotEmpty()) {
                    newLine()
                    newLine()

                    center {
                        +"Rewards".reset.gold
                    }

                    for (i in 0..<dungeonRewards.size step 2) {
                        newLine()

                        fun Text.Builder.reward(string: String) {
                            when {
                                string.endsWith(" XP") -> {
                                    val xp = string.removeSuffix(" XP").toLong()

                                    +xp.toCommaString().escapeCommas().gold
                                    +" XP".gold
                                }

                                string.endsWith(" falling emeralds") -> {
                                    val emeralds = string.removeSuffix(" falling emeralds").toLong()

                                    +emeralds.toCommaString().escapeCommas().green
                                    +" emeralds".green
                                }

                                else -> {
                                    +string.gray
                                }
                            }
                        }

                        center {
                            reward(dungeonRewards[i])

                            val next = dungeonRewards.getOrNull(i + 1) ?: return@center

                            +", ".reset.gray

                            reward(next)

                            if ((i + 1) != dungeonRewards.lastIndex)
                                +", ".reset.gray
                        }
                    }

                    dungeonRewards.clear()
                }
            }.send()

            pending = null
        }
    }


    //Legendary Island
    private val liRewards = mutableListOf<StyledText>()

    private fun legendaryIsland() {
        Ticks.schedule(10) {
            val timer = pending!!

            Text {
                center {
                    if (liRewards.isEmpty())
                        scramble(timer.type.print(), ChatFormatting.RED.esl, true, false)
                    else
                        +timer.type.print().green.underline
                }

                val lines = timer.fold(mutableListOf<MutableList<Stage>>()) { list, it ->
                    if (it is TextStage)
                        list += mutableListOf<Stage>()

                    list.last() += it

                    list
                }

                val chatWidth = ChatComponent.getWidth(mc().options.chatWidth().get())

                val p1 = chatWidth / 2 - chatWidth / 4
                val p2 = chatWidth / 2 + chatWidth / 4

                val spaceWidth = TextRenderer.width(' '.code, Style.EMPTY)

                newLine()

                for (line in 0..<lines.size step 2) {
                    newLine()

                    val end = max(lines[line].size, lines.getOrNull(line + 1)?.size ?: 0) - 1
                    for (i in 0..end)
                        line {
                            val first = Text first@{
                                val stage = lines[line].getOrNull(i) ?: return@first

                                if (stage is TextStage)
                                    +stage.text
                                else {
                                    +stage.name.reset.lightPurple
                                    +": ".lightPurple
                                    +stage.duration.stringify().aqua
                                }
                            }

                            val firstWidth = TextRenderer.split(first).width
                            var current = 0f

                            if (!first.isEmpty) {
                                if (lines.getOrNull(line + 1) == null) {
                                    val spaces = ((chatWidth / 2 - firstWidth / 2f) / spaceWidth).toInt()
                                    current += spaceWidth * spaces

                                    +" ".repeat(spaces.coerceAtLeast(0)).reset
                                } else {
                                    val spaces = ((p1 - firstWidth / 2) / spaceWidth).toInt()
                                    current += spaceWidth * spaces

                                    +" ".repeat(spaces.coerceAtLeast(0)).reset
                                }

                                +first
                                current += firstWidth
                            }

                            val stage = lines.getOrNull(line + 1)?.getOrNull(i) ?: return@line
                            val second = Text {
                                if (stage is TextStage)
                                    +stage.text
                                else {
                                    +stage.name.reset.lightPurple
                                    +": ".lightPurple
                                    +stage.duration.stringify().aqua
                                }
                            }

                            val secondWidth: Float = TextRenderer.split(second).width
                            val spaces = (p2 - current - (secondWidth / 2f)) / spaceWidth

                            +" ".repeat(spaces.toInt().coerceAtLeast(0)).reset
                            +second
                        }
                }

                newLine()

                center {
                    +"Total: ".lightPurple
                    +timer.duration.stringify().aqua
                }

                if (!fail && timer.isPb) {
                    newLine()

                    center {
                        +"NEW PERSONAL BEST!".yellow.bold.underline
                    }
                }

                if (liRewards.isNotEmpty()) {
                    newLine()
                    newLine()

                    center {
                        +"Rewards".reset.green
                    }

                    for (i in 0..<liRewards.size step 2) {
                        newLine()

                        center {
                            +liRewards[i]

                            val next = liRewards.getOrNull(i + 1) ?: return@center

                            +", ".reset.gray

                            +next

                            if ((i + 1) != liRewards.lastIndex)
                                +", ".reset.gray
                        }
                    }

                    liRewards.clear()
                }
            }.send()

            pending = null
        }
    }

    @Define(
        name = "Content Timer Overlay",
        at = At(
            x = 20.25F,
            y = 5.0F
        ),
        size = Size(
            width = 300f,
            height = 30f
        ),
        align = Align(
            vert = VerticalAlignment.TOP,
            horizontal = HorizontalAlignment.LEFT
        ),
        anchor = Anchor.TOP_LEFT
    )
    @Replaces(RaidProgressOverlay::class)
    object Overlay : AbstractOverlay() {
        @Value("Text Style")
        private var style = TextShadow.OUTLINE

        @Value("Background Color", alpha = true)
        private var backgroundColor = CustomColor(0, 0, 0, 127).esl

        @Value("Show in Raids")
        private var raids: Boolean = true

        @Value("Show in Dungeons")
        private var dungeons: Boolean = true

        @Value("Show in Legendary Island")
        private var legendaryIsland: Boolean = true

        private val PREVIEW = ContentTimer(
            LegendaryIslandType,
            LegendaryIslandType,
            Triggers.nothing(),
            emptyList()
        ).also { it.close() }

        override fun render(ctx: Context): Boolean {
            textbox {
                val timer = if (ctx.preview)
                    PREVIEW
                else
                    ContentModel.active ?: return@textbox false

                when {
                    !ctx.preview && timer.start == null ->
                        return@textbox false

                    timer.type is RaidType && !raids ->
                        return@textbox false

                    timer.type is DungeonType && !dungeons ->
                        return@textbox false

                    timer.type is LegendaryIslandType && !legendaryIsland ->
                        return@textbox false
                }

                frame()

                background = backgroundColor
                style = this@Overlay.style

                padding.all(5f)

                text = Text {
                    +timer.type.print().gold.underline

                    newLine()
                    newLine()

                    for (stage in timer) {
                        if (stage is TextStage) {
                            if (timer[0] !== stage)
                                newLine()

                            +stage.text
                        } else {
                            +stage.name.reset.lightPurple

                            +": ".lightPurple
                            +stage.duration.stringify().aqua
                        }

                        newLine()
                    }

                    newLine()

                    +"Total: ".lightPurple
                    +timer.duration.stringify().aqua
                }

                true
            }

            return true
        }
    }
}