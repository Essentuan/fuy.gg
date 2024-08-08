package com.busted_moments.client.screens.territories

import com.busted_moments.client.framework.Commands
import com.busted_moments.client.framework.render.elements.TextureElement
import com.busted_moments.client.framework.render.helpers.Percentage.Companion.pct
import com.busted_moments.client.framework.render.screen.Title
import com.busted_moments.client.framework.render.screen.elements.click
import com.busted_moments.client.framework.render.screen.elements.item
import com.busted_moments.client.framework.sounds.Sounds.play
import com.busted_moments.client.models.territories.eco.TerritoryData
import com.busted_moments.client.models.territories.eco.TerritoryScanner
import com.busted_moments.client.models.territories.war.WarModel
import net.essentuan.esl.time.duration.Duration
import net.essentuan.esl.time.duration.ms
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack

private const val LOADOUT_SLOT = 36

@Title("Manage Territories")
class ManageTerritoriesScreen(
    container: Int
) : TerritoryScreen<ManageTerritoriesScreen.Scanner>(
    container,
    Scanner(container)
) {
    init {
        scanner.screen = this
    }

    override fun select(territory: TerritoryData) {
        if (WarModel.current == null) {
            SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON.play(SoundSource.MASTER)
            Commands.execute("gu territory ${territory.name}")
        } else
            scanner.select(territory)
    }

    override fun TextureElement<Context>.renderSidebar(
        ctx: Context
    ) {
        item {
            x = this@renderSidebar.x + ((50.pct of this@renderSidebar.width) - (50.pct of this.width)) - 0.5f
            y = (50.pct of ctx.window.guiScaledHeight) - (50.pct of this.height)

            stack = scanner.contents.getOrNull(BACK_SLOT) ?: return@item false
            tooltip = true

            click { _, _, _ ->
                scanner.click(BACK_SLOT, sound = SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON)

                true
            }

            true
        }

        item {
            x = this@renderSidebar.x + ((50.pct of this@renderSidebar.width) - (50.pct of this.width))
            y = this@renderSidebar.y + this@renderSidebar.height - 5 - this.height

            stack = scanner.contents.getOrNull(LOADOUT_SLOT) ?: return@item false
            tooltip = true

            click { _, _, _ ->
                scanner.click(LOADOUT_SLOT)

                true
            }

            true
        }
    }

    class Scanner(container: Int) : TerritoryScanner(container) {
        internal lateinit var screen: ManageTerritoriesScreen
        private var selected: String? = null

        fun select(territory: TerritoryData) {
            selected = territory.name
        }

        override val delay: Duration
            get() = 0.ms

        override suspend fun process(territory: String, stack: ItemStack, slot: Int): Boolean {
            if (territory == selected) {
                selected = null
                click(slot, sound = SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON)

                return true
            }

            return false
        }
    }
}