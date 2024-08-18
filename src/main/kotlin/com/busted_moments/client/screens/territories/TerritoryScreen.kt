package com.busted_moments.client.screens.territories

import com.busted_moments.buster.api.Territory
import com.busted_moments.client.features.war.TerritoryHelperMenuFeature
import com.busted_moments.client.features.war.wynntils.PROD_SIZE
import com.busted_moments.client.framework.Containers
import com.busted_moments.client.framework.FabricLoader
import com.busted_moments.client.framework.wynntils.wynntils
import com.busted_moments.client.framework.wynntils.defenseColor
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.keybind.Inputs
import com.busted_moments.client.framework.render.Renderer.Companion.contains
import com.busted_moments.client.framework.render.TextRenderer
import com.busted_moments.client.framework.render.Texture
import com.busted_moments.client.framework.render.Textures
import com.busted_moments.client.framework.render.dynamic
import com.busted_moments.client.framework.render.elements.TextureElement
import com.busted_moments.client.framework.render.elements.textbox
import com.busted_moments.client.framework.render.elements.texture
import com.busted_moments.client.framework.render.helpers.Percentage.Companion.pct
import com.busted_moments.client.framework.render.of
import com.busted_moments.client.framework.render.screen.Screen
import com.busted_moments.client.framework.render.screen.elements.click
import com.busted_moments.client.framework.render.screen.elements.hover
import com.busted_moments.client.framework.render.screen.elements.scrollable
import com.busted_moments.client.framework.render.screen.elements.textinput
import com.busted_moments.client.framework.render.text
import com.busted_moments.client.framework.render.texture
import com.busted_moments.client.framework.sounds.Sounds.play
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.text.esl
import com.busted_moments.client.framework.util.Numbers.toCommaString
import com.busted_moments.client.models.territories.eco.Economy
import com.busted_moments.client.models.territories.eco.TerritoryData
import com.busted_moments.client.models.territories.eco.TerritoryScanner
import com.busted_moments.client.screens.territories.search.Criteria
import com.busted_moments.client.screens.territories.search.TerritorySearch
import com.google.common.collect.SetMultimap
import com.mojang.blaze3d.systems.RenderSystem
import com.wynntils.core.components.Models
import com.wynntils.mc.event.ContainerSetContentEvent
import com.wynntils.mc.event.MenuEvent.MenuClosedEvent
import com.wynntils.utils.MathUtils
import com.wynntils.utils.mc.McUtils.mc
import com.wynntils.utils.render.FontRenderer
import com.wynntils.utils.render.RenderUtils
import com.wynntils.utils.render.buffered.BufferedRenderUtils
import com.wynntils.utils.render.type.HorizontalAlignment
import com.wynntils.utils.render.type.VerticalAlignment
import me.shedaniel.clothconfig2.impl.EasingMethod
import net.essentuan.esl.collections.maps.IntMap
import net.essentuan.esl.collections.multimap.Multimaps
import net.essentuan.esl.collections.multimap.hashSetValues
import net.essentuan.esl.format.truncate
import net.essentuan.esl.iteration.extensions.iterate
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.PlainTextContents
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.neoforged.bus.api.EventPriority
import java.util.Optional
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.floor

private const val BACKGROUND_SCALE = 1.15f
private const val BACKGROUND_TOP_MARGINS = 14.5f
private const val BACKGROUND_RIGHT_MARGINS = 26f
private const val BACKGROUND_BOTTOM_MARGINS = 22f
private const val BACKGROUND_LEFT_MARGINS = 8f

private const val ITEM_SCALE = 1.45F
private const val ITEM_WIDTH = (16 * ITEM_SCALE) + 5.3f
private const val ITEM_HEIGHT = (16 * ITEM_SCALE) + 4.9f
private const val MAX_COLS = 9

const val BACK_SLOT = 18

private const val ENTRIES_PER_PAGE = 4

abstract class TerritoryScreen<T : TerritoryScanner>(
    val container: Int,
    val scanner: T
) : Screen() {
    private var eco: Economy? = null

    @Suppress("UNCHECKED_CAST")
    private var counts: SetMultimap<Int, String> =
        Multimaps
            .keys { IntMap<Collection<Any?>>() as MutableMap<in Any?, Collection<Any?>> }
            .hashSetValues()

    private var matched: List<Pair<TerritoryData, List<QuickAccess.Option>>> = emptyList()
    private var output: List<Component> = emptyList()
    private var outputWidth: Float = 0f
    private var outputHeight: Float = 0f

    private var legend: List<Component> = emptyList()
    private val maxPages: Int
        get() =
            Criteria.Provider.builders.size / ENTRIES_PER_PAGE

    private var page: Int = -1
        set(value) {
            val difference = value - field

            field = MathUtils.overflowInRange(
                page,
                difference,
                0,
                maxPages - 1
            )

            legend = Text {
                +"Legend".white.bold

                Criteria.Provider.builders.asSequence()
                    .drop(page * ENTRIES_PER_PAGE)
                    .take(ENTRIES_PER_PAGE)
                    .forEach { builder ->
                        newLine()
                        newLine()

                        +builder.names.joinToString().yellow
                        +" | ".yellow

                        builder.operators iterate {
                            +it.symbol.aqua

                            if (hasNext())
                                +", ".gray
                        }

                        builder.suggestions.let {
                            if (it.isEmpty())
                                return@let

                            newLine()

                            +"Options: ".gray

                            it iterate { option ->
                                +option.aqua

                                if (hasNext())
                                    +", ".gray
                            }
                        }
                    }

                newLine()
                newLine()

                +"Page ${page + 1}/$maxPages".gray.bold
                newLine()
                +"(Switch pages with Left/Right click)".reset.gray
            }.split("\n").map { it.component }
        }

    init {
        scanner.update = ::handle

        page = 0
    }

    abstract fun TextureElement<Context>.renderSidebar(ctx: Context)

    @Subscribe(priority = EventPriority.HIGHEST)
    fun ContainerSetContentEvent.Pre.on() {
        if (container != containerId) {
            mc().setScreen(null)
        }
    }

    @Subscribe(priority = EventPriority.HIGHEST)
    fun MenuClosedEvent.on() {
        mc().setScreen(null)
    }

    private fun handle(eco: Economy) {
        this.eco = eco

        val text = Text {
            +"Guild Output".white.bold
            newLine()
            +"Total resource output".reset.gray
            newLine()
            +"and overall costs".gray
            newLine()
            newLine()

            for (resource in Territory.Resource.entries) {
                val color = resource.wynntils.color.esl
                val storage = eco.total[resource] ?: TerritoryData.Storages(-1)
                val tributes = Models.Guild.getReceivedTributesForResource(resource.wynntils)

                val prefix = if (resource.wynntils.symbol.isNotEmpty()) "${resource.wynntils.symbol} " else ""
                +"$prefix+${(storage.production + tributes).toCommaString()} ${resource.wynntils.getName()} per hour".color(
                    color
                )
                newLine()

                if (tributes > 0) {
                    +"$prefix(${tributes.toCommaString()} from Tributes)".color(color)
                    newLine()
                }

                +"$prefix${storage.stored.toCommaString()}/${storage.capacity.toCommaString()} in storage".color(color)

                newLine()
            }

            newLine()

            +"Overall Cost (per hour):".green

            for (resource in Territory.Resource.entries) {
                newLine()

                val storage = eco.total[resource] ?: TerritoryData.Storages(-1)
                val cost = storage.cost + Models.Guild.getSentTributesForResource(resource.wynntils)
                val prod = storage.production + Models.Guild.getReceivedTributesForResource(resource.wynntils)
                val overall = prod - cost

                val prefix = if (resource.wynntils.symbol.isNotEmpty()) "${resource.wynntils.symbol} " else ""

                +"- ".green
                +prefix.gray
                +cost.toCommaString().gray
                +" "
                +resource.wynntils.getName().gray
                +" (${if (overall < 0) "-" else "+"}${overall.absoluteValue.truncate()})".let {
                    if (overall < 0)
                        it.red
                    else
                        it.blue
                }

                if (TerritoryHelperMenuFeature.showUsagePercents) {
                    +" (${((cost / prod.toFloat()) * 100).toInt()}%)".let {
                        if (overall < 0)
                            it.red
                        else
                            it.darkGray
                    }
                }
            }
        }

        output = text.split("\n").map { it.component }

        val split = TextRenderer.split(text)
        outputWidth = split.width + 16
        outputHeight = split.height

        counts.clear()

        matched = eco.values.asSequence()
            .map {
                val filters = QuickAccess.filterIndexed { index, option ->
                    if (option.test(it)) {
                        counts[index] += it.name

                        if (option is QuickAccess.Option.Predicate) option.enabled else true
                    } else {
                        if (option.required)
                            return@map null

                        false
                    }
                }

                if (filters.isEmpty() && QuickAccess.Strict.enabled)
                    null
                else if (!TerritorySearch.test(it))
                    null
                else
                    it to filters
            }.filterNotNull().toList()

        eco.mapValues { (name, territory) ->
            QuickAccess.filterIndexed { index, option ->
                if (option.test(territory)) {
                    counts[index] += name

                    true
                } else false
            }
        }
    }

    private fun update() {
        handle(eco ?: return)
    }

    final override fun render(ctx: Context): Boolean {
        dynamic {
            renderBackground(
                ctx.graphics,
                ctx.mouseX.toInt(),
                ctx.mouseY.toInt(),
                ctx.deltaTracker.realtimeDeltaTicks
            )
        }

        texture background@{
            texture = Textures.TERRITORY_MANAGEMENT_BACKGROUND.texture
            size *= BACKGROUND_SCALE

            pos = (50.pct of ctx.window) - (50.pct of this)

            textbox {
                fun hovered(): Int {
                    if (!this.contains(ctx))
                        return -1

                    return (((ctx.mouseY - y) / TextRenderer.font.lineHeight) / 2).toInt();
                }

                text = Text {
                    val hovered = hovered()
                    var index = 0

                    QuickAccess iterate {
                        it.run {
                            append(counts[index].size, hovered == index)
                            index++
                        }

                        if (hasNext()) {
                            newLine()
                            newLine()
                        }
                    }
                }

                x = this@background.x + this@background.width + 10
                y = (50.pct of ctx.window.guiScaledHeight) - (split.height / 2f)

                horizontalAlignment = HorizontalAlignment.LEFT
                verticalAlignment = VerticalAlignment.TOP

                padding.all(5f)

                resize()

                click { _: Double, _: Double, button: Int ->
                    when {
                        QuickAccess.getOrNull(hovered())?.click(button) == true -> {
                            SoundEvents.WOODEN_BUTTON_CLICK_ON.play(SoundSource.MASTER)
                            update()

                            true
                        }

                        button == 2 -> {
                            SoundEvents.WOODEN_BUTTON_CLICK_ON.play(SoundSource.MASTER)
                            QuickAccess.reset()
                            update()

                            true
                        }

                        else -> false
                    }
                }
            }

            scrollable {
                x = this@background.x + (BACKGROUND_LEFT_MARGINS * BACKGROUND_SCALE)
                y = this@background.y + (BACKGROUND_TOP_MARGINS * BACKGROUND_SCALE)
                width = this@background.width - (BACKGROUND_RIGHT_MARGINS * BACKGROUND_SCALE)
                height = this@background.height - (BACKGROUND_BOTTOM_MARGINS * BACKGROUND_SCALE)

                texture = Textures.SCROLL_BUTTON.texture
                sliderOriginX =
                    ((this@background.x + this@background.width) - (BACKGROUND_RIGHT_MARGINS * BACKGROUND_SCALE) / 2f) - texture.width / 2f - 1f
                sliderOriginY = y - (texture.height / 2f) + 9f
                sliderHeight = height - 18f

                intensity = 30.0

                easing = EasingMethod.EasingMethodImpl.QUINTIC

                fun scrollOffset(): Float {
                    val maxRows = this.height / ITEM_HEIGHT
                    val rows = ceil((eco ?: return 0f).size / MAX_COLS.toFloat())

                    return (((rows - maxRows).coerceAtLeast(2f) * ITEM_HEIGHT) * progress.toFloat()) - 0.5f
                }

                dynamic {
                    var hovered: TerritoryData? = null

                    if (eco == null)
                        return@dynamic

                    val offset = scrollOffset()

                    for (i in matched.indices) {
                        val (territory, highlights) = matched[i]

                        val col = i % MAX_COLS
                        val row = floor(i / MAX_COLS.toDouble()).toInt()

                        val renderX = this@scrollable.x + (col * ITEM_WIDTH)
                        val renderY = this@scrollable.y + (row * ITEM_HEIGHT) - offset

                        if (
                            !this@scrollable.contains(renderX, renderY, ctx.pose) &&
                            !this@scrollable.contains(renderX, renderY + this@scrollable.height, ctx.pose)
                        )
                            continue

                        territory.render(
                            ctx,
                            highlights,
                            renderX,
                            renderY,
                            ((i + 1) % MAX_COLS) != col
                        )

                        if (
                            this@scrollable.contains(ctx) &&
                            hovered == null &&
                            ctx.mouseX > renderX &&
                            ctx.mouseX < (renderX + ITEM_WIDTH) &&
                            ctx.mouseY > renderY &&
                            ctx.mouseY < renderY + ITEM_HEIGHT
                        ) {
                            hovered = territory
                        }
                    }

                    ctx.buffer.endBatch()
                    RenderUtils.disableScissor()

                    if (hovered != null) {
                        var added = false

                        ctx.graphics.renderTooltip(
                            font,
                            getTooltipFromItem(mc(), hovered.item).flatMap {
                                val contents = it.siblings.getOrNull(0)?.contents ?: it.contents

                                if (contents !is PlainTextContents.LiteralContents)
                                    return@flatMap listOf(it)

                                val text = Text.strip(contents.text)

                                when {
                                    text.getOrNull(0) == '\u2726' -> {
                                        added = true

                                        listOf(
                                            it,
                                            Text.component {
                                                +"\u26e8 Defense: ".lightPurple
                                                +hovered.defense.print().color(
                                                    hovered.defense.defenseColor
                                                )
                                            }
                                        )
                                    }

                                    !added && (text.startsWith("Upgrades:") || text.startsWith("No upgrades active")) -> {
                                        added = true

                                        listOf(
                                            Text.component {
                                                +"\u26e8 Defense: ".lightPurple
                                                +hovered.defense.print().color(
                                                    hovered.defense.defenseColor
                                                )
                                            },
                                            Text.component(""),
                                            it,
                                        )
                                    }

                                    else -> listOf(it)
                                }
                            },
                            hovered.item.tooltipImage,
                            ctx.mouseX.toInt(),
                            ctx.mouseY.toInt()
                        )
                    }
                }

                this@scrollable.click { mouseX, mouseY, button ->
                    if (button != Inputs.MOUSE_BUTTON_LEFT)
                        return@click false

                    val col = floor((mouseX - this@scrollable.x) / ITEM_WIDTH)
                    val row = floor((mouseY - this@scrollable.y + scrollOffset()) / ITEM_HEIGHT)
                    val index = floor(col + (row * MAX_COLS)).toInt()

                    select(matched.getOrNull(index)?.first ?: return@click false)

                    true
                }
            }

            textinput(TerritorySearch.input, TerritorySearch) {
                width = 200f
                height = 12f

                maxWidth = width - 23f

                scale = 0.75f

                x =
                    this@background.x + this@background.width / 2f - this.width / 2f - (BACKGROUND_LEFT_MARGINS * BACKGROUND_SCALE)
                y = this@background.y + 2.5f

                texture {
                    texture = Textures.INFO.texture
                    size *= 0.35

                    x = this@textinput.x + this@textinput.maxWidth + 7f
                    y = this@textinput.y + this@textinput.height / 2f - this.height / 2f

                    click { _: Double, _: Double, button: Int ->
                        when (button) {
                            Inputs.MOUSE_BUTTON_LEFT -> page++
                            Inputs.MOUSE_BUTTON_RIGHT -> page--
                        }

                        SoundEvents.WOODEN_BUTTON_CLICK_ON.play()

                        true
                    }

                    hover {
                        ctx.graphics.renderTooltip(
                            TextRenderer.font,
                            legend,
                            Optional.empty(),
                            it.mouseX.toInt(),
                            it.mouseY.toInt(),
                        )
                    }
                }
            }

            texture sidebar@{
                texture = Textures.TERRITORY_SIDEBAR.texture
                size *= 1.05

                x = this@background.x - width - 7
                y = (50.pct of ctx.window.guiScaledHeight) - (50.pct of height)

                renderSidebar(it)

                dynamic {
                    ctx.pose.pushPose()
                    ctx.pose.translate(0f, 24.5f, 0f)

                    ctx.graphics.renderComponentTooltip(
                        FontRenderer.getInstance().font,
                        output,
                        (this@sidebar.x - outputOffsetX - outputWidth - 4).toInt(),
                        (this@background.y - outputOffsetY - (outputHeight / 4f)).toInt()
                    )

                    ctx.pose.popPose()
                }
            }
        }

        return true
    }

    abstract fun select(territory: TerritoryData)

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        for (listener in children()) {
            if (listener.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
                return true
        }

        return false
    }

    override fun close() {
        scanner.close()

        Containers.close(container)
    }

    protected open fun TerritoryData.render(
        ctx: Context,
        highlights: List<QuickAccess.Option>,
        x: Float,
        y: Float,
        last: Boolean
    ) {
        ctx.pose.pushPose()
        ctx.pose.translate(x, y, 1000f)

        if (highlights.isNotEmpty()) {
            val group = highlights.firstOrNull { it.group != -1 }?.group

            if (group != null)
                BufferedRenderUtils.drawMulticoloredRect(
                    ctx.pose,
                    ctx.buffer,
                    highlights.asSequence()
                        .filter {
                            it.group == group
                        }
                        .map {
                            it.color.darken(1.35f).with(alpha = 0.5f).wynntils
                        }
                        .toList(),
                    0f,
                    0f,
                    0f,
                    ITEM_WIDTH,
                    ITEM_HEIGHT
                )
        }

        ctx.pose.pushPose()
        ctx.pose.translate(2.5f, if (hq) 4f else 3f, -1000f)

        ctx.pose.scale(ITEM_SCALE, ITEM_SCALE, 1f)

        if (ignored) {
            RenderSystem.enableBlend()
            RenderSystem.setShaderColor(1f, 1f, 1f, 0.5f)
        }

        ctx.graphics.renderItem(
            item,
            0,
            0
        )

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        RenderSystem.disableBlend()

        ctx.pose.popPose()

        var maxLabelSize = 0.9f

        if (TerritoryHelperMenuFeature.showProduction) {
            val icons = ArrayList<Texture>(4)

            for ((resource, storage) in resources) {
                if (storage.base <= 0)
                    continue

                when (resource) {
                    Territory.Resource.EMERALDS -> {
                        if (storage.base > 9000)
                            icons += Texture.EMERALD
                    }

                    else -> {
                        val icon = when (resource) {
                            Territory.Resource.ORE -> Texture.ORE
                            Territory.Resource.WOOD -> Texture.WOOD
                            Territory.Resource.FISH -> Texture.FISH
                            Territory.Resource.CROP -> Texture.CROP
                            else -> throw IllegalArgumentException()
                        }

                        for (i in 0..<ceil((storage.base / 900.0) / 4.0).toInt())
                            icons += icon
                    }
                }
            }

            val lines = ceil(icons.size / 2.0).toInt()

            if (lines > 1)
                maxLabelSize = 0.85f

            val prodX: Float = 2.5f + (ITEM_WIDTH / 2) - if (icons.size == 1) 2f else 1f
            val prodY: Float = (ITEM_HEIGHT / 2) - ((PROD_SIZE * lines) / 2f) + (if (hq) 0f else 1f)

            var row = 0
            var col = 0

            var cols = 1

            icons iterate {
                if (col == 0) {
                    cols = if (hasNext()) 2 else 1
                }

                it.render(
                    ctx.pose,
                    ctx.buffer,
                    x = prodX + (col * 8f) - ((cols * PROD_SIZE) / 2f),
                    y = prodY + (row * 8f),
                    width = PROD_SIZE,
                    height = PROD_SIZE
                )

                col++

                if (col == 2) {
                    col = 0
                    row++
                }
            }
        } else
            maxLabelSize = 1f

        val split = TextRenderer.split(
            Text {
                +acronym.gold
            }
        )

        ctx.text(
            split,
            2f,
            2f,
            scale = (1 / (split.width / (ITEM_WIDTH - 7))).coerceAtMost(maxLabelSize)
        )

        ctx.pose.popPose()

        ctx.buffer.endLastBatch()
    }

    companion object {
        private val outputOffsetX: Float
        private val outputOffsetY: Float

        init {
            val container = FabricLoader.getModContainer(
                "legendarytooltips"
            )

            if (container.isPresent) {
                outputOffsetX = 2f
                outputOffsetY = 6.5f
            } else {
                outputOffsetX = 0f
                outputOffsetY = 0f
            }
        }
    }
}