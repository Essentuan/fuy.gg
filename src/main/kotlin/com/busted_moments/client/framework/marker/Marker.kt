package com.busted_moments.client.framework.marker

import com.busted_moments.client.framework.render.Textures
import com.busted_moments.client.framework.render.helpers.IContext
import com.busted_moments.client.framework.wynntils.wynntils
import com.busted_moments.client.framework.wynntils.esl
import com.mojang.blaze3d.platform.Window
import com.wynntils.core.components.Models
import com.wynntils.models.marker.type.LocationSupplier
import com.wynntils.models.marker.type.MarkerInfo
import com.wynntils.models.marker.type.MarkerProvider
import com.wynntils.services.map.pois.MarkerPoi
import com.wynntils.services.map.pois.Poi
import com.wynntils.services.map.pois.WaypointPoi
import com.wynntils.utils.colors.CommonColors
import com.wynntils.utils.mc.type.Location
import com.wynntils.utils.mc.type.PoiLocation
import com.wynntils.utils.render.type.TextShadow
import net.essentuan.esl.color.Color
import net.essentuan.esl.reflections.Reflections
import net.essentuan.esl.reflections.extensions.instance
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import java.util.stream.Stream
import kotlin.streams.asStream

typealias IconPoi = MarkerPoi

abstract class Marker<T : Poi>(
    val name: String,
    val position: PoiLocation,
    val icon: Textures,
    val beaconColor: Color? = null,
    val textColor: Color = CommonColors.WHITE.esl,
    val textureColor: Color = CommonColors.WHITE.esl,
    val label: String? = null
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
        var force: Boolean
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
                        it.beaconColor!!.wynntils,
                        it.textColor.wynntils,
                        it.textureColor.wynntils,
                        it.label,
                    )
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
            label: String? = null,
        ) : Marker<IconPoi> = object : Marker<IconPoi>(
            name,
            position,
            icon,
            beaconColor,
            textColor,
            textureColor,
            label,
        ) {
            override val poi: IconPoi
                get() = IconPoi(position, name, icon)

        }
    }
}