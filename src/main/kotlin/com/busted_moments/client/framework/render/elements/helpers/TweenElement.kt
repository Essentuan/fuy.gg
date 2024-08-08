@file:OptIn(ExperimentalTypeInference::class)

package com.busted_moments.client.framework.render.elements.helpers

import com.busted_moments.client.framework.render.Element
import com.busted_moments.client.framework.render.Renderer
import com.busted_moments.client.framework.render.helpers.Context
import net.essentuan.esl.time.duration.Duration
import kotlin.experimental.ExperimentalTypeInference

abstract class TweenElement<CTX : Context, T>(
    duration: Duration
) : Element<CTX>() {
    val mills = duration.toMills()
    var progress: Double = 0.0
        private set

    abstract val value: T

    override fun compute(ctx: CTX): Boolean {
        progress = (System.currentTimeMillis() % mills) / mills

        return true
    }
}

inline fun <CTX : Context, T> Renderer<CTX>.tween(
    over: Duration,
    range: ClosedRange<T>,
    crossinline plus: T.(T) -> T,
    crossinline minus: T.(T) -> T,
    crossinline mul: T.(Double) -> T,
    crossinline block: TweenElement<CTX, T>.(CTX) -> Boolean
) where T : Number, T : Comparable<T> {
    if (first)
        this += object : TweenElement<CTX, T>(over) {
            override lateinit var value: T
                private set

            override fun draw(ctx: CTX): Boolean {
                value = range.endInclusive.minus(range.start).mul(progress).plus(range.start)

                return block(ctx)
            }
        }
}

@JvmName("tweenInt")
@OverloadResolutionByLambdaReturnType
inline fun <CTX : Context> Renderer<CTX>.tween(
    over: Duration,
    range: ClosedRange<Int>,
    crossinline block: TweenElement<CTX, Int>.(CTX) -> Boolean
) = tween(
    over,
    range,
    Int::plus,
    Int::minus,
    { (this.toDouble() * it).toInt() },
    block
)

@JvmName("tweenUnitInt")
inline fun <CTX : Context> Renderer<CTX>.tween(
    over: Duration,
    range: ClosedRange<Int>,
    crossinline block: TweenElement<CTX, Int>.(CTX) -> Unit
) = tween(
    over,
    range,
    Int::plus,
    Int::minus,
    { (this.toDouble() * it).toInt() }
) ret@{
    block(it)

    return@ret true
}

@JvmName("tweenLong")
@OverloadResolutionByLambdaReturnType
inline fun <CTX : Context> Renderer<CTX>.tween(
    over: Duration,
    range: ClosedRange<Long>,
    crossinline block: TweenElement<CTX, Long>.(CTX) -> Boolean
) = tween(
    over,
    range,
    Long::plus,
    Long::minus,
    { (this.toDouble() * it).toLong() },
    block
)

@JvmName("tweenUnitLong")
inline fun <CTX : Context> Renderer<CTX>.tween(
    over: Duration,
    range: ClosedRange<Long>,
    crossinline block: TweenElement<CTX, Long>.(CTX) -> Unit
) = tween(
    over,
    range,
    Long::plus,
    Long::minus,
    { (this.toDouble() * it).toLong() }
) ret@{
    block(it)

    return@ret true
}

@JvmName("tweenFloat")
@OverloadResolutionByLambdaReturnType
inline fun <CTX : Context> Renderer<CTX>.tween(
    over: Duration,
    range: ClosedRange<Float>,
    crossinline block: TweenElement<CTX, Float>.(CTX) -> Boolean
) = tween(
    over,
    range,
    Float::plus,
    Float::minus,
    { (this.toDouble() * it).toFloat() },
    block
)

@JvmName("tweenUnitFloat")
inline fun <CTX : Context> Renderer<CTX>.tween(
    over: Duration,
    range: ClosedRange<Float>,
    crossinline block: TweenElement<CTX, Float>.(CTX) -> Unit
) = tween(
    over,
    range,
    Float::plus,
    Float::minus,
    { (this.toDouble() * it).toFloat() }
) ret@{
    block(it)

    return@ret true
}

@JvmName("tweenDouble")
@OverloadResolutionByLambdaReturnType
inline fun <CTX : Context> Renderer<CTX>.tween(
    over: Duration,
    range: ClosedRange<Double>,
    crossinline block: TweenElement<CTX, Double>.(CTX) -> Boolean
) = tween(
    over,
    range,
    Double::plus,
    Double::minus,
    Double::times,
    block
)

@JvmName("tweenUnitDouble")
inline fun <CTX : Context> Renderer<CTX>.tween(
    over: Duration,
    range: ClosedRange<Double>,
    crossinline block: TweenElement<CTX, Double>.(CTX) -> Unit
) = tween(
    over,
    range,
    Double::plus,
    Double::minus,
    Double::times
) ret@{
    block(it)

    return@ret true
}

//@OverloadResolutionByLambdaReturnType
//inline fun <CTX : Context> Renderer<CTX>.tween(
////    range: ClosedRange<Double>
//    crossinline block: TweenElement<CTX>.(CTX) -> Boolean
//) {
//    if (first)
//        this += object : ResetElement<CTX>() {
//            override fun compute(ctx: CTX): Boolean = block(ctx)
//        }
//}
//
//
//private fun <T> tween(
//    over: Duration
//) {
//
//}
//
//fun tween(over: Duration, range: ClosedRange<Double>) {
//
//}