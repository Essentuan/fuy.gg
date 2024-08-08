package com.busted_moments.client.models.content

import com.busted_moments.client.framework.events.post
import com.busted_moments.client.models.content.event.ContentEvent
import net.essentuan.esl.time.duration.Duration
import net.essentuan.esl.time.duration.seconds
import net.essentuan.esl.time.extensions.timeSince
import java.util.Date
import java.util.UUID

class ContentTimer(
    val type: ContentType,
    stages: List<Stage.Builder>,
    completion: Trigger.Builder,
    failure: List<Trigger.Builder>,
    private val delegate: MutableList<Stage> = mutableListOf()
) : List<Stage> by delegate {
    private val completion = completion.build(this::finish)
    private val failure = failure.map { it.build(this::fail) }

    init {
        for (stage in stages)
            delegate.add(stage.build(this::next))
    }

    fun start() {
        ContentEvent.Enter(this).post()
    }

    var start: Date? = null
        private set
    var end: Date? = null
        private set

    val duration: Duration
        get() =
            when {
                start != null && end == null ->
                    start!!.timeSince()

                start == null || end == null ->
                    0.seconds

                else ->
                    Duration(start!!, end!!)
            }

    private fun next(stage: Stage) {
        if (start == null)
            start = Date()

        val index = indexOf(stage)

        if (index != -1)
            for (i in 0..<index) {
                val stage = this[i]
                val finished = stage.end == null

                stage.close()

                if (finished)
                    ContentEvent.StageEnd(this, stage).post()
            }

        ContentEvent.StageStart(this, stage).post()
    }

    private fun finish() {
        if (end != null)
            return

        close()

        ContentEvent.Finish(this).post()
    }

    private fun fail() {
        if (end != null)
            return

        close()

        ContentEvent.Fail(this).post()
    }

    fun close() {
        if (end != null)
            return

        end = Date()

        for (stage in this)
            stage.close()

        completion.close()

        for (trigger in failure)
            trigger.close()
    }
} 