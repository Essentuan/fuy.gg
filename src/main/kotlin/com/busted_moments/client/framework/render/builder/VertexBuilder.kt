package com.busted_moments.client.framework.render.builder

import com.busted_moments.client.framework.render.Vec3f
import net.essentuan.esl.color.Color
import net.essentuan.esl.tuples.numbers.FloatPair

interface VertexBuilder {
    var color: Color
    var uv: FloatPair
    var normal: Vec3f

    val shader: Shader

    fun begin(pos: Vec3f): Vertex

    fun begin(x: Float, y: Float, z: Float): Vertex =
        begin(Vec3f(x, y, z))

    fun end()
}

context(VertexBuilder)
inline fun vertex(pos: Vec3f, block: context(Vertex) () -> Unit) {
    val vertex = begin(pos)

    try {
        block(vertex)
    } finally {
        vertex.end()
    }
}

context(VertexBuilder)
inline fun vertex(x: Float, y: Float, z: Float, block: context(Vertex) () -> Unit) {
    val vertex = begin(x, y, z)

    try {
        block(vertex)
    } finally {
        vertex.end()
    }
}

context(VertexBuilder)
fun vertex(
    pos: Vec3f,
    color: Color = this@VertexBuilder.color,
    uv: FloatPair = this@VertexBuilder.uv,
    normal: Vec3f = this@VertexBuilder.normal
) {
    val vertex = begin(pos)

    vertex.color = color
    vertex.uv = uv
    vertex.normal = normal

    vertex.end()
}

context(VertexBuilder)
fun vertex(
    x: Number, y: Number, z: Number,
    color: Color = this@VertexBuilder.color,
    uv: FloatPair = this@VertexBuilder.uv,
    normal: Vec3f = this@VertexBuilder.normal
) {
    val vertex = begin(x.toFloat(), y.toFloat(), z.toFloat())

    vertex.color = color
    vertex.uv = uv
    vertex.normal = normal

    vertex.end()
}

