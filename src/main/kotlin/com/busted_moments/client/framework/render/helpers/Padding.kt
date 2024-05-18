package com.busted_moments.client.framework.render.helpers

data class Padding(
    var left: Float,
    var top: Float,
    var right: Float,
    var bottom: Float,
) {
    fun all(number: Float) {
        left = number
        top = number
        right = number
        bottom = number
    }
}