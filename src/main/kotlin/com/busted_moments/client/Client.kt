package com.busted_moments.client

import com.busted_moments.framework.Extension
import com.busted_moments.framework.FabricLoader
import com.busted_moments.framework.Scan
import com.busted_moments.framework.events.events
import com.busted_moments.framework.events.post
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import net.essentuan.esl.iteration.extensions.toTypedArray
import net.essentuan.esl.reflections.Reflections
import net.essentuan.esl.reflections.extensions.instance
import net.essentuan.esl.reflections.extensions.isObject
import net.essentuan.esl.scheduling.Scheduler
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.metadata.ModMetadata
import net.fabricmc.loader.api.metadata.ModOrigin
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.Optional

@Scan("com.busted_moments.client", "com.busted_moments.framework")
object Client : Extension, ModContainer, Logger by LoggerFactory.getLogger("fuy_gg") {
    private lateinit var container: ModContainer

    @Override
    override fun init() {
        container = FabricLoader
            .getModContainer("fuy_gg")
            .orElseThrow { IllegalStateException("Where is fuy.gg? :(") }

        Scheduler.apply {
            capacity = 50
            @OptIn(DelicateCoroutinesApi::class)
            DISPATCHER = GlobalScope
            this += this@Client

            start()
        }

        Reflections.register(
            *FabricLoader
                .getEntrypointContainers("fuy_gg", Extension::class.java)
                .asSequence()
                .map { it.entrypoint }
                .onEach {
                    it.sounds.forEach { key ->
                        val location = ResourceLocation(key)

                        Registry.register(
                            BuiltInRegistries.SOUND_EVENT,
                            location,
                            SoundEvent.createVariableRangeEvent(location)
                        )
                    }
                }
                .flatMap { it.scan.asIterable() }
                .toTypedArray()
        )

        Reflections.types.forEach {
            it.java.events.register()

            if (it.isObject)
                it.instance?.events?.register()
        }
    }

    override fun getMetadata(): ModMetadata =
        container.metadata

    override fun getRootPaths(): List<Path> =
        container.rootPaths

    override fun getOrigin(): ModOrigin =
        container.origin

    override fun getContainingMod(): Optional<ModContainer> =
        container.containingMod

    override fun getContainedMods(): Collection<ModContainer> =
        container.containedMods

    override fun getRootPath(): Path =
        container.rootPath

    override fun getPath(file: String): Path =
        container.getPath(file)
}