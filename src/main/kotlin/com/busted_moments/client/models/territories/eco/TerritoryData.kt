package com.busted_moments.client.models.territories.eco

import com.busted_moments.buster.api.GuildType
import com.busted_moments.buster.api.Territory
import com.busted_moments.buster.api.Territory.Rating
import com.busted_moments.buster.api.Territory.Resource
import com.busted_moments.buster.api.Territory.Storage
import com.busted_moments.client.models.territories.TerritoryModel
import net.essentuan.esl.delegates.final
import net.minecraft.world.item.ItemStack

data class TerritoryData(
    val item: ItemStack,
    private val economy: Economy,
    private val territory: Territory,
    override val hq: Boolean,
    override val resources: Map<Resource, Storages>,
    val upgrades: Map<Upgrade, Pair<Int, UpgradeLevel>>,
    val treasuryBonus: Double,
) : Territory by territory {
    var ignored: Boolean by final()
        private set

    val acronym: String = TerritoryModel.getAcronym(name)

    override var defense: Rating = Rating.VERY_LOW
        private set

    override val owner: GuildType
        get() = economy.guild

    var route: Route? = null
        internal set

    internal var absoluteRoute: Route? = null

    val distance: Int
        get() = absoluteRoute?.size ?: -1

    fun ready() {
        ignored = route == null

        var total = 0f
        var multiplier = 1f

        if (hq) {
            total+= economy.externals * 4
            multiplier = 1.4f
        }

        if (upgrades[Upgrade.TOWER_AURA] == null)
            total-= 5
        else
            total+= upgrades[Upgrade.TOWER_AURA]!!.first * multiplier

        if (upgrades[Upgrade.TOWER_VOLLEY] == null)
            total-= 3
        else
            total+= upgrades[Upgrade.TOWER_VOLLEY]!!.first * multiplier

        total+= (upgrades[Upgrade.DAMAGE]?.first ?: 0) * multiplier
        total+= (upgrades[Upgrade.ATTACK]?.first ?: 0) * multiplier
        total+= (upgrades[Upgrade.HEALTH]?.first ?: 0) * multiplier
        total+= (upgrades[Upgrade.DEFENCE]?.first ?: 0) * multiplier

        defense = when {
            total >= 41 -> Rating.VERY_HIGH
            total >= 23 -> Rating.HIGH
            total >= 11 -> Rating.MEDIUM
            total >= -2 -> Rating.LOW
            else -> Rating.VERY_LOW
        }

        if (hq)
            defense = Rating.entries[(defense.ordinal + 1).coerceIn(Rating.entries.indices)]
    }

    data class Storages(
        override val base: Int,
        override var capacity: Int = 0,
        override var production: Int = 0,
        override var stored: Int = 0,
        var cost: Long = 0
    ) : Storage
}