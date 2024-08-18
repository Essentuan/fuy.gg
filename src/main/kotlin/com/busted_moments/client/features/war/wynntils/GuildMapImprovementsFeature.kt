package com.busted_moments.client.features.war.wynntils

import com.busted_moments.buster.api.Territory
import com.busted_moments.client.buster.TerritoryList
import com.busted_moments.client.buster.color
import com.busted_moments.client.features.war.timerString
import com.busted_moments.client.framework.wynntils.wynntils
import com.busted_moments.client.framework.wynntils.esl
import com.busted_moments.client.framework.config.annotations.Category
import com.busted_moments.client.framework.config.annotations.Tooltip
import com.busted_moments.client.framework.config.entries.value.Value
import com.busted_moments.client.framework.features.Feature
import com.busted_moments.client.framework.render.TextRenderer
import com.busted_moments.client.framework.render.Texture
import com.busted_moments.client.framework.render.Textures
import com.busted_moments.client.framework.render.helpers.Context
import com.busted_moments.client.framework.render.line
import com.busted_moments.client.framework.render.text
import com.busted_moments.client.framework.render.texture
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.models.territories.timers.TimerModel
import com.mojang.blaze3d.vertex.PoseStack
import com.wynntils.core.components.Models
import com.wynntils.services.map.pois.TerritoryPoi
import com.wynntils.services.map.type.TerritoryDefenseFilterType
import com.wynntils.utils.colors.CommonColors
import com.wynntils.utils.colors.CustomColor
import com.wynntils.utils.render.MapRenderer
import com.wynntils.utils.render.buffered.BufferedRenderUtils
import me.shedaniel.clothconfig2.impl.EasingMethod
import net.essentuan.esl.color.Color
import net.essentuan.esl.time.duration.minutes
import net.essentuan.esl.time.duration.ms
import net.essentuan.esl.time.duration.seconds
import net.essentuan.esl.time.extensions.timeSince
import net.essentuan.esl.tuples.numbers.FloatPair
import net.minecraft.client.renderer.MultiBufferSource
import kotlin.math.ceil
import kotlin.math.floor


const val LINE_HEIGHT = 10f
const val PROD_SIZE = 10.5f

@Category("War")
object GuildMapImprovementsFeature : Feature() {
    @Value("Connection Color", alpha = true)
    var connectionColor: Color = CustomColor(191, 191, 191).esl

    @Value("Connection Thickness", floatMin = 0f)
    var connectionThickness: Float = 0.5f

    @Value("Opacity", floatMin = 0f, floatMax = 1f)
    var opacity: Float = 0.3f

    @Value("Outline Thickness", floatMin = 0f)
    var outlineThickness: Float = 1.0f

    @Value("Show production")
    var showProd: Boolean = true

    @Value("Production Cutoff")
    var prodCutoff: Float = 0.25f

    @Value("Label cutoff")
    var labelCutoff: Float = 0.15f

    @Value("Show timers")
    var showTimers: Boolean = true

    @Value("Show territory when hovered")
    var showTerritory: Boolean = true

    @Value("Use cooldown for color")
    @Tooltip(["Will change the fill of a territory based on how long until its off cooldown"])
    var cooldown: Boolean = true
}

class RenderDetails(
    val poi: TerritoryPoi,
    mapCenterX: Float,
    mapCenterZ: Float,
    centerX: Float,
    centerZ: Float,
    resourceMode: Boolean,
    private val scale: Float
) {
    private val territory: Territory = TerritoryList[poi.name]!!

    private val label: CustomColor
    private val outline: List<CustomColor>
    private val background: List<CustomColor>

    init {
        when {
            resourceMode -> {
                outline = poi.territoryInfo.resourceColors
                label = outline[0]
                background = poi.territoryInfo.resourceColors
            }

            GuildMapImprovementsFeature.cooldown && territory.acquired.timeSince() < 10.minutes -> {
                label = territory.owner.color.wynntils

                val held = territory.acquired.timeSince()

                background = listOf(
                    CustomColor.fromHSV(
                        (floor(122.0 * (held / 10.minutes).toSeconds()) / 360f).toFloat(),
                        0.75f,
                        1f,
                        1f
                    )
                )

                if (held < 590.seconds)
                    outline = listOf(label)
                else {
                    val (h, s, l) = label.esl.asHsl()
                    val delta = 1 - l

                    outline = listOf(
                        Color(
                            hue = h,
                            saturation = s,
                            luminance = l + (EasingMethod.EasingMethodImpl.SINE.apply((held.toSeconds() * 2) % 2) * delta).toFloat()
                        ).wynntils
                    )
                }
            }

            else -> listOf(territory.owner.color.wynntils).also {
                label = it[0]
                background = it
                outline = it
            }
        }
    }

    private val size = FloatPair(
        poi.getWidth(scale, 1f).toFloat(),
        poi.getHeight(scale, 1f).toFloat()
    )

    val width: Float
        get() = size.first

    val height: Float
        get() = size.second

    private val pos = FloatPair(
        MapRenderer.getRenderX(
            poi,
            mapCenterX,
            centerX,
            scale
        ) - width / 2f,
        MapRenderer.getRenderZ(
            poi,
            mapCenterZ,
            centerZ,
            scale
        ) - height / 2f
    )

    val x: Float
        get() = pos.first

    private val z: Float
        get() = pos.second

    private val center = pos + (size / 2)

    private val centerX: Float
        get() = center.first

    private val centerZ: Float
        get() = center.second

    fun render(poseStack: PoseStack, bufferSource: MultiBufferSource) {
        BufferedRenderUtils.drawMulticoloredRect(
            poseStack,
            bufferSource,
            background.map { it.withAlpha(GuildMapImprovementsFeature.opacity) },
            x,
            z,
            0f,
            width,
            height
        )

        BufferedRenderUtils.drawMulticoloredRectBorders(
            poseStack,
            bufferSource,
            outline,
            x,
            z,
            0f,
            width,
            height,
            GuildMapImprovementsFeature.outlineThickness,
            0f
        )
    }

    fun renderLabel(
        context: Context,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource.BufferSource,
        hovered: Boolean
    ) {
        if (poi.territoryInfo.isHeadquarters)
            Textures.GUILD_HEADQUARTERS.texture.render(
                poseStack,
                bufferSource,
                centerX - Textures.GUILD_HEADQUARTERS.width() / 2f,
                centerZ - Textures.GUILD_HEADQUARTERS.height() / 2f
            )
        else if (scale > GuildMapImprovementsFeature.labelCutoff) {
            val icons: List<Texture>
            val lines: Int

            if (GuildMapImprovementsFeature.showProd && scale > GuildMapImprovementsFeature.prodCutoff) {
                icons = territory.resources.entries.flatMap { (resource, storage) ->
                    when (resource) {
                        Territory.Resource.EMERALDS ->
                            if (storage.base > 9000)
                                listOf(Texture.EMERALD)
                            else
                                emptyList()

                        Territory.Resource.ORE ->
                            (0..<(ceil(storage.base / 3600.0).toInt())).flatMap {
                                listOf(Texture.ORE)
                            }

                        Territory.Resource.WOOD ->
                            (0..<(ceil(storage.base / 3600.0).toInt())).flatMap {
                                listOf(Texture.WOOD)
                            }

                        Territory.Resource.FISH ->
                            (0..<(ceil(storage.base / 3600.0).toInt())).flatMap {
                                listOf(Texture.FISH)
                            }

                        Territory.Resource.CROP ->
                            (0..<(ceil(storage.base / 3600.0).toInt())).flatMap {
                                listOf(Texture.CROP)
                            }
                    }
                }

                if (icons.isEmpty())
                    lines = 0
                else
                    lines = (icons.size * LINE_HEIGHT).let { size ->
                        if (size < width)
                            1
                        else
                            ceil(icons.size / 2f).toInt()
                    }
            } else {
                icons = emptyList()
                lines = 0
            }

            val startZ: Float = centerZ - if (lines == 0)
                0f
            else
                ((lines + 1) * LINE_HEIGHT) / 2f

            TextRenderer.split(Text(poi.territoryProfile.guildPrefix, label)).also {
                context.text(
                    it,
                    centerX - (it.width / 2f),
                    if (startZ == centerZ) startZ - it.height / 2f else startZ
                )
            }

            if (lines == 1) {
                val startX = centerX - ((icons.size * PROD_SIZE) / 2f)

                for (i in icons.indices) {
                    poseStack.pushPose()
                    poseStack.translate(
                        startX + (PROD_SIZE * i).toDouble(),
                        (startZ + LINE_HEIGHT).toDouble(),
                        0.0
                    )

                    poseStack.scale(1.4f, 1.4f, 0f)

                    icons[i].render(
                        poseStack,
                        bufferSource,
                        0f,
                        0f,
                    )

                    poseStack.popPose()
                }
            } else {
                for (i in icons.indices step 2) {
                    val first = icons[i]
                    val second = icons.getOrNull(i + 1)

                    val startX = centerX - if (second == null)
                        PROD_SIZE / 2f
                    else
                        (PROD_SIZE * 2) / 2f

                    val z = (startZ + (LINE_HEIGHT * (i / 2f + 1))).toDouble()

                    poseStack.pushPose()
                    poseStack.translate(
                        startX.toDouble(),
                        z,
                        0.0
                    )

                    poseStack.scale(1.4f, 1.4f, 0f)

                    first.render(
                        poseStack,
                        bufferSource,
                        0f,
                        0f,
                    )

                    if (second != null) {
                        poseStack.translate(PROD_SIZE / 1.4, 0.0, 0.0)
                        second.render(
                            poseStack,
                            bufferSource,
                            0f,
                            0f
                        )
                    }

                    poseStack.popPose()
                }
            }
        }

        if (hovered && GuildMapImprovementsFeature.showTerritory) {
            TextRenderer.split(Text(poi.name, CommonColors.WHITE)).also {
                context.text(
                    it,
                    centerX - (it.width / 2f),
                    z
                )
            }
        }

        if (GuildMapImprovementsFeature.showTimers) {
            TimerModel[poi.territoryProfile.name]
                .asSequence()
                .filter { it.remaining > 10.ms }
                .minByOrNull { it.remaining }
                ?.let {
                    TextRenderer.split(Text(it.timerString, CommonColors.WHITE)).also {
                        context.text(
                            it,
                            centerX - (it.width / 2f),
                            z + (height - it.height)
                        )
                    }
                }
        }
    }
}

class Link(
    from: String,
    to: String,
    private val poi: TerritoryPoi
) {
    val from: String
    val to: String

    init {
        if (from < to) {
            this.from = from
            this.to = to
        } else {
            this.from = to
            this.to = from
        }
    }

    fun render(
        ctx: Context,
        mapCenterX: Float,
        mapCenterZ: Float,
        centerX: Float,
        centerZ: Float,
        zoom: Float,
        filterLevel: Int,
        filterType: TerritoryDefenseFilterType?
    ) {
        val from = poi
        val to = Models.Territory.getTerritoryPoiFromAdvancement(this.to) ?: return

        when(filterType) {
            null -> Unit

            TerritoryDefenseFilterType.DEFAULT -> {
                if (to.territoryInfo.defences.level != filterLevel)
                    return
            }

            TerritoryDefenseFilterType.HIGHER -> {
                if (to.territoryInfo.defences.level < filterLevel)
                    return
            }

            TerritoryDefenseFilterType.LOWER -> {
                if (to.territoryInfo.defences.level > filterLevel)
                    return
            }
        }

        val fromX = MapRenderer.getRenderX(from, mapCenterX, centerX, zoom);
        val fromZ = MapRenderer.getRenderZ(from, mapCenterZ, centerZ, zoom);
        val toX = MapRenderer.getRenderX(to, mapCenterX, centerX, zoom);
        val toZ = MapRenderer.getRenderZ(to, mapCenterZ, centerZ, zoom);

        ctx.line(
            GuildMapImprovementsFeature.connectionColor,
            fromX,
            fromZ,
            toX,
            toZ,
            GuildMapImprovementsFeature.connectionThickness
        )
    }
}