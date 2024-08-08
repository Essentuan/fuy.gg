package com.busted_moments.client.models.content.stages

import com.busted_moments.client.models.content.Stage
import com.busted_moments.client.models.content.Tracker
import com.busted_moments.client.models.content.Trigger
import net.essentuan.esl.time.duration.Duration
import net.essentuan.esl.time.duration.seconds
import net.essentuan.esl.time.extensions.timeSince
import java.util.*

class GenericStage(
    override val name: String,
    trigger: Trigger.Builder,
    override val tracker: Tracker?,
    val handler: Stage.() -> Unit
) : Stage {
    private val trigger: Trigger = trigger.build(this::ready)
    
    override var start: Date? = null
        private set
    
    override var end: Date? = null
        private set
    override val duration: Duration
        get() =
            when {
                start != null && end == null ->
                    start!!.timeSince()
                
                start == null || end == null ->
                    0.seconds
                
                else ->
                    Duration(start!!, end!!)
            }

    private fun ready() {
        if (start != null)
            return

        start = Date()

        handler()

        // Trigger can be null because ready may
        // be called before the stage is finished initializing
        trigger?.close()
    }

    override fun close() {
        if (end != null)
            return
        
        end = Date()
        
        trigger.close()
        tracker?.close()
    }

}