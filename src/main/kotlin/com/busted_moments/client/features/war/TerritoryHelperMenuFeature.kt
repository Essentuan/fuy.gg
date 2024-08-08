package com.busted_moments.client.features.war

import com.busted_moments.client.Patterns.GUILD_MANAGE_MENU_TITLE
import com.busted_moments.client.Patterns.SELECT_TERRITORIES_MENU_TITLE
import com.busted_moments.client.Patterns.TERRITORY_MENU_TITLE
import com.busted_moments.client.framework.Containers
import com.busted_moments.client.framework.config.annotations.Category
import com.busted_moments.client.framework.config.annotations.Floating
import com.busted_moments.client.framework.config.annotations.Tooltip
import com.busted_moments.client.framework.config.entries.array.Array
import com.busted_moments.client.framework.config.entries.value.Value
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.features.Feature
import com.busted_moments.client.framework.features.Replaces
import com.busted_moments.client.framework.render.screen.open
import com.busted_moments.client.framework.text.StyleType
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.text.Text.matches
import com.busted_moments.client.screens.territories.ManageTerritoriesScreen
import com.busted_moments.client.screens.territories.QuickAccess
import com.busted_moments.client.screens.territories.SelectTerritoriesScreen
import com.busted_moments.client.screens.territories.search.TerritorySearch
import com.wynntils.features.ui.CustomTerritoryManagementScreenFeature
import com.wynntils.mc.event.ContainerClickEvent
import com.wynntils.mc.event.ContainerSetContentEvent
import com.wynntils.mc.event.MenuEvent.MenuOpenedEvent
import com.wynntils.models.worlds.event.WorldStateEvent
import net.essentuan.esl.scheduling.api.schedule
import net.essentuan.esl.time.duration.ms
import net.minecraft.world.item.Items

@Category("War")
@Replaces(CustomTerritoryManagementScreenFeature::class)
object TerritoryHelperMenuFeature : Feature() {
    @Value("Redirect back button")
    private var doRedirect: Boolean = true
    var redirect: Boolean = true

    @Value("Show production")
    var showProduction: Boolean = true
        private  set

    @Value("Show usage percents")
    var showUsagePercents: Boolean = false

    @Value("Ignore resources from FFA")
    var ignoreFFA: Boolean = true
        private set

    @Floating
    @Array("Blacklist")
    @Tooltip(["Resources from territories on the blacklist will be ignored."])
    private var _blacklist: List<String> = emptyList()
        set(value) {
            field = value

            blacklist = value.toSet()
        }

    var blacklist: Set<String> = emptySet()
        private set

    private var noReset = false

    @Subscribe
    private fun MenuOpenedEvent.Pre.on() {
        Text(title).matches {
            TERRITORY_MENU_TITLE { _, _ ->
                isCanceled = true
                ManageTerritoriesScreen(containerId).open()

                if (!noReset) {
                    QuickAccess.reset()
                    TerritorySearch.reset()
                }

                noReset = false

                return@matches
            }

            SELECT_TERRITORIES_MENU_TITLE { _, _ ->
                isCanceled = true
                SelectTerritoriesScreen(containerId).open()

                return@matches
            }

            GUILD_MANAGE_MENU_TITLE { _, _ ->
                if (redirect && doRedirect) {
                    isCanceled = true
                    noReset = true
                }

                return@matches
            }
        }
    }

    @Subscribe
    private fun ContainerSetContentEvent.Pre.on() {
        Text(Containers.opened?.title ?: return).matches {
            GUILD_MANAGE_MENU_TITLE { _, _ ->
                if (redirect && doRedirect)
                    Containers.click(14, 0)

                redirect = false
            }
        }
    }

    @Subscribe
    private fun ContainerClickEvent.on() {
        if (slotNum != 9)
            return

        val stack = containerMenu.slots.getOrNull(11)?.item ?: return
        redirect = stack.item == Items.DISPENSER && Text(stack.displayName).contains("[Guild Tower]", StyleType.NONE)

        if (redirect) {
            schedule {
                redirect = false
            } after 500.ms
        }
    }

    @Subscribe
    private fun WorldStateEvent.on() {
        redirect = false
    }
}