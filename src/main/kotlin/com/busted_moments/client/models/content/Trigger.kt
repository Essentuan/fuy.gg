package com.busted_moments.client.models.content

interface Trigger {
    fun close()
    
    fun interface Builder {
        fun build(handler: () -> Unit): Trigger
    }
}