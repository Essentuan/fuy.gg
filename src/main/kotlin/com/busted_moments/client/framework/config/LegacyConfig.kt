package com.busted_moments.client.framework.config

import com.busted_moments.client.Client
import com.busted_moments.client.framework.FabricLoader
import com.busted_moments.client.framework.config.annotations.Persistent
import com.busted_moments.client.framework.events.post
import me.shedaniel.cloth.clothconfig.shadowed.com.moandjiezana.toml.Toml
import net.neoforged.bus.api.Event
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.reader

object LegacyConfig : Storage {
    @Persistent
    var read: Boolean = false

    fun read() {
        if (read)
            return

        val legacyFile = FabricLoader.configDir / "fuy_gg.toml"

        if (!legacyFile.exists()) {
            read = true
            return
        }


        try {
            ImportEvent(Toml().read(legacyFile.reader())).post()

            read = true
        } catch(ex: Exception) {
            Client.error("Error reading legacy config!", ex)
        }
    }

    class ImportEvent(val toml: Toml) : Event()
}