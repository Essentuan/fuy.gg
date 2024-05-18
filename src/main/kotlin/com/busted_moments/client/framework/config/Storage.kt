package com.busted_moments.client.framework.config

import com.busted_moments.client.framework.config.annotations.Category
import com.busted_moments.client.framework.config.annotations.File
import com.busted_moments.client.framework.config.annotations.Section
import net.essentuan.esl.json.Json
import net.essentuan.esl.model.BaseModel
import net.essentuan.esl.reflections.extensions.get
import net.essentuan.esl.reflections.extensions.simpleString
import net.essentuan.esl.reflections.extensions.tags
import net.essentuan.esl.scheduling.annotations.Auto

@Auto(false)
abstract class Storage : BaseModel<Json>() {
    val file: String = this::class.tags[File::class]?.run {
        if (value.endsWith(".json"))
            value
        else
            "$value.json"
    } ?: "fuy_gg.json"
    val category: String = this::class.tags[Category::class]?.value ?: "General"
    val section: String = this::class.tags[Section::class]?.value ?: Config.keyOf(this::class.simpleString())
}