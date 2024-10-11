package com.busted_moments.client.framework.config

import com.busted_moments.client.framework.config.annotations.Category
import com.busted_moments.client.framework.config.annotations.File
import com.busted_moments.client.framework.config.annotations.Section
import net.essentuan.esl.json.Json
import net.essentuan.esl.model.Model
import net.essentuan.esl.reflections.extensions.get
import net.essentuan.esl.reflections.extensions.simpleString
import net.essentuan.esl.reflections.extensions.tags
import net.essentuan.esl.scheduling.annotations.Auto

@Auto(false)
interface Storage : Model<Json> {
    fun ready(): Unit = Unit

    val file: String
        get() = this::class.tags[File::class]?.run {
            if (value.endsWith(".json"))
                value
            else
                "$value.json"
        } ?: "fuy_gg.json"

    val category: String
        get() = this::class.tags[Category::class]?.value ?: "General"

    val section: String
        get() = this::class.tags[Section::class]?.value ?: Config.nameOf(Config.keyOf(this::class.java))
}