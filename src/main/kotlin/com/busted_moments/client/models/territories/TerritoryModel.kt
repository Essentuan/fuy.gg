package com.busted_moments.client.models.territories

import com.busted_moments.buster.api.Territory
import com.busted_moments.client.buster.TerritoryList
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.models.territories.TerritoryModel.clean
import com.wynntils.mc.event.TickEvent
import com.wynntils.utils.mc.McUtils.player
import java.util.regex.Pattern

private val CLEANER = Pattern.compile("[^\\w\\s]")

object TerritoryModel {
    var inside: Territory? = null
        private set

    @Subscribe
    fun TickEvent.on() {
        inside = TerritoryList.firstOrNull {
            it.location.contains(
                player().x.toInt(),
                player().z.toInt()
            )
        }
    }

    fun Territory.Location.contains(x: Int, z: Int): Boolean {
        return start.x <= x && end.x >= x && start.z <= z && end.z >= z
    }

    private fun String.clean(): String =
        CLEANER.matcher(this).replaceAll("").lowercase()

    fun getAcronym(string: String) =
        string.split(' ', '-', ignoreCase = true)
        .asSequence()
        .map { if (it.isBlank()) "" else it[0] }
        .joinToString(separator = "")

    fun matches(input: String, search: String): Boolean {
        if (search.isBlank())
            return true

        val cleaned = input.clean().also { clean ->
            val acronym = clean.split(' ', '-', ignoreCase = true)
                .asSequence()
                .map { if (it.isBlank()) "" else it[0] }
                .joinToString(separator = "")

            if (acronym.startsWith(search, ignoreCase = true))
                return true
        }.split(' ')

        val parts = search.clean().split(' ')

        if (parts.size > cleaned.size)
            return false

        var offset: Int = -1

        for (i in cleaned.indices) {
            if (offset == -1 && cleaned[i].contains(parts[0], ignoreCase = true))
                offset = i

            if (offset == -1)
                continue

            val index = i - offset
            if (index >= parts.size)
                break

            if (!cleaned[i].contains(parts[index], ignoreCase = true))
                offset = -1
        }

        return offset != -1 && (parts.size + offset) <= cleaned.size
    }
}