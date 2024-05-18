package com.busted_moments.client.framework.render.helpers

private const val MASK: Long = 0xFFFFFFFF

@JvmInline
value class Floats(
    private val data: Long
) {
    constructor(
        first: Float,
        second: Float
    ) : this((first.toRawBits().toLong() shl 32) or second.toRawBits().toLong())

    operator fun component1(): Float = first

    operator fun component2(): Float = second

    val first: Float
        get() = Float.fromBits((data shr 32).toInt())

    val second: Float
        get() = Float.fromBits((data and MASK).toInt())

    fun copy(first: Float = this.first, second: Float = this.second) =
        Floats(first, second)

    override fun toString(): String {
        return "Floats[first=$first, second=$second]"
    }

    companion object {
        val ZERO = Floats(0f, 0f)
    }
}