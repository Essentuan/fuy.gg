package com.busted_moments.client.models.content

import net.essentuan.esl.time.duration.Duration
import java.util.Date

interface Stage {
    val name: String
    val tracker: Tracker?
    
    val start: Date?
    val end: Date?
    
    val duration: Duration
    
    fun close()
    
    fun interface Builder {
        fun build(handler: Stage.() -> Unit): Stage
    }
}