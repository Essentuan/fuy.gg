package com.busted_moments.client.framework.config

import com.wynntils.utils.mc.McUtils.mc
import java.util.UUID
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class AccountDelegate<T> : ReadWriteProperty<Storage, T> {
    private var uuid: UUID? = null
    private var value: T? = null

    protected abstract fun default(): T

    override fun getValue(thisRef: Storage, property: KProperty<*>): T {
        val now = mc().user.profileId
        if (uuid != now) {
            uuid = now
            value = default()
        }

        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    override fun setValue(thisRef: Storage, property: KProperty<*>, value: T) {
        this.uuid = mc().user.profileId
        this.value = value
    }
}

inline fun <T> Storage.account(crossinline default: () -> T): ReadWriteProperty<Storage, T> =
    object : AccountDelegate<T>() {
        override fun default(): T =
            default()
    }