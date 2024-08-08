package com.busted_moments.client.models.content.stages

import com.busted_moments.client.models.content.Stage
import com.busted_moments.client.models.content.Tracker
import com.busted_moments.client.models.content.Trigger
import net.essentuan.esl.time.duration.Duration
import net.essentuan.esl.time.duration.seconds
import java.util.Date

class LazyStage(
    private val placeholder: String,
    trigger: Trigger.Builder,
    private val builder: Stage.Builder,
    private val handler: Stage.() -> Unit,
) : Stage {
    private var stage: Stage? = null
    private val trigger = trigger.build(this::start)

    override val name: String
        get() = stage?.name ?: placeholder
    override val tracker: Tracker?
        get() = stage?.tracker
    override val start: Date?
        get() = stage?.start
    override val end: Date?
        get() = stage?.end
    override val duration: Duration
        get() = stage?.duration ?: 0.seconds

    private fun start() {
        trigger.close()
        stage = builder.build {
            this@LazyStage.handler()
        }
    }

    override fun close() {
        trigger.close()
        stage?.close()
    }
}