package com.busted_moments.client.framework.config

import com.busted_moments.client.Client
import com.busted_moments.client.events.MinecraftEvent
import com.busted_moments.client.framework.FabricLoader
import com.busted_moments.client.framework.config.annotations.Category
import com.busted_moments.client.framework.config.annotations.Floating
import com.busted_moments.client.framework.config.annotations.Override
import com.busted_moments.client.framework.config.annotations.Section
import com.busted_moments.client.framework.config.annotations.Tooltip
import com.busted_moments.client.framework.config.entries.HiddenEntry
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.events.events
import com.busted_moments.client.framework.text.Text
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder
import net.essentuan.esl.json.Json
import net.essentuan.esl.json.type.AnyJson
import net.essentuan.esl.model.BaseModel
import net.essentuan.esl.model.impl.IBound
import net.essentuan.esl.model.impl.Property
import net.essentuan.esl.other.Result
import net.essentuan.esl.other.ofNullable
import net.essentuan.esl.other.orElse
import net.essentuan.esl.other.orNull
import net.essentuan.esl.other.result
import net.essentuan.esl.reflections.Reflections
import net.essentuan.esl.reflections.Types.Companion.objects
import net.essentuan.esl.reflections.extensions.annotatedWith
import net.essentuan.esl.reflections.extensions.get
import net.essentuan.esl.reflections.extensions.instance
import net.essentuan.esl.reflections.extensions.simpleString
import net.essentuan.esl.reflections.extensions.tags
import net.essentuan.esl.scheduling.tasks
import net.essentuan.esl.string.reader.consume
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import java.nio.file.Path
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR
import java.util.function.Consumer
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.reflect.KProperty

object NoCopy

private typealias CategoryBuilder = com.busted_moments.client.framework.config.Category

object Config {
    private val path: Path =
        FabricLoader.configDir / "fuy"

    val failed: MutableList<Pair<Path, Path>> = mutableListOf()

    private val suffixes: MutableSet<String> = mutableSetOf(
        "feature",
        "model"
    )

    private lateinit var storages: List<Storage>

    //TODO: Add legacy config reading
    fun read() {
        if (Config::storages.isInitialized)
            return

        storages = Reflections.types
            .subtypesOf(Storage::class)
            .objects()
            .map { it.instance }
            .filterNotNull()
            .onEach {
                it.events.register()
                it.tasks.resume()

                it.properties
                    .values
                    .forEach { bound ->
                        if (bound is Entry<*>.Bound)
                            bound.init()
                    }
            }
            .toList()

        val files: MutableMap<String, Result<Json>> = mutableMapOf()

        fun file(str: String): Json? =
            files.computeIfAbsent(str) {
                val file = path / str

                return@computeIfAbsent try {
                    Json.read(file).ofNullable()
                } catch (ex: Throwable) {
                    val name = str.removeSuffix(".json")
                    val date = Calendar.getInstance()

                    val baseName = "$name-${date[DAY_OF_MONTH]}-${date[MONTH]}-${date[YEAR]}"

                    var i = 0L
                    var out = path / "backup" / "$baseName.json"

                    while (out.exists() && i < Long.MAX_VALUE) {
                        i++

                        out = path / "backup" / "$baseName-$i.json"
                    }

                    out.parent.createDirectories()

                    file.copyTo(
                        out,
                        overwrite = true
                    )

                    file.deleteIfExists()

                    Client.error("Error reading config file $str! A backup has been moved to $out.", ex)

                    failed += file to out

                    Result.empty()
                }
            }.orNull()

        for (storage in storages) {
            storage.load(
                file(storage.file)
                    ?.getJson(storage.category.lowercase())
                    ?.getJson(keyOf(storage.javaClass.simpleString()))
                    ?: continue
            )
        }
    }

    fun write() {
        val files: MutableMap<String, Json> = mutableMapOf()
        fun file(str: String): Json =
            files.computeIfAbsent(str) { Json() }

        for (storage in storages) {
            val name = keyOf(storage.javaClass.simpleString())
            val file = file(storage.file)
            val category = file
                .getJson(storage.category.lowercase())
                ?.getJson(name)

            if (category != null)
                category.addAll(storage.export())
            else
                file["${storage.category.lowercase()}.$name"] = storage.export()
        }

        for ((file, data) in files)
            data.write(path / file)
    }

    fun open(parent: Screen?): ConfigBuilder =
        ConfigBuilder.create()
            .setTitle(Component.literal("fuy.gg | Configuration"))
            .setSavingRunnable(this::write)
            .setShouldListSmoothScroll(true)
            .setShouldTabsSmoothScroll(true)
            .setTransparentBackground(true)
            .apply {
                if (parent != null)
                    parentScreen = parent

                val categories = mutableMapOf(
                    "General" to CategoryBuilder("General")
                )

                storages.asSequence()
                    .sortedBy { it.javaClass.simpleName }
                    .flatMap { it.properties.values }
                    .filterIsInstance<Entry<*>.Bound>()
                    .forEach {
                        categories.computeIfAbsent(it.category) { title -> CategoryBuilder(title) }.add(it)
                    }

                categories.values.forEach { it.build(this) }
            }

    fun suffix(string: String) {
        suffixes += string
    }

    fun keyOf(name: String): String {
        val reader = name.consume()
        val result = StringBuilder()

        while (reader.canRead()) {
            if (reader.peek() == '_')
                reader.skip()

            var first = true

            val part = reader.readUntil(map = Char::lowercaseChar, skip = { it == '.' }) {
                if (first) {
                    first = false
                    return@readUntil false
                }

                it.isUpperCase() || it == '_'
            }

            if (part in suffixes)
                break

            if (result.isNotEmpty())
                result.append('_')

            result.append(part)
        }

        val str = result.toString()

        for (suffix in suffixes)
            if (str.endsWith("_$suffix"))
                return str.substring(0, str.length - (suffix.length + 1))


        return str
    }

    fun nameOf(key: String): String {
        val builder = StringBuilder()

        key.consume {
            val char = read()

            if (char != '_')
                builder.append(if (builder.isEmpty()) char.uppercaseChar() else char)
            else if (canRead()) {
                builder.append(' ')
                builder.append(read().uppercaseChar())
            }
        }

        return builder.toString()
    }

    abstract class Entry<T>(
        kotlin: KProperty<T?>,
        var title: Component
    ) : Property(
        kotlin.tags[Override::class]?.value ?: keyOf(kotlin.name),
        kotlin
    ) {
        constructor(
            kotlin: KProperty<T?>,
            title: String
        ) : this(kotlin, Text.component(title))

        private val tooltip: Array<Component>? = kotlin.tags[Tooltip::class]?.run {
            Array(value.size) { Text.component(value[it]) }
        }

        protected fun <U : AbstractFieldBuilder<T, *, *>> T.create(constructor: (Component, T) -> U) =
            constructor(title, this)

        protected abstract fun T.open(builder: ConfigEntryBuilder): AbstractFieldBuilder<T, *, *>

        @Suppress("UNCHECKED_CAST")
        protected open fun mutate(value: Any?): T? = value as T?

        override fun bind(obj: BaseModel<*>): IBound {
            return Bound(obj)
        }

        inner class Bound(
            obj: BaseModel<*>
        ) : Property.Bound(obj), Consumer<T> {
            inline fun <reified T> instanceOf(): Boolean {
                return this@Entry is T
            }

            var title: Component
                get() = this@Entry.title
                set(value) {
                    this@Entry.title = value
                }

            val file: String
                get() = (obj as Storage).file

            lateinit var category: String
                private set

            var section: String? = null

            private var default: Any? = NoCopy

            internal fun init() {
                category = tags[Category::class]?.value ?: (obj as Storage).category

                if (!this.annotatedWith(Floating::class))
                    section = tags[Section::class]?.value ?: (obj as Storage).section

                if (this@Entry !is HiddenEntry)
                    default = value.copy().orElse(NoCopy)
            }

            @Suppress("UNCHECKED_CAST")
            internal fun open(builder: ConfigEntryBuilder): AbstractConfigListEntry<*> =
                (value as T).open(builder).apply {
                    saveConsumer = this@Bound
                    @Suppress("UNCHECKED_CAST")

                    if (default !is NoCopy)
                        setDefaultValue(default as T)

                    if (tooltip != null)
                        setTooltip(*tooltip)


                }.build()

            override fun export(data: AnyJson) {
                if (value != default)
                    super.export(data)
            }

            override fun accept(value: T) {
                this.value = mutate(value)
            }
        }
    }

    private fun <T> Collection<*>.copy(collect: Sequence<Any?>.() -> T): Result<T> =
        asSequence().map { it?.copy()?.orElse(null) }.collect().result()

    fun Any?.copy(): Result<Any?> =
        when (this) {
            is List<*> -> copy(Sequence<Any?>::toList)
            is Set<*> -> copy(Sequence<Any?>::toSet)
            is Map<*, *> -> {
                asSequence()
                    .map { (key, value) -> key?.copy()?.orElse(null) to value?.copy()?.orElse(null) }
                    .toMap()
                    .result()
            }

            else -> this.result()
        }
}

fun Json.Companion.read(path: Path): Json? {
    if (!path.exists())
        return null

    return Json(path.readText())
}

fun Json.write(path: Path) {
    path.parent.createDirectories()
    path.writeText(this.asString(true))
}

@Subscribe
private fun MinecraftEvent.Stop.on() = Config.write()