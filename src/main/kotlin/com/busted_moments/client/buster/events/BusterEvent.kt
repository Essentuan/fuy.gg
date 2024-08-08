package com.busted_moments.client.buster.events

import com.busted_moments.buster.api.Account
import com.busted_moments.buster.protocol.Packet
import com.busted_moments.client.buster.Socket
import com.busted_moments.client.framework.events.Cancellable
import com.wynntils.core.events.EventThread
import net.neoforged.bus.api.Event


private typealias IPacket = Packet

abstract class BusterEvent(val socket: Socket) : Event() {
    @EventThread(EventThread.Type.ANY)
    class Packet(socket: Socket, val packet: IPacket) : BusterEvent(socket), Cancellable

    @EventThread(EventThread.Type.ANY)
    class Open(socket: Socket) : BusterEvent(socket)
    @EventThread(EventThread.Type.ANY)
    class Auth(socket: Socket, account: Account) : BusterEvent(socket)
    @EventThread(EventThread.Type.ANY)
    class Close(socket: Socket) : BusterEvent(socket)
}