package com.busted_moments.client.buster

import com.busted_moments.buster.Buster
import com.busted_moments.buster.Ray
import com.busted_moments.buster.api.Party
import com.busted_moments.buster.exceptions.RequestException
import com.busted_moments.buster.protocol.Packet
import com.busted_moments.buster.protocol.Request
import com.busted_moments.buster.protocol.Response
import com.busted_moments.buster.protocol.clientbound.ClientboundLoginPacket
import com.busted_moments.buster.protocol.requests.AuthRequest
import com.busted_moments.buster.protocol.serverbound.ContentStage
import com.busted_moments.buster.protocol.serverbound.ServerboundContentCompletion
import com.busted_moments.client.Client
import com.busted_moments.client.Patterns
import com.busted_moments.client.buster.events.BusterEvent
import com.busted_moments.client.framework.config.Storage
import com.busted_moments.client.framework.config.annotations.Category
import com.busted_moments.client.framework.config.annotations.Floating
import com.busted_moments.client.framework.config.entries.value.Value
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.events.post
import com.busted_moments.client.framework.text.FUY_PREFIX
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.text.Text.invoke
import com.busted_moments.client.framework.text.Text.matches
import com.busted_moments.client.framework.text.Text.send
import com.busted_moments.client.framework.text.Text.unwrap
import com.busted_moments.client.framework.text.get
import com.busted_moments.client.framework.text.text
import com.busted_moments.client.framework.wynntils.Ticks
import com.busted_moments.client.models.content.event.ContentEvent
import com.busted_moments.client.models.content.stages.TextStage
import com.wynntils.core.components.Models
import com.wynntils.core.text.StyledText
import com.wynntils.core.text.StyledTextPart
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent
import com.wynntils.mc.event.ConnectionEvent.DisconnectedEvent
import com.wynntils.models.worlds.event.WorldStateEvent
import com.wynntils.models.worlds.type.WorldState
import com.wynntils.utils.mc.McUtils.mc
import com.wynntils.utils.mc.StyledTextUtils
import com.wynntils.utils.type.IterationDecision
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import kotlinx.coroutines.launch
import net.essentuan.esl.collections.synchronized
import net.essentuan.esl.coroutines.delay
import net.essentuan.esl.other.lock
import net.essentuan.esl.reflections.Reflections
import net.essentuan.esl.rx.toList
import net.essentuan.esl.rx.toSet
import net.essentuan.esl.scheduling.annotations.Auto
import net.essentuan.esl.time.duration.seconds
import net.minecraft.network.chat.HoverEvent
import java.util.EnumSet
import java.util.LinkedList
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val BUSTER_URL = "wss://thesimpleones.net/buster"

private val client = HttpClient {
    install(WebSockets) {
        extensions {
            install(Buster) {
                val profile = mc().user

                username = profile.name
                uuid = profile.profileId
                version = Buster.version
            }
        }
    }
}

@Auto(true)
@Category("Buster")
object BusterService : Storage {
    private val DISALLOWED_STATES = EnumSet.of(
        WorldState.NOT_CONNECTED,
        WorldState.CONNECTING,
        WorldState.HUB
    )

    internal val listeners = Reflections.functions
        .annotatedWith(Listener::class)
        .map { PacketListener(it) }
        .filterNotNull()
        .distinctBy { it.packet }
        .associateBy { it.packet }

    private val continuations = LinkedList<Continuation<Socket>>()

    private val pending = mutableMapOf<Ray, Continuation<Response>>().synchronized()
    private var attempts: Int = 0

    @Floating
    @Value("Enabled")
    private var enabled: Boolean = true
        set(value) {
            if (field != value)
                field = value

            if (value) {
                if (Models.WorldState.currentState in DISALLOWED_STATES || waiting)
                    return

                spin()
            } else
                socket?.launch { socket?.close() }
        }

    @Floating
    @Value("Verbose")
    private var verbose: Boolean = false

    private var waiting: Boolean = false
    private var socket: Socket? = null

    val ready: Boolean
        get() = socket?.ready == true

    @Subscribe
    private fun ContentEvent.Finish.on() {
        client.launch {
            try {
                send(
                    ServerboundContentCompletion(
                        timer.type.id,
                        timer.type.print(),
                        timer.party,
                        timer.start!!,
                        timer.filterNot { it is TextStage }.map { ContentStage(it.name, it.start!!, it.end!!) },
                        timer.end!!,
                        timer.modifiers().await()
                    )
                )
            } catch(ex: Exception) {
                Client.error("Error sending content completion packet packet", ex)
            }
        }
    }


    @Synchronized
    fun offer(continuation: Continuation<Socket>) {
        if (socket != null)
            continuation.resume(socket!!)
        else
            continuations.offer(continuation)
    }

    suspend fun send(packet: Packet) =
        await().send(packet)

    @Synchronized
    internal fun spin() {
        if (waiting || !enabled || socket != null)
            return

        waiting = true

        client.launch {
            try {
                val seconds = attempts++ * 2.5
                if (seconds != 0.0)
                    delay(seconds.coerceAtMost(20.0).seconds)

                if (verbose)
                    FUY_PREFIX {
                        +"Connecting to Buster.".yellow
                    }.send()

                client.webSocket(BUSTER_URL) {
                    FUY_PREFIX {
                        +"Logging into Buster.".yellow
                    }.send()

                    socket = Socket(this)

                    lock {
                        while (continuations.isNotEmpty())
                            continuations.poll().resume(socket ?: break)
                    }

                    BusterEvent.Open(socket!!).post()

                    socket!!.start()
                }

                BusterEvent.Close(socket ?: return@launch).post()
            } catch (ex: Exception) {
                error("Error while connecting to Buster!", true)
                Client.error("Error while connecting to Buster!", ex)
            }
        }
    }

    @Listener
    private suspend fun Socket.on(packet: ClientboundLoginPacket) {
        mc().minecraftSessionService.joinServer(
            mc().user.profileId,
            mc().user.accessToken,
            packet.id
        )

        val account = AuthRequest().execute()
        if (account == null) {
            close()
            return
        }

        socket?.also {
            it.account = account
            BusterEvent.Auth(it, account).post()
        }

        FUY_PREFIX {
            +"Successfully logged into buster!".green
        }.send()
    }

    suspend fun <T> Request<T>.execute(): T? {
        send(this)

        val response: Response = suspendCoroutine { pending[ray] = it }

        if (response.status == Response.ERROR)
            throw RequestException(response.payload as? String ?: "")

        try {
            return wrap(response)
        } catch (ex: Exception) {
            throw ex
        }
    }

    @Subscribe
    private fun BusterEvent.Packet.onResponse() {
        if (packet !is Response)
            return

        pending.remove(packet.ray)?.resume(packet)

        isCanceled = true
    }

    private fun error(message: String = "You have been disconnected from Buster!", debug: Boolean = false) {
        if (!debug || verbose)
            FUY_PREFIX {
                +message.red
            }.send()

        this@BusterService.socket = null
        waiting = false

        if (Models.WorldState.currentState !in DISALLOWED_STATES)
            spin()
    }

    @Subscribe
    @Synchronized
    private fun BusterEvent.Close.on() =
        error()

    @Subscribe
    private fun WorldStateEvent.on() {
        if (newState in DISALLOWED_STATES || waiting)
            return

        spin()
    }

    @Subscribe
    private fun DisconnectedEvent.on() {
        socket?.apply {
            launch {
                close()
                error()
            }
        }
    }

    suspend fun await(): Socket {
        return socket ?: suspendCoroutine { offer(it) }
    }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Listener