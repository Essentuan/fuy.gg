package com.busted_moments.client.framework.artemis

import com.busted_moments.buster.api.Territory
import com.busted_moments.client.framework.Objenesis
import com.busted_moments.mixin.accessors.TerritoryModelAccessor
import com.wynntils.core.components.Managers
import com.wynntils.models.territories.TerritoryInfo
import com.wynntils.models.territories.TerritoryModel
import com.wynntils.models.territories.profile.TerritoryProfile
import com.wynntils.models.territories.type.GuildResource
import com.wynntils.models.territories.type.GuildResourceValues
import com.wynntils.services.map.pois.TerritoryPoi
import com.wynntils.utils.colors.CustomColor
import net.essentuan.esl.color.Color

val CustomColor.esl: Color
    get() = this as Color

val Color.artemis: CustomColor
    get() {
        if (this is CustomColor)
            return this

        return CustomColor(red, green, blue, alpha)
    }

val Territory.Resource.artemis: GuildResource
    get() = when(this) {
        Territory.Resource.EMERALDS -> GuildResource.EMERALDS
        Territory.Resource.ORE -> GuildResource.ORE
        Territory.Resource.WOOD -> GuildResource.WOOD
        Territory.Resource.FISH -> GuildResource.FISH
        Territory.Resource.CROP -> GuildResource.CROPS
    }

val GuildResource.buster: Territory.Resource
    get() = when(this) {
        GuildResource.EMERALDS -> Territory.Resource.EMERALDS
        GuildResource.ORE -> Territory.Resource.ORE
        GuildResource.WOOD -> Territory.Resource.WOOD
        GuildResource.FISH -> Territory.Resource.FISH
        GuildResource.CROPS -> Territory.Resource.CROP
    }

val Territory.Rating.artemis: GuildResourceValues
    get() = when(this) {
        Territory.Rating.VERY_LOW -> GuildResourceValues.VERY_LOW
        Territory.Rating.LOW -> GuildResourceValues.LOW
        Territory.Rating.MEDIUM -> GuildResourceValues.MEDIUM
        Territory.Rating.HIGH -> GuildResourceValues.HIGH
        Territory.Rating.VERY_HIGH -> GuildResourceValues.VERY_HIGH
    }

val Territory.Rating.defenseColor: Color
    get() = Color(artemis.defenceColor.color!!, alpha = false)

val Territory.Rating.treasuryColor: Color
    get() = Color(artemis.treasuryColor.color!!, alpha = false)

val GuildResourceValues.buster: Territory.Rating
    get() = when(this) {
        GuildResourceValues.VERY_LOW -> Territory.Rating.VERY_LOW
        GuildResourceValues.LOW -> Territory.Rating.LOW
        GuildResourceValues.MEDIUM -> Territory.Rating.MEDIUM
        GuildResourceValues.HIGH -> Territory.Rating.HIGH
        GuildResourceValues.VERY_HIGH -> Territory.Rating.VERY_HIGH
    }

var TerritoryModel.territoryProfileMap: MutableMap<String, TerritoryProfile>
    get() = (this as TerritoryModelAccessor).territoryProfileMap
    set(value) {
        (this as TerritoryModelAccessor).territoryProfileMap = value
    }

val TerritoryModel.territoryPoiMap: MutableMap<String, TerritoryPoi>
    get() = (this as TerritoryModelAccessor).territoryPoiMap

var TerritoryModel.allTerritoryPois: MutableSet<TerritoryPoi>
    get() = (this as TerritoryModelAccessor).allTerritoryPois
    set(value) {
        (this as TerritoryModelAccessor).allTerritoryPois = value
    }

fun Territory.toTerritoryInfo(): TerritoryInfo =
    Objenesis<TerritoryInfo>().also {
        (it as TerritoryCopier).copyOf(this)
    }

internal fun interface TerritoryCopier {
    fun copyOf(territory: Territory)
}

object Ticks {
    fun schedule(ticks: Int = 0, block: Runnable) =
        Managers.TickScheduler.scheduleLater(block, ticks)
}