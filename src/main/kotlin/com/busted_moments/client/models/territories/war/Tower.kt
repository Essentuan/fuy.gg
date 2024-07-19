package com.busted_moments.client.models.territories.war

import com.busted_moments.client.framework.events.post
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.text.Text.matches
import com.busted_moments.client.framework.text.get
import com.busted_moments.client.framework.util.Numbers.escapeCommas
import com.busted_moments.client.framework.util.Numbers.toCommaString
import com.busted_moments.client.models.territories.war.events.WarEvent
import com.wynntils.core.text.StyledText
import net.essentuan.esl.json.Json
import java.util.Date
import java.util.regex.Pattern
import kotlin.math.abs
import kotlin.math.floor

private val TOWER_REGEX: Pattern =
    Pattern.compile("\\[(?<guild>.+)\\] (?<territory>.+) Tower - . (?<health>.+) \\((?<defense>.+)%\\) - .{1,2} (?<damagemin>.+)-(?<damagemax>.+) \\((?<attackspeed>.+)x\\)")

data class Tower(
    private val updates: MutableList<Update> = mutableListOf()
) : Collection<Tower.Update> by updates {
    val initial: Stats
        get() = updates.first().before

    val stats: Stats
        get() = updates.last().after

    fun update(
        war: War,
        stats: Stats,
        at: Date
    ) {
        val previous = this@Tower.stats
        if (previous == stats)
            return

        Update(
            at,
            previous,
            stats
        ).also {
            updates+= it
            WarEvent.TowerUpdate(war, it).post()
        }
    }

    data class Stats(
        val health: Long,
        val defense: Float,
        val damageMin: Int,
        val damageMax: Int,
        val attackSpeed: Float
    ) : Json.Model {
        val ehp: Long
            get() = floor(health / (1f - (defense / 100f))).toLong()

        fun appendTo(builder: Text.Builder) {
            builder.apply {
                +"\u2665 ".darkRed
                +health.toCommaString().escapeCommas().darkRed
                +" (".gray
                +"$defense%".gold
                +") -".gray
                +" \u2620 ".red
                +damageMin.toCommaString().escapeCommas().red
                +"-".red
                +damageMax.toCommaString().escapeCommas().red
                +" (".gray
                +"${attackSpeed}x".aqua
                +")".gray
            }
        }

        companion object {
            operator fun invoke(text: StyledText): Stats? {
                text.matches {
                    TOWER_REGEX { matcher, _ ->
                        return Stats(
                            matcher["health"]!!.toLong(),
                            matcher["defense"]!!.toFloat(),
                            matcher["damagemin"]!!.toInt(),
                            matcher["damagemax"]!!.toInt(),
                            matcher["attackspeed"]!!.toFloat()
                        )
                    }
                }

                return null
            }
        }
    }

    data class Update(
        val at: Date,
        val before: Stats,
        val after: Stats
    ) {
        inline fun <T : Number> difference(stat: (Stats) -> T, extractor: (Double) -> T): T {
            return extractor(
                abs(
                    floor(
                        stat(before).toDouble() - stat(after).toDouble()
                    )
                )
            )
        }
    }
}