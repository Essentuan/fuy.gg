package com.busted_moments.client.framework.marker

import com.busted_moments.client.framework.artemis.artemis
import com.busted_moments.client.framework.artemis.esl
import com.busted_moments.client.framework.render.TextRenderer
import com.busted_moments.client.framework.render.Textures
import com.busted_moments.client.framework.render.helpers.IContext
import com.busted_moments.client.framework.render.text
import com.busted_moments.client.framework.render.texture
import com.busted_moments.client.framework.text.Text
import com.mojang.blaze3d.platform.Window
import com.mojang.blaze3d.systems.RenderSystem
import com.wynntils.core.components.Models
import com.wynntils.models.marker.type.LocationSupplier
import com.wynntils.models.marker.type.MarkerInfo
import com.wynntils.models.marker.type.MarkerProvider
import com.wynntils.services.map.pois.MarkerPoi
import com.wynntils.services.map.pois.Poi
import com.wynntils.services.map.pois.WaypointPoi
import com.wynntils.utils.colors.CommonColors
import com.wynntils.utils.colors.CustomColor
import com.wynntils.utils.mc.type.Location
import com.wynntils.utils.mc.type.PoiLocation
import com.wynntils.utils.render.RenderUtils
import com.wynntils.utils.render.buffered.BufferedRenderUtils
import com.wynntils.utils.render.type.HorizontalAlignment
import com.wynntils.utils.render.type.TextShadow
import com.wynntils.utils.render.type.VerticalAlignment
import net.essentuan.esl.color.Color
import net.essentuan.esl.reflections.Reflections
import net.essentuan.esl.reflections.extensions.get
import net.essentuan.esl.reflections.extensions.instance
import net.minecraft.util.parsing.packrat.Term.marker
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import java.util.stream.Stream
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.streams.asStream

typealias IconPoi = MarkerPoi

abstract class Marker<T : Poi>(
    val name: String,
    val position: PoiLocation,
    val icon: Textures,
    val beaconColor: Color? = null,
    val textColor: Color = CommonColors.WHITE.esl,
    val textureColor: Color = CommonColors.WHITE.esl,
    val hasLabel: Boolean = true
) : LocationSupplier {
    abstract val poi: T

    override fun getLocation(): Location =
        Location(
            position.x,
            position.y.orElse(0),
            position.z
        )

    interface Context : IContext {
        val beacons: List<RenderedInfo>
        val maxWaypointDistance: Int
        val scale: Float
        val opacity: Float
        val textStyle: TextShadow
        val dummy: WaypointPoi

        fun getIntersection(position: Vec3, window: Window): Vec2?
    }

    interface Extension {
        var hasLabel: Boolean
    }

    data class RenderedInfo(
        val distance: Double,
        val distanceText: String,
        val markerInfo: MarkerInfo,
        val screenCoordinates: Vec3
    )

    interface Provider<T : Poi> : Iterable<Marker<T>>, MarkerProvider<T> {
        override fun getPois(): Stream<T> =
            asSequence().map { it.poi }.asStream()

        override fun getMarkerInfos(): Stream<MarkerInfo> =
            asSequence()
                .filterNot { it.beaconColor == null }
                .map {
                    MarkerInfo(
                        it.name,
                        it,
                        it.icon,
                        it.beaconColor!!.artemis,
                        it.textColor.artemis,
                        it.textureColor.artemis
                    ).also {
                        it.hasLabel = true
                    }
                }.asStream()

        companion object {
            fun register() {
                Reflections.types
                    .subtypesOf(Provider::class)
                    .map { it.instance }
                    .filterNotNull()
                    .forEach {
                        Models.Marker.registerMarkerProvider(it)
                    }
            }
        }
    }

    companion object {
        fun icon(
            name: String,
            position: PoiLocation,
            icon: Textures,
            beaconColor: Color? = null,
            textColor: Color = CommonColors.WHITE.esl,
            textureColor: Color = CommonColors.WHITE.esl,
            hasLabel: Boolean = true
        ) : Marker<IconPoi> = object : Marker<IconPoi>(
            name,
            position,
            icon,
            beaconColor,
            textColor,
            textureColor,
            hasLabel
        ) {
            override val poi: IconPoi
                get() = IconPoi(position, name, icon)

        }

        fun Context.render() {
            for (marker in beacons) {
                if (maxWaypointDistance != 0 && marker.distance > maxWaypointDistance)
                    continue

                val intersection = getIntersection(marker.screenCoordinates, window)
                marker.markerInfo.textureColor
                    .asFloatArray()
                    .let {
                        RenderSystem.setShaderColor(it[0], it[1], it[2], 1f)
                    }

                if (intersection == null)
                    renderOnScreen(marker)
                else
                    renderOffScreen(marker, intersection)
            }

            buffer.endBatch()
        }

        private fun Context.renderOnScreen(marker: RenderedInfo) {
            val label = TextRenderer.split(
                Text {
                    if (marker.markerInfo.hasLabel) {
                        +marker.markerInfo.name.white
                        newLine()
                    }

                    +marker.distanceText.color(marker.markerInfo.textColor.let {
                        if (it == CustomColor.NONE)
                            CommonColors.WHITE
                        else
                            it
                    }.esl)
                }
            )

            val displayX = marker.screenCoordinates.x.toFloat()
            val displayY = marker.screenCoordinates.y.toFloat()

            val icon = marker.markerInfo.texture

            icon.texture.render(
                pose,
                buffer,
                x = displayX - (scale * (icon.width() / 2f)),
                y = displayY - (scale * (icon.height() + (label.height / 2f) + 3f)),
                width = scale * icon.width().toFloat(),
                height = scale * icon.height().toFloat()
            )

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f)

            BufferedRenderUtils.drawRect(
                pose,
                buffer,
                CommonColors.BLACK.withAlpha(opacity),
                displayX - (scale * ((label.width / 2f) + 2f)),
                displayY - (scale * ((label.height / 2f) + 2f)),
                0f,
                scale * (label.width + 3),
                scale * (label.height + 2)
            )

            text(
                label,
                displayX - (scale * ((label.width / 2f) + 1.5f)),
                displayY - (scale * (label.height /2f)),
                scale * (label.width + 3),
                scale * (label.height + 2),
                HorizontalAlignment.CENTER,
                VerticalAlignment.TOP,
                textStyle,
                scale
            )
        }

        private fun Context.renderOffScreen(marker: RenderedInfo, intersection: Vec2) {
            val displayX = intersection.x
            val displayY = intersection.y

            val icon = marker.markerInfo.texture

            val angle = Math.toDegrees(
                atan2(
                    displayY - (window.guiScaledHeight / 2.0),
                    displayX - (window.guiScaledWidth / 2.0)
                )
            ) + 90f

            val radius = (icon.width() / 2f) + 8f

            val pointerXOffset = radius * cos(((angle - 90) * 3) / 180).toFloat()
            val pointerX = displayX + pointerXOffset
            val pointerYOffset = (radius * sin(((angle - 90) * 3) / 180)).toFloat()
            val pointerY = displayY + pointerYOffset

            icon.texture.render(
                pose,
                buffer,
                x = displayX - ((scale * icon.width()) / 2f) + (pointerXOffset * (1 - scale)),
                y = displayY - ((scale * icon.height()) / 2f) + (pointerYOffset * (1 - scale)),
                width = scale * icon.width().toFloat(),
                height = scale * icon.height().toFloat()
            )

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f)

            pose.pushPose()
            pose.translate(pointerX, pointerY, 0f)
            pose.mulPose(
                Quaternionf().rotateXYZ(
                    0f,
                    0f,
                    Math.toRadians(angle).toFloat()
                )
            )
            pose.translate(-pointerX, -pointerY, 0f)

            dummy.pointerPoi.renderAt(
                pose,
                buffer,
                pointerX,
                pointerY,
                false,
                scale,
                1f,
                50f,
                true
            )

            pose.popPose()
        }
    }
}

var MarkerInfo.hasLabel: Boolean
    get() = (this as Marker.Extension).hasLabel
    set(value) {
        (this as Marker.Extension).hasLabel = value
    }