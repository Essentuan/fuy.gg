package com.busted_moments.client.framework.render.builder

import com.busted_moments.client.framework.render.Vec3f
import net.essentuan.esl.color.Color
import net.essentuan.esl.tuples.numbers.FloatPair

interface Vertex {
    val pos: Vec3f

    var color: Color
    var uv: FloatPair
    var normal: Vec3f

    fun end()
}