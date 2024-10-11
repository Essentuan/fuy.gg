package com.busted_moments.client.framework.config

import com.busted_moments.client.Client
import com.busted_moments.client.events.MinecraftEvent
import com.busted_moments.client.framework.FabricLoader
import com.busted_moments.client.framework.config.annotations.*
import com.busted_moments.client.framework.config.annotations.Category
import com.busted_moments.client.framework.config.annotations.Section
import com.busted_moments.client.framework.config.entries.HiddenEntry
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.events.events
import com.busted_moments.client.framework.features.Feature
import com.busted_moments.client.framework.text.Text
import com.wynntils.mc.event.ConnectionEvent
import com.wynntils.utils.mc.McUtils.mc
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder
import net.essentuan.esl.Result
import net.essentuan.esl.ifPresent
import net.essentuan.esl.json.Json
import net.essentuan.esl.json.type.AnyJson
import net.essentuan.esl.model.Model
import net.essentuan.esl.model.Model.Companion.export
import net.essentuan.esl.model.Model.Companion.load
import net.essentuan.esl.model.field.Parameter
import net.essentuan.esl.model.field.Property
import net.essentuan.esl.model.impl.PropertyImpl
import net.essentuan.esl.ofNullable
import net.essentuan.esl.orElse
import net.essentuan.esl.other.unsupported
import net.essentuan.esl.reflections.Reflections
import net.essentuan.esl.reflections.Types.Companion.objects
import net.essentuan.esl.reflections.extensions.annotatedWith
import net.essentuan.esl.reflections.extensions.extends
import net.essentuan.esl.reflections.extensions.get
import net.essentuan.esl.reflections.extensions.instance
import net.essentuan.esl.reflections.extensions.isDelegated
import net.essentuan.esl.reflections.extensions.tags
import net.essentuan.esl.result
import net.essentuan.esl.scheduling.tasks
import net.essentuan.esl.string.reader.consume
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import java.io.File
import java.nio.file.Path
import java.util.Calendar
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR
import java.util.LinkedList
import java.util.function.Consumer
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.sequences.flatMap

object NoCopy

private typealias CategoryBuilder = com.busted_moments.client.framework.config.Category

object Config {
    private val path: Path =
        FabricLoader.configDir / "fuy"

    val failed: MutableList<Pair<Path, Path>> = mutableListOf()

    private val suffixes: MutableSet<String> = mutableSetOf(
        "feature",
        "overlay",
        "model"
    )

    private lateinit var storages: List<Storage>

    //TODO: Add legacy config reading
    fun read() {
        val initalized = ::storages.isInitialized

        if (!initalized)
            storages = Reflections.types
                .subtypesOf(Storage::class)
                .objects()
                .filter { it.java.enclosingClass == null }
                .sortedBy { it.simpleName }
                .flatMap {
                    sequence<KClass<out Storage>> {
                        yield(it)

                        val stack = LinkedList<Class<*>>(it.java.declaredClasses.asList())

                        while (stack.isNotEmpty()) {
                            val next = stack.pop()

                            @Suppress("UNCHECKED_CAST")
                            if (next extends Storage::class)
                                yield(next.kotlin as KClass<out Storage>)


                            val classes = next.declaredClasses
                            for (i in (classes.size - 1) downTo 0) {
                                stack.offerFirst(classes[i])
                            }
                        }
                    }
                }
                .map { it.instance }
                .filterNotNull()
                .onEach {
                    if (it !is Feature) {
                        it.events.register()
                        it.tasks.resume()
                    }

                    Model.descriptor(it.javaClass)
                        .forEach { prop ->
                            if (prop is Entry<*>)
                                prop.init(it)
                        }
                }
                .toList()

        val files: MutableMap<String, Result<Json>> = mutableMapOf()

        fun read(storage: Storage, block: Storage.(Json) -> Unit) {
            val str = storage.file.replace("{uuid}", mc().user.profileId.toString())
            val file = path / str
            var json = files[str]

            try {
                if (json == null) {
                    json = Json.read(file).ofNullable()
                    files[str] = json
                }

                json.ifPresent { storage.block(it) }
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

                files[str] = Result.empty()
            }
        }

        for (storage in storages) {
            read(storage) load@{
                load(
                    it
                        .getJson(storage.category.lowercase())
                        ?.getJson(keyOf(storage.javaClass))
                        ?: return@load
                )
            }

            if (!initalized)
                storage.ready()
        }

        LegacyConfig.read()
    }

    fun write() {
        val files: MutableMap<String, Json> = mutableMapOf()
        fun file(str: String): Json =
            files.computeIfAbsent(str) { Json() }

        for (storage in storages) {
            val name = keyOf(storage.javaClass)
            val file = file(storage.file.replace("{uuid}", mc().user.profileId.toString()))
            val category = file
                .getJson(storage.category.lowercase())
                ?.getJson(name)

            if (category != null)
                category.addAll(storage.export())
            else
                file["${storage.category.lowercase()}.$name"] = storage.export()
        }

        for ((file, data) in files) {
            try {
                data.write(path / file)
            } catch (ex: Exception) {
                Client.error("An exception was thrown while saving ${file}!", ex)
            }
        }
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
                    .flatMap {
                        Model.descriptor(it.javaClass)
                            .filterIsInstance<Entry<*>>()
                            .map { e -> it to e }
                    }
                    .forEach {
                        categories.computeIfAbsent(it.second.category) { title ->
                            CategoryBuilder(title)
                        }.add(it)
                    }

                categories.values.forEach { it.build(this) }
            }

    fun suffix(string: String) {
        suffixes += string
    }

    fun keyOf(store: Class<*>): String {
        val result = StringBuilder()

        val hierarchy = LinkedList<Class<*>>()

        var current: Class<*>? = store
        while (current != null) {
            if (!(current annotatedWith Skip::class))
                hierarchy.offerFirst(current)

            current = current.enclosingClass
        }

        for (cls in hierarchy) {
            if (result.isNotEmpty())
                result.append(".")

            if (cls.simpleName.lowercase() in suffixes) {
                result.append(cls.simpleName.lowercase())
                continue
            }

            val reader = cls.simpleName.consume()
            val initial = result.length

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
                    continue

                if (result.length != initial)
                    result.append('_')

                result.append(part)
            }
        }

        val str = result.toString()

        for (suffix in suffixes)
            if (str.endsWith("_$suffix"))
                return str.substring(0, str.length - (suffix.length + 1))


        return str
    }

    fun nameOf(key: String): String {
        val builder = StringBuilder()

        val reader = key.consume()
        while (reader.canRead()) {
            val char = reader.read()

            when {
                char == '.' ->
                    break

                char != '_' ->
                    builder.append(if (builder.isEmpty()) char.uppercaseChar() else char)

                reader.canRead() -> {
                    builder.append(' ')
                    builder.append(reader.read().uppercaseChar())
                }
            }
        }

        return builder.toString()
    }

    @Subscribe
    private fun ConnectionEvent.ConnectedEvent.on() {
        read()
    }

    @Subscribe
    private fun ConnectionEvent.DisconnectedEvent.on() {
        write()
    }

    abstract class Entry<T>(
        kotlin: KProperty<T?>,
        var title: Component
    ) : PropertyImpl(kotlin) {
        constructor(
            kotlin: KProperty<T?>,
            title: String
        ) : this(kotlin, Text.component(title))

        lateinit var file: File
            private set

        lateinit var category: String
            private set

        var section: String? = null

        var default: Any? = NoCopy
            private set

        var tooltip: Array<Component>? = kotlin.tags[Tooltip::class]?.run {
            Array(value.size) { Text.component(value[it]) }
        }

        internal fun init(obj: Storage) {
            if (::category.isInitialized)
                return

            category = tags[Category::class]?.value ?: obj.category

            if (!this.annotatedWith(Floating::class))
                section = tags[Section::class]?.value ?: obj.section

            if (this@Entry !is HiddenEntry)
                default = get(obj).copy().orElse(NoCopy)
        }

        protected open fun default(value: Any?): Any? = value

        protected fun <U : AbstractFieldBuilder<T, *, *>> T.create(constructor: (Component, T) -> U) =
            constructor(title, this)

        protected abstract fun T.open(builder: ConfigEntryBuilder): AbstractFieldBuilder<T, *, *>

        @Suppress("UNCHECKED_CAST")
        protected open fun mutate(value: Any?): T? = value as T?

        @Suppress("UNCHECKED_CAST")
        open fun open(model: Storage, builder: ConfigEntryBuilder): AbstractConfigListEntry<*> =
            (get(model) as T).open(builder).apply {
                saveConsumer = Consumer { set(model, mutate(it)) }
                @Suppress("UNCHECKED_CAST")

                if (default !is NoCopy)
                    setDefaultValue(default(default as T) as T)

                setTooltip(*tooltip ?: return@apply)
            }.build()

        override fun export(model: Model<*>, out: AnyJson, flags: Set<Any>) {
            if (get(model) != default)
                super.export(model, out, flags)
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

    interface Extension : net.essentuan.esl.model.Extension<Storage> {
        fun register(property: KProperty<*>): Property?

        override fun invoke(param: KParameter): Parameter? =
            unsupported("Storages do not support constructors!")

        override fun invoke(property: KProperty<*>): Property? {
            return if (property is KMutableProperty<*> || property.isDelegated)
                register(property)
            else
                null
        }
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