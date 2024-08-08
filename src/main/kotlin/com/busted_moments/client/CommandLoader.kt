package com.busted_moments.client

import com.busted_moments.client.framework.events.Subscribe
import com.essentuan.acf.core.annotations.Command
import com.essentuan.acf.core.command.arguments.Argument
import com.essentuan.acf.fabric.core.FabricCommandBuilder
import com.essentuan.acf.fabric.core.client.FabricClientCommandLoader
import com.mojang.brigadier.context.CommandContext
import com.wynntils.mc.event.CommandSentEvent
import net.essentuan.esl.isPresent
import net.essentuan.esl.orElse
import net.essentuan.esl.reflections.Functions.Companion.static
import net.essentuan.esl.reflections.Reflections
import net.essentuan.esl.reflections.extensions.annotatedWith
import net.essentuan.esl.reflections.extensions.isObject
import net.essentuan.esl.reflections.extensions.isStatic
import net.essentuan.esl.scheduling.annotations.Auto
import net.essentuan.esl.unsafe
import kotlin.reflect.jvm.javaMethod

typealias ACFEvent = com.essentuan.acf.fabric.core.client.events.CommandSentEvent

@Auto(false)
object CommandLoader : FabricClientCommandLoader(
    FabricCommandBuilder.Client()
        .inPackage {
            Reflections.functions
                .filter {
                    try {
                        it.javaMethod?.isStatic() == true
                    } catch(ex: Throwable) {
                        false
                    }
                }
                .map { it.javaMethod?.declaringClass }
                .filter { it != null && it annotatedWith Command::class }
                .distinct()
                .toList()
        }.withArguments {
            Reflections.types
                .subtypesOf(Argument::class)
                .filterNot { it.isObject }
                .map { it.java }
                .toList()
        }
) {
    override fun info(s: String?, vararg objects: Any?) {
        Client.info(s, objects)
    }

    override fun error(s: String?, vararg objects: Any?) {
        Client.error(s, objects)
    }

    override fun debug(s: String?, vararg objects: Any?) {
        Client.debug(s, objects)
    }

    @Subscribe
    private fun CommandSentEvent.on() {
        isCanceled = ACFEvent(command).post()
    }
}