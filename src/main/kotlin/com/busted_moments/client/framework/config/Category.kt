package com.busted_moments.client.framework.config

import com.busted_moments.client.framework.config.entries.HiddenEntry
import com.busted_moments.client.framework.text.Text
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder
import net.minecraft.network.chat.Component

typealias Section = SubCategoryBuilder

internal class Category(private val title: String) : ArrayList<Pair<Storage, Config.Entry<*>>>() {
    fun build(builder: ConfigBuilder): ConfigCategory =
        builder.getOrCreateCategory(Text.component(title)).apply {
            val entryBuilder = ConfigEntryBuilder.create()

            val entries = mutableListOf<() -> AbstractConfigListEntry<*>>()
            val sections = mutableMapOf<String, Section>()

            fun section(title: String): Section = sections.computeIfAbsent(title) { _ ->
                entryBuilder.startSubCategory(Component.literal(Config.nameOf(title))).also { entries += it::build }
            }

            sortWith { (_, e1), (_, e2) ->
                when {
                    e1.section != null && e2.section == null -> -1
                    e1.section == null && e2.section != null -> 1
                    else -> 0
                }
            }

            for ((owner, entry) in this@Category) {
                if (entry is HiddenEntry<*>)
                    continue

                if (entry.section == null)
                    entries.add { entry.open(owner, entryBuilder) }
                else
                    section(entry.section!!)+= entry.open(owner, entryBuilder)
            }

            entries.forEach { addEntry(it()) }
        }
}