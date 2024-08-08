package com.busted_moments.client.models.content

interface Tracker {
    val name: String
    val value: Double
    
    fun close()
    
    fun interface Builder {
        fun build(): Tracker
    }
}