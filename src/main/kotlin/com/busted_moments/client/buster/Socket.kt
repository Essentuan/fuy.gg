package com.busted_moments.client.buster

import com.busted_moments.buster.Buster
import com.busted_moments.buster.api.Account
import com.busted_moments.buster.exceptions.PacketFormatException
import com.busted_moments.buster.protocol.Packet
import com.busted_moments.client.Client
import com.busted_moments.client.buster.events.BusterEvent
import com.busted_moments.client.framework.events.post
import com.google.gson.stream.JsonReader
import com.wynntils.utils.mc.McUtils.mc
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.ClientWebSocketSession
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.suitableCharset
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import net.essentuan.esl.Result
import net.essentuan.esl.get
import net.essentuan.esl.json.Json
import net.essentuan.esl.model.Model.Companion.export
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

private val client = HttpClient {
    install(WebSockets) {
        extensions {
            install(Buster) {
                val profile = mc().gameProfile

                username = profile.name
                uuid = profile.id
                version = Buster.version
            }
        }
    }
}

class Socket(
    private val session: DefaultClientWebSocketSession
) : ClientWebSocketSession by session {
    private val charset = call.request.headers.suitableCharset()
    lateinit var account: Account

    val ready: Boolean
        get() = ::account.isInitialized

    suspend fun start() {
        try {
            for (frame in incoming)
                if (frame is Frame.Text)
                    launch {
                        process(frame)
                    }
        } catch (_: ClosedReceiveChannelException) {
            Client.info("Closed")
            //onClose
        } catch (ex: CancellationException) {
            Client.info("Cancelled")
            //onClose
        } catch (ex: Throwable) {
            Client.error("Uncaught exception in Socket!", ex)
        }
    }

    private suspend fun process(frame: Frame.Text) {
        val result = read(frame)

        if (result is Result.Fail<*>) {
            if (result.cause is PacketFormatException) {
                return
            }

            throw result.cause
        }

        val packet = result.get()

        try {
            if (!BusterEvent.Packet(this@Socket, packet).post())
                BusterService.listeners[packet.javaClass]?.invoke(this@Socket, packet)
        } catch (ex: Exception) {
            Client.error("Error handing $packet!", ex)
        }
    }

    suspend fun send(packet: Packet) {
        outgoing.send(
            Frame.Text(
                true,
                packet.export().asString().toByteArray(charset)
            )
        )
    }

    private fun read(frame: Frame.Text) = Packet(
        Json(
            JsonReader(
                InputStreamReader(
                    ByteArrayInputStream(frame.data), charset
                )
            )
        )
    )

    suspend fun close() {
        close(reason = CloseReason(CloseReason.Codes.NORMAL, ""))
    }
}