package com.busted_moments.client.screens.territories

import com.busted_moments.client.framework.render.elements.TextureElement
import com.busted_moments.client.framework.render.helpers.Percentage.Companion.pct
import com.busted_moments.client.framework.render.screen.Title
import com.busted_moments.client.framework.render.screen.elements.click
import com.busted_moments.client.framework.render.screen.elements.item
import com.busted_moments.client.models.territories.eco.TerritoryData
import com.busted_moments.client.models.territories.eco.TerritoryScanner
import net.essentuan.esl.time.duration.Duration
import net.essentuan.esl.time.duration.ms
import net.essentuan.esl.time.extensions.timeSince
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.Date

private const val CONFIRM_SLOT = 0

@Title("Select Territories")
class SelectTerritoriesScreen(
    container: Int
) : TerritoryScreen<SelectTerritoriesScreen.Scanner>(container, Scanner(container)) {
    override fun TextureElement<Context>.renderSidebar(ctx: Context) {
        item {
            x = this@renderSidebar.x + ((50.pct of this@renderSidebar.width) - (50.pct of this.width)) - 0.5f
            y = this@renderSidebar.y + 5

            stack = scanner.contents.getOrNull(CONFIRM_SLOT) ?: return@item false
            tooltip = true

            click { _, _, _ ->
                scanner.disable()
                scanner.click(CONFIRM_SLOT, sound = SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON)

                true
            }

            true
        }

        item {
            x = this@renderSidebar.x + ((50.pct of this@renderSidebar.width) - (50.pct of this.width)) - 0.5f
            y = (50.pct of ctx.window.guiScaledHeight) - (50.pct of this.height)

            stack = scanner.contents.getOrNull(BACK_SLOT) ?: return@item false
            tooltip = true

            click { _, _, _ ->
                scanner.enable()
                scanner.click(BACK_SLOT, sound = SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON)

                true
            }

            true
        }
    }

    override fun select(territory: TerritoryData) =
        scanner.select(territory)

    class Scanner(container: Int) : TerritoryScanner(container) {
        private val selected = mutableSetOf<String>()
        private val territories = mutableMapOf<String, Item>()

        private var ready: Boolean = true
        private var lastClick: Date = Date()

        fun select(territory: TerritoryData) {
            selected.add(territory.name)

            enable()
        }

        override fun peek(items: Map<Int, List<Slot>>) {
            for (slot in items.values.flatten()) {
                val previous = territories.put(slot.territory, slot.stack.item) ?: continue

                if (previous != slot.stack.item) {
                    ready = true
                }
            }
        }

        override val delay: Duration
            get() = 0.ms

        override suspend fun process(territory: String, stack: ItemStack, slot: Int): Boolean {
            return if ((ready || lastClick.timeSince() > 150.ms) && selected.remove(territory)) {
                ready = false

                lastClick = Date()

                click(slot)

                true
            } else false
        }
    }
}