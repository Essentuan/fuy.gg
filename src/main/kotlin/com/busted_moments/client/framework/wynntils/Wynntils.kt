package com.busted_moments.client.framework.wynntils

import com.busted_moments.buster.api.Territory
import com.busted_moments.client.framework.Objenesis
import com.busted_moments.mixin.accessors.TerritoryModelAccessor
import com.wynntils.core.components.Managers
import com.wynntils.core.text.StyledText
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent
import com.wynntils.models.territories.TerritoryInfo
import com.wynntils.models.territories.TerritoryModel
import com.wynntils.models.territories.profile.TerritoryProfile
import com.wynntils.models.territories.type.GuildResource
import com.wynntils.models.territories.type.GuildResourceValues
import com.wynntils.services.map.pois.TerritoryPoi
import com.wynntils.utils.colors.CustomColor
import com.wynntils.utils.mc.McUtils.mc
import com.wynntils.utils.type.CappedValue
import kotlinx.serialization.StringFormat
import net.essentuan.esl.color.Color
import net.minecraft.client.Minecraft
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.BuiltInRegistries

val CustomColor.esl: Color
    get() = this as Color

val Color.wynntils: CustomColor
    get() {
        if (this is CustomColor)
            return this

        return CustomColor(red, green, blue, alpha)
    }

val Territory.Resource.wynntils: GuildResource
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

val Territory.Rating.wynntils: GuildResourceValues
    get() = when(this) {
        Territory.Rating.VERY_LOW -> GuildResourceValues.VERY_LOW
        Territory.Rating.LOW -> GuildResourceValues.LOW
        Territory.Rating.MEDIUM -> GuildResourceValues.MEDIUM
        Territory.Rating.HIGH -> GuildResourceValues.HIGH
        Territory.Rating.VERY_HIGH -> GuildResourceValues.VERY_HIGH
    }

val Territory.Rating.defenseColor: Color
    get() = Color(wynntils.defenceColor.color!!, alpha = false)

val Territory.Rating.treasuryColor: Color
    get() = Color(wynntils.treasuryColor.color!!, alpha = false)

val GuildResourceValues.buster: Territory.Rating
    get() = when(this) {
        GuildResourceValues.NONE, GuildResourceValues.VERY_LOW -> Territory.Rating.VERY_LOW
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

var ChatMessageReceivedEvent.message: StyledText
    get() = styledText!!
    set(value) = setMessage(value)

internal interface MutableTerritoryPoi {
    var guildName: String
    var guildPrefix: String
    var storage: MutableMap<GuildResource, CappedValue>
    var generators: MutableMap<GuildResource, Int>
    var tradingRoutes: MutableList<String>
    var treasury: GuildResourceValues
    var defences: GuildResourceValues
    var headquarters: Boolean

    fun generateResourceColors();
}

val Minecraft.registry: RegistryAccess
    get() = mc().level?.registryAccess() ?: RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)

object Ticks {
    fun schedule(ticks: Int = 0, block: Runnable) =
        Managers.TickScheduler.scheduleLater(block, ticks)
}