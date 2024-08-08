package com.busted_moments.client.models.content.stages

import com.busted_moments.client.models.content.Stage
import com.busted_moments.client.models.content.Tracker
import net.essentuan.esl.time.duration.Duration
import net.essentuan.esl.time.duration.ms
import java.util.*

class Multistage(
    options: Array<out Stage.Builder>,
    val handler: Stage.() -> Unit
) : Stage {
    private val options =
        options.map {it.build(this::handle)}
    
    private var selected: Stage? = null
    override val name: String
        get() = selected?.name ?: "[TBD]"
    
    override val tracker: Tracker?
        get() = selected?.tracker
    override val start: Date?
        get() = selected?.start
    override val end: Date?
        get() = selected?.end
    override val duration: Duration
        get() = selected?.duration ?: 0.ms

    private fun handle(stage: Stage) {
        if (selected != null)
            return
        
        selected = stage
        
        for (option in options)
            if (option !== stage)
                option.close()
        
        handler()
    }
    
    override fun close() =
        options.forEach(Stage::close)
}