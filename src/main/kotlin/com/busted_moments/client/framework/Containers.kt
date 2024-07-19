package com.busted_moments.client.framework

import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.text.Text
import com.wynntils.core.text.StyledText
import com.wynntils.handlers.container.type.ContainerContent
import com.wynntils.mc.event.ContainerCloseEvent
import com.wynntils.mc.event.ContainerSetContentEvent
import com.wynntils.mc.event.MenuEvent
import com.wynntils.utils.wynn.ContainerUtils
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.EventPriority
import java.util.regex.Pattern


object Containers {
    private
    var ID: Int = -1
    private var TITLE: StyledText? = null

    private var ITEMS: List<ItemStack>? = null
    private var MENU_TYPE: MenuType<*>? = null

    val opened: ContainerContent?
        get() {
            return ContainerContent(
                ITEMS ?: return null,
                TITLE?.component ?: return null,
                MENU_TYPE ?: return null,
                ID
            )
        }

    @Synchronized
    @Subscribe(priority = EventPriority.HIGHEST)
    private fun MenuEvent.MenuOpenedEvent.Pre.on() {
        ID = containerId
        TITLE = Text(title)
        MENU_TYPE = menuType
    }

    @Synchronized
    @Subscribe(priority = EventPriority.HIGHEST)
    private fun ContainerSetContentEvent.Pre.on() {
        if (ID != containerId) clear()
        else ITEMS = items
    }

    @Synchronized
    @Subscribe(priority = EventPriority.HIGHEST)
    private fun ContainerCloseEvent.Pre.on() {
        clear()
    }

    @Synchronized
    @Subscribe(priority = EventPriority.HIGHEST)
    private fun MenuEvent.MenuClosedEvent.on() {
        if (ID == containerId)
            clear()
    }

    @Synchronized
    private fun clear() {
        ID = -1
        TITLE = null
        ITEMS = null
    }

    @Synchronized
    fun isOpen(id: Int): Boolean {
        return opened?.containerId == id
    }

    @Synchronized
    fun click(slot: Int, button: Int): Boolean {
        return click(slot, button, ID)
    }

    @Synchronized
    fun click(slot: Int, button: Int, title: Pattern): Boolean {
        if (opened == null || !title.matcher(TITLE!!.stringWithoutFormatting).matches()) return false

        return click(slot, button, ID)
    }

    @Synchronized
    fun click(slot: Int, button: Int, container: Int): Boolean {
        if (ID != container) return false

        ContainerUtils.clickOnSlot(slot, container, button, ITEMS)

        return true
    }

    @Synchronized
    fun close(id: Int) {
        if (opened?.containerId == id)
            ContainerUtils.closeContainer(id)
    }
}