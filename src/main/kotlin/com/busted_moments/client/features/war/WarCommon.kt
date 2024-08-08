package com.busted_moments.client.features.war

import com.busted_moments.client.framework.config.Storage
import com.busted_moments.client.framework.config.annotations.Category
import com.busted_moments.client.framework.config.annotations.Floating
import com.busted_moments.client.framework.config.annotations.Tooltip
import com.busted_moments.client.framework.config.entries.array.Array
import com.busted_moments.client.framework.config.entries.value.Value
import net.essentuan.esl.time.TimeUnit
import net.essentuan.esl.time.duration.Duration
import net.essentuan.esl.time.duration.FormatFlag


@Category("War")
object WarCommon : Storage {
    @Floating
    @Value("Show seconds")
    @Tooltip(["Shows seconds instead of a formatted time (1m 43s)"])
    private var useSeconds: Boolean = false

    fun Duration.toWarString(): String = when {
        isForever -> "--"
        useSeconds -> {
            val seconds = toSeconds().toInt()

            "$seconds second${if (seconds == 1) "" else "s"}"
        }
        else -> print(FormatFlag.COMPACT, TimeUnit.SECONDS)
    }
}