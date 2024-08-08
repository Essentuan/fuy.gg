package com.busted_moments.client.framework

import org.objenesis.Objenesis
import org.objenesis.ObjenesisStd

private typealias IObjenesis = Objenesis

object Objenesis : IObjenesis by ObjenesisStd() {
    inline operator fun <reified T> invoke(): T =
        newInstance(T::class.java)
}