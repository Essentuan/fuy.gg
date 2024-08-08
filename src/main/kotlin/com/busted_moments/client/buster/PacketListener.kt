package com.busted_moments.client.buster

import com.busted_moments.buster.protocol.Packet
import net.essentuan.esl.reflections.extensions.extends
import net.essentuan.esl.reflections.extensions.instance
import net.essentuan.esl.reflections.extensions.javaClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.jvm.isAccessible

class PacketListener(
    val function: KFunction<*>,
    val ref: Any?,
    val packet: Class<*>
) {
    init {
        function.isAccessible = true
    }

    val size = function.parameters.size

    suspend operator fun invoke(socket: Socket, packet: Packet) {
        val args = arrayOfNulls<Any?>(size)

        if (size == 3)
            args[0] = ref

        args[size - 2] = socket
        args[size - 1] = packet

        if (function.isSuspend)
            function.callSuspend(*args)
        else
            function.call(*args)
    }

    companion object {
        operator fun invoke(func: KFunction<*>): PacketListener? {
            if (func.parameters.size !in 2..3 || Socket::class.java != func.extensionReceiverParameter?.type?.javaClass)
                return null

            val type = func.parameters.firstOrNull { it.type.javaClass extends Packet::class }?.type?.javaClass ?: return null
            return if (func.parameters.size == 2)
                PacketListener(
                    func,
                    null,
                    type
                )
            else
                PacketListener(
                    func,
                    func.instanceParameter?.type?.javaClass?.instance ?: return null,
                    type
                )
        }
    }
}