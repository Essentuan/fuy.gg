package com.busted_moments.client.framework.keybind

import com.wynntils.core.keybinds.KeyBind
import net.essentuan.esl.reflections.Reflections
import net.essentuan.esl.reflections.extensions.instance
import net.essentuan.esl.time.duration.ms
import net.essentuan.esl.time.extensions.timeSince
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import java.util.Date

private val HELD_DURATION = 750.ms

abstract class Keybind(
    name: String,
    default: Int,
    category: String = "fuy.gg"
) : KeyMapping(
    name,
    default,
    category
) {
    private var heldAt: Date = Date(0)

    override fun isDown(): Boolean {
        return !isUnbound && super.isDown()
    }

    final override fun setDown(value: Boolean) {
        if (value == isDown)
            return

        super.setDown(value)

        if (value) {
            heldAt = Date()
            onDown()
        } else {
            if (heldAt.timeSince() > HELD_DURATION)
                onHeld()
            else
                onPress()

            onUp()
        }
    }

    protected open fun onDown() = Unit

    protected open fun onPress() = Unit

    protected open fun onHeld() = Unit

    protected open fun onUp() = Unit

    companion object {
        fun register() {
            Reflections.types
                .subtypesOf(Keybind::class)
                .map { it.instance }
                .filterNotNull()
                .forEach(KeyBindingHelper::registerKeyBinding)
        }
    }
}