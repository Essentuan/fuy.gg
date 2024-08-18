package com.busted_moments.client.screens.territories

import com.busted_moments.buster.api.Territory
import com.busted_moments.client.framework.keybind.Inputs
import com.busted_moments.client.framework.keybind.Key
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.text.esl
import com.busted_moments.client.framework.wynntils.defenseColor
import com.busted_moments.client.framework.wynntils.esl
import com.busted_moments.client.framework.wynntils.treasuryColor
import com.busted_moments.client.framework.wynntils.wynntils
import com.busted_moments.client.models.territories.eco.TerritoryData
import com.busted_moments.client.models.territories.eco.Upgrade
import com.wynntils.core.text.StyledText
import com.wynntils.utils.colors.CommonColors
import com.wynntils.utils.colors.CustomColor
import net.essentuan.esl.color.Color
import net.minecraft.ChatFormatting

object QuickAccess : List<QuickAccess.Option> by listOf(
    Option.Title("Quick Access"),
    Option.Cycling(
        "Defense",
        options = Territory.Rating.entries.toTypedArray(),
        painter = { it.defenseColor },
        extractor = { it.print() },
        predicate = { defense == it }
    ),
    Option.Cycling(
        "Treasury",
        options = Territory.Rating.entries.toTypedArray(),
        painter = { it.treasuryColor },
        extractor = { it.print() },
        predicate = { treasury == it }
    ),
    Option.Cycling(
        "Produces",
        options = Territory.Resource.entries.toTypedArray(),
        painter = { it.wynntils.color.esl },
        extractor = { it.print() },
        predicate = predicate@{
            when (it) {
                Territory.Resource.EMERALDS -> (resources[it]?.base ?: return@predicate false) > 9000
                else -> (resources[it]?.base ?: return@predicate false) > 0
            }
        }
    ),
    Option.Title("Filters"),
    Option.Predicate("No Route", ChatFormatting.RED.esl, 0) {
        it.route == null
    },
    Option.Predicate(Upgrade.TOWER_MULTI_ATTACKS, ChatFormatting.AQUA.esl, 1),
    Option.Predicate(Upgrade.EMERALD_SEEKING, ChatFormatting.GREEN.esl, 1),
    Option.Predicate(Upgrade.TOME_SEEKING, ChatFormatting.BLUE.esl, 1),
    Option.Predicate(Upgrade.MOB_EXPERIENCE, ChatFormatting.YELLOW.esl, 1),
    Option.Predicate(Upgrade.MOB_DAMAGE, ChatFormatting.LIGHT_PURPLE.esl, 1),
    Option.Predicate(Upgrade.GATHERING_EXPERIENCE, ChatFormatting.DARK_PURPLE.esl, 1),
    Option.Spacer,
    Strict
) {
    const val WIDTH = 150f

    fun reset() {
        forEach(Option::reset)
    }

    interface Option {
        val color: Color
        val group: Int
        val required: Boolean
            get() = false

        fun reset()

        fun click(button: Int): Boolean

        fun Text.Builder.append(count: Int, hovered: Boolean)

        fun test(territory: TerritoryData): Boolean

        data class Title(private val text: StyledText) : Option {
            constructor(text: String) : this(Text(text))

            override val color: Color
                get() = CustomColor.NONE.esl

            override val group: Int
                get() = -1

            override fun reset() = Unit

            override fun click(button: Int): Boolean = false

            override fun Text.Builder.append(count: Int, hovered: Boolean) {
                center(offset = 2.5f, maxWidth = WIDTH.toInt()) {
                    +text
                }
            }

            override fun test(territory: TerritoryData): Boolean =
                false
        }

        object Spacer : Option {
            override val color: Color
                get() = CustomColor.NONE.esl
            override val group: Int
                get() = -1

            override fun reset() = Unit

            override fun click(button: Int): Boolean =
                false

            override fun Text.Builder.append(count: Int, hovered: Boolean) =
                Unit

            override fun test(territory: TerritoryData): Boolean =
                false
        }

        class Cycling<T : Any>(
            private val name: String,
            private vararg val options: T,
            private val painter: (T) -> Color,
            private val extractor: (T) -> String = Any::toString,
            private val predicate: TerritoryData.(T) -> Boolean
        ) : Option {
            private val current: T?
                get() = if (index == -1)
                    null
                else
                    options[index]


            private var index: Int = -1
                set(value) {
                    field = when {
                        value >= options.size -> -1
                        value == -1 -> -1
                        value < -1 -> options.lastIndex
                        else -> value
                    }
                }

            override val color: Color
                get() {
                    return painter(current ?: return CommonColors.DARK_GRAY.esl)
                }
            override val group: Int
                get() = -1

            override val required: Boolean
                get() = index != -1

            override fun reset() {
                index = -1
            }

            override fun click(button: Int): Boolean {
                when (button) {
                    0 -> index++
                    1 -> index--
                    2 -> index = -1
                    else -> return false
                }

                return true
            }

            override fun Text.Builder.append(count: Int, hovered: Boolean) {
                if (current == null) {
                    +"$name: -".darkGray.also { it.isBold = hovered }
                    return
                }

                +name.white.also { it.isBold = hovered }
                +": ".white.also { it.isBold = hovered }
                +extractor(current!!).color(color).also { it.isBold = hovered }
            }

            override fun test(territory: TerritoryData): Boolean {
                return predicate(territory, current ?: return false)
            }
        }

        data class Predicate(
            private val name: String,
            override val color: Color,
            override val group: Int,
            var enabled: Boolean = true,
            private val predicate: (TerritoryData) -> Boolean
        ) : Option {
            constructor(upgrade: Upgrade, color: Color, group: Int) : this(
                upgrade.getName(),
                color,
                group,
                enabled = true,
                predicate@{ (it.upgrades[upgrade]?.first ?: return@predicate false) != 0 }
            )

            override fun reset() {
                enabled = true
            }

            override fun click(button: Int): Boolean {
                if (Key.isDown(Inputs.KEY_LSHIFT)) {
                    enabled = true

                    for (option in QuickAccess) {
                        if (option is Predicate && option !== this)
                            option.enabled = false
                    }
                } else {
                    enabled = !enabled
                }

                return true
            }

            override fun Text.Builder.append(count: Int, hovered: Boolean) {
                +"[$count] $name".color(
                    if (enabled)
                        color
                    else
                        ChatFormatting.DARK_GRAY.esl
                ).also {
                    if (hovered)
                        it.isBold = true

                    if (!enabled)
                        it.isStrikethrough = true
                }
            }

            override fun test(territory: TerritoryData): Boolean =
                predicate(territory)

        }
    }

    object Strict : Option {
        var enabled: Boolean = false

        override val color: Color
            get() = ChatFormatting.WHITE.esl
        override val group: Int
            get() = -1

        override fun reset() {
            enabled = false
        }

        override fun click(button: Int): Boolean {
            enabled = !enabled

            return true
        }

        override fun Text.Builder.append(count: Int, hovered: Boolean) {
            +"Strict Mode".color(
                if (enabled)
                    color
                else
                    ChatFormatting.DARK_GRAY.esl
            ).also {
                if (hovered)
                    it.isBold = true

                if (!enabled)
                    it.isStrikethrough = true
            }

        }

        override fun test(territory: TerritoryData): Boolean =
            false
    }
}