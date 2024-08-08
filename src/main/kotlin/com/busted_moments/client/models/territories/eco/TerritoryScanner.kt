package com.busted_moments.client.models.territories.eco

import com.busted_moments.client.framework.Containers
import com.busted_moments.client.framework.artemis.Ticks
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.events.events
import com.busted_moments.client.framework.keybind.Inputs
import com.busted_moments.client.framework.sounds.Sounds.play
import com.busted_moments.client.framework.text.StyleType
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.inline
import com.wynntils.mc.event.ContainerSetContentEvent
import com.wynntils.mc.event.MenuEvent.MenuOpenedEvent
import net.essentuan.esl.collections.maps.IntMap
import net.essentuan.esl.collections.synchronized
import net.essentuan.esl.coroutines.delay
import net.essentuan.esl.delegates.final
import net.essentuan.esl.scheduling.annotations.Every
import net.essentuan.esl.scheduling.tasks
import net.essentuan.esl.time.duration.Duration
import net.essentuan.esl.time.duration.ms
import net.essentuan.esl.time.extensions.timeSince
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.EventPriority
import java.io.Closeable
import java.util.Date
import java.util.LinkedList
import java.util.Queue
import kotlin.math.min

private const val UP = 0
private const val DOWN = 1
private const val PREVIOUS_PAGE = 9
private const val NEXT_PAGE = 27

abstract class TerritoryScanner(
    private val container: Int
) : Closeable {
    private val clicks: Queue<ClickAction> = LinkedList()
    private var refreshed: Boolean = true

    var contents: List<ItemStack> = ArrayList()
        private set
    private var page = 0

    private val pages: MutableMap<Int, List<Slot>> = IntMap<List<Slot>>().synchronized()

    private var scanning: Boolean = true
    private var direction: Int = DOWN

    var update: (Economy) -> Unit by final()

    private var singlePaged: Boolean = false
    private var lastClick: Date = Date(0)

    protected abstract val delay: Duration

    init {
        events.register()
        tasks.resume()
    }

    fun disable() {
        scanning = false
    }

    fun enable() {
        scanning = true

        ContainerSetContentEvent.Pre(
            contents,
            null,
            container,
            0
        ).on()
    }

    private suspend fun wait() {
        val wait = delay - lastClick.timeSince()
        if (wait > 0.ms)
            delay(wait)
    }

    @Subscribe(priority = EventPriority.HIGHEST)
    fun ContainerSetContentEvent.Pre.on() = inline {
        if (containerId != containerId || items == null || items.isEmpty())
            return@inline

        refreshed = true

        contents = items

        if (!hasPreviousPage() && !hasNextPage())
            singlePaged = true

        val now: MutableList<Slot> = ArrayList()

        for (slot in 0..<min(45, contents.size)) {
            val stack = contents[slot]
            if (Economy.isTerritory(stack))
                now.add(Slot(Economy.nameOf(stack), stack, slot))
        }

        if (!hasPreviousPage() || page < 0)
            page = 0

        pages[page] = now

        peek(pages)

        update(
            Economy(
                pages
                    .values
                    .asSequence()
                    .flatMap { it.asSequence() }
                    .map { it.stack }
            )
        )

        var ret = false

        for ((territory, stack, slot) in now)
            if (process(territory, stack, slot)) {
                ret = true

                break
            }

        if (clicks.isNotEmpty()) {
            wait()

            val (slot, button, sound) = clicks.poll()
            clickNow(slot, button, sound)

            ret = true
        }

        if (ret)
            return@inline

        when {
            !scanning -> return@inline

            direction == DOWN -> {
                if (!hasNextPage()) {
                    if (pages.size > page + 1) {
                        for (i in (page + 1)..<pages.size)
                            pages.remove(i)
                    }

                    click(PREVIOUS_PAGE)
                } else
                    click(NEXT_PAGE)
            }


            direction == UP -> {
                if (!hasPreviousPage())
                    click(NEXT_PAGE)
                else
                    click(PREVIOUS_PAGE)
            }
        }
    }

    @Subscribe(priority = EventPriority.HIGHEST)
    fun MenuOpenedEvent.Pre.on() {
        close()
    }

    @Every(ms = 100.0)
    private fun doRefresh() {
        if (singlePaged)
            Ticks.schedule {
                val container = Containers.opened ?: return@schedule

                if (Containers.opened?.containerId == this.container) {
                    ContainerSetContentEvent.Pre(
                        container.items,
                        null,
                        container.containerId,
                        0
                    ).on()
                }
            }
    }

    private fun clickNow(slot: Int, button: Int, sound: SoundEvent?) {
        lastClick = Date()
        refreshed = false

        when (slot) {
            NEXT_PAGE -> {
                page++
                direction = DOWN
            }

            PREVIOUS_PAGE -> {
                page--
                direction = UP
            }
        }

        sound?.play(SoundSource.MASTER)
        Containers.click(slot, button, container)
    }

    fun click(slot: Int, button: Int = Inputs.MOUSE_BUTTON_LEFT, sound: SoundEvent? = null) {
        val delay =
            if (slot == PREVIOUS_PAGE || slot == NEXT_PAGE)
                true
            else
                lastClick.timeSince() > delay


        if ((refreshed && clicks.isEmpty() && delay))
            clickNow(slot, button, sound)
        else
            clicks.add(ClickAction(slot, button, sound))
    }

    protected open fun peek(items: Map<Int, List<Slot>>) = Unit

    /**
     * Called on every slot each refresh.
     *
     * @return `true` when this [TerritoryScanner] should not process the remaining items
     */
    protected abstract suspend fun process(territory: String, stack: ItemStack, slot: Int): Boolean

    fun rescan() {
        ContainerSetContentEvent.Pre(
            contents,
            null,
            container,
            0
        ).run {
            on()
        }
    }

    protected fun hasNextPage(): Boolean {
        val next = contents[NEXT_PAGE]

        return !next.isEmpty && Text(next.displayName).contains("Next Page", StyleType.NONE)
    }

    protected fun hasPreviousPage(): Boolean {
        val previous = contents[PREVIOUS_PAGE]

        return !previous.isEmpty && Text(previous.displayName).contains("Previous Page", StyleType.NONE)
    }

    override fun close() {
        scanning = false
        events.unregister()
        tasks.suspend()
    }

    data class Slot(val territory: String, val stack: ItemStack, val slot: Int)
    private data class ClickAction(val slot: Int, val button: Int, val sound: SoundEvent?)
}