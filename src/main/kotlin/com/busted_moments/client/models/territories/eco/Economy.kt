package com.busted_moments.client.models.territories.eco

import com.busted_moments.buster.api.GuildType
import com.busted_moments.buster.api.Territory
import com.busted_moments.client.Patterns.PRODUCTION
import com.busted_moments.client.Patterns.STORAGE
import com.busted_moments.client.Patterns.TREASURY
import com.busted_moments.client.Patterns.UPGRADE
import com.busted_moments.client.buster.GuildList
import com.busted_moments.client.buster.TerritoryList
import com.busted_moments.client.framework.wynntils.buster
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.text.Text.matches
import com.busted_moments.client.framework.text.get
import com.busted_moments.client.framework.util.Items.tooltip
import com.wynntils.core.components.Models
import com.wynntils.models.territories.type.GuildResource
import com.wynntils.models.territories.type.TerritoryUpgrade
import net.essentuan.esl.collections.enumMapOf
import net.minecraft.world.item.ItemStack

typealias Upgrade = TerritoryUpgrade
typealias UpgradeLevel = TerritoryUpgrade.Level

class Economy(
    private val territories: MutableMap<String, TerritoryData> = mutableMapOf()
) : Map<String, TerritoryData> by territories {
    var guild: GuildType = GuildList.NONE
        private set
    var hq: TerritoryData? = null
        private set

    lateinit var total: Map<Territory.Resource, TerritoryData.Storages>
        private set

    var externals: Int = 0

    constructor(items: Sequence<ItemStack>) : this() {
        guild = GuildList.firstOrNull { it.name == Models.Guild.guildName } ?: GuildList.NONE

        for (item in items) {
            val territory = parse(item) ?: continue
            territories[territory.name] = territory

            if (territory.hq)
                hq = territory
        }

        Route.visit(this, true)
        Route.visit(this, false)

        for (territory in values) {
            if (!territory.hq && territory.distance <= 3)
                externals++
        }

        val total = enumMapOf<Territory.Resource, TerritoryData.Storages>()

        for (resource in Territory.Resource.entries)
            total[resource] = TerritoryData.Storages(-1)

        for (territory in values) {
            territory.ready()

            for ((resource, storage) in territory.resources) {
                total[resource]!!.also {
                    if (!territory.ignored) {
                        it.capacity += storage.capacity
                        it.production += storage.production
                        it.stored += storage.stored
                    }

                    it.cost += storage.cost
                }
            }
        }

        this.total = total
    }

    private fun parse(stack: ItemStack): TerritoryData? {
        val name = nameOf(stack)
        val territory = TerritoryList[name] ?: return null

        val resources = enumMapOf<Territory.Resource, TerritoryData.Storages>()

        for ((resource, storage) in territory.resources)
            resources[resource] = TerritoryData.Storages(storage.base)

        val upgrades = enumMapOf<Upgrade, Pair<Int, UpgradeLevel>>()

        var treasury = 0.0

        for (line in stack.tooltip) {
            line.normalized.matches {
                UPGRADE { matcher, _ ->
                    val type = Upgrade.fromName(matcher["upgrade"]) ?: return@UPGRADE
                    val level = matcher["level"]!!.toInt()
                    val upgrade = type.levels[matcher["level"]!!.toInt()]

                    upgrades[type] = level to upgrade

                    resources[type.costResource.buster]!!.cost += upgrade.cost

                    return@matches
                }

                STORAGE { matcher, _ ->
                    resources[
                        GuildResource.fromSymbol(matcher["type"] ?: "").buster
                    ]!!.let {
                        it.stored = matcher["stored"]!!.toInt()
                        it.capacity = matcher["capacity"]!!.toInt()
                    }

                    return@matches
                }

                PRODUCTION { matcher, _ ->
                    resources[GuildResource.fromName(matcher["resource"]).buster]!!.production =
                        matcher["amount"]!!.toInt()

                    return@matches
                }

                TREASURY { matcher, _ ->
                    treasury = matcher["treasury"]!!.toDouble()
                    return@matches
                }
            }
        }

        return TerritoryData(
            stack,
            this,
            territory,
            stack.hoverName.string.contains("(HQ)"),
            resources,
            upgrades,
            treasury
        )
    }

    companion object {
        fun nameOf(stack: ItemStack): String {
            val base: String = Text(stack.hoverName).normalized.stringWithoutFormatting

            return base
                .replace(" (HQ)", "")
                .replace("[!] ", "")
                .trim()
        }

        fun isTerritory(stack: ItemStack): Boolean {
            return stack.tooltip.any {
                val text: String = it.normalized.stringWithoutFormatting

                when {
                    UPGRADE.matcher(text).matches() -> true
                    STORAGE.matcher(text).matches() -> true
                    PRODUCTION.matcher(text).matches() -> true
                    TREASURY.matcher(text).matches() -> true
                    else -> false
                }
            }
        }
    }
}