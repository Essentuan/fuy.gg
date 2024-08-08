package com.busted_moments.client.models.content.stages

import com.busted_moments.client.models.content.Stage
import com.busted_moments.client.models.content.Tracker
import com.wynntils.core.text.StyledText
import net.essentuan.esl.time.duration.Duration
import net.essentuan.esl.time.duration.ms
import java.util.*

class TextStage(
    val text: StyledText
) : Stage, Stage.Builder {
    override val name: String = text.stringWithoutFormatting
    
    override val tracker: Tracker?
        get() = null
    override val start: Date?
        get() = null
    override val end: Date?
        get() = null
    override val duration: Duration
        get() = 0.ms

    override fun close() = Unit

    override fun build(handler: Stage.() -> Unit): Stage =
        this
}