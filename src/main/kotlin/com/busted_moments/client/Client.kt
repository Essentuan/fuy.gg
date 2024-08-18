package com.busted_moments.client

import com.busted_moments.buster.Buster
import com.busted_moments.client.framework.Extension
import com.busted_moments.client.framework.FabricLoader
import com.busted_moments.client.framework.Scan
import com.busted_moments.client.framework.config.Config
import com.busted_moments.client.framework.events.events
import com.busted_moments.client.framework.keybind.Keybind
import com.busted_moments.client.framework.marker.Marker
import com.busted_moments.client.framework.wynntils.Function
import com.busted_moments.client.framework.sounds.Sounds
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import net.essentuan.esl.future.api.CompletionException
import net.essentuan.esl.future.api.Future
import net.essentuan.esl.iteration.extensions.toTypedArray
import net.essentuan.esl.reflections.Reflections
import net.essentuan.esl.reflections.extensions.instance
import net.essentuan.esl.reflections.extensions.isObject
import net.essentuan.esl.scheduling.Scheduler
import net.essentuan.esl.scheduling.annotations.isAutoLoaded
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.metadata.ModMetadata
import net.fabricmc.loader.api.metadata.ModOrigin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.Optional
import kotlin.system.exitProcess

@Scan("com.busted_moments.client")
object Client : Extension, ModContainer, Logger by LoggerFactory.getLogger("fuy_gg") {
    private lateinit var container: ModContainer

    @Override
    @OptIn(DelicateCoroutinesApi::class)
    override fun init() {
        container = FabricLoader
            .getModContainer("fuy_gg")
            .orElseThrow { IllegalStateException("Where is fuy.gg? :(") }

        Reflections.register(
            *FabricLoader
                .getEntrypointContainers("fuy_gg", Extension::class.java)
                .asSequence()
                .map { it.entrypoint }
                .onEach {
                    it.sounds.forEach { key ->
                        Sounds += key
                    }
                }
                .flatMap { it.scan.asIterable() }
                .toTypedArray()
        )
        Buster

        Scheduler.apply {
            capacity = 50
            DISPATCHER = GlobalScope
            this += this@Client

            start()
        } finally { exitProcess(0) }

        Reflections.types.forEach {
            it.java.events.register()

            if (it.isObject && it.isAutoLoaded)
                it.instance?.events?.register()
        }

        Config.read()
        Keybind.register()
        Function.register()
        Marker.Provider.register()
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

    @Deprecated("Deprecated in Java")
    override fun getRootPath(): Path =
        container.rootPath

    @Deprecated("Deprecated in Java")
    override fun getPath(file: String): Path =
        container.getPath(file)
}

inline fun <T> inline(crossinline block: suspend () -> T): Future<T> = Future(block).also {
    it.except { ex -> Client.error("Exception in future!", CompletionException(it, cause = ex)) }
}
