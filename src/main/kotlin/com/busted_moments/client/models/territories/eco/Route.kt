package com.busted_moments.client.models.territories.eco

import com.busted_moments.buster.api.Territory
import com.busted_moments.client.buster.TerritoryList
import com.wynntils.core.components.Models
import java.util.LinkedList
import java.util.Queue


class Route private constructor(private val route: List<Territory>) : Collection<Territory> by route {
    fun append(territory: Territory): Route {
        val route = route.toMutableList()
        route.add(territory)

        return Route(route)
    }

    override fun iterator(): Iterator<Territory> {
        return route.iterator()
    }

    companion object {
        fun visit(eco: Economy, ideal: Boolean) {
            if (eco.hq == null) return

            eco.hq!!.route = Route(mutableListOf())
            eco.hq!!.absoluteRoute = eco.hq!!.route

            val visited = HashSet<String>()
            val completed = HashSet<String>()

            val queue: Queue<Pair<String, Route>> = LinkedList()
            queue.add(Pair(eco.hq!!.name, eco.hq!!.route!!))

            while (!queue.isEmpty() && eco.size > completed.size) {
                val (name, route) = queue.poll()

                if (name == "Military Base Upper") {
                    name + "1"
                }

                if (visited.contains(name) || (!ideal && name !in eco))
                    continue

                val territory = TerritoryList[name] ?: continue

                val terr = eco[name]
                if (ideal)
                    terr?.absoluteRoute = route
                else
                    terr?.route = route

                if (name in eco)
                    completed.add(name)

                visited.add(name)

                val next = route.append(territory)

                territory.connections.forEach { queue.add(it to next) }
            }
        }
    }
}