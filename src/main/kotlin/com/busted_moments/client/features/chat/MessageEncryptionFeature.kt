package com.busted_moments.client.features.chat

import com.busted_moments.client.Client
import com.busted_moments.client.framework.Commands
import com.busted_moments.client.framework.config.annotations.Category
import com.busted_moments.client.framework.config.annotations.Persistent
import com.busted_moments.client.framework.config.annotations.Tooltip
import com.busted_moments.client.framework.config.entries.value.Value
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.features.Feature
import com.busted_moments.client.framework.text.FUY_PREFIX
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.text.Text.invoke
import com.busted_moments.client.framework.text.Text.send
import com.busted_moments.client.framework.text.Text.unwrap
import com.busted_moments.client.framework.text.text
import com.mojang.brigadier.StringReader
import com.wynntils.core.components.Managers
import com.wynntils.core.text.StyledTextPart
import com.wynntils.features.chat.ChatItemFeature
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent
import com.wynntils.mc.event.ChatSentEvent
import com.wynntils.mc.event.CommandSentEvent
import com.wynntils.utils.EncodedByteBuffer
import com.wynntils.utils.mc.McUtils
import com.wynntils.utils.type.IterationDecision
import com.wynntils.utils.type.UnsignedByte
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.neoforged.bus.api.EventPriority
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

private val HEADER = byteArrayOf(69, -35, -41)
private const val AES = "AES/GCM/NoPadding"

private const val MESSAGE_START = "$-"
private const val MESSAGE_END = '!'

private val RANDOM = SecureRandom()

private val ITEM_FEATURE: ChatItemFeature by lazy {
    Managers.Feature.getFeatureInstance(ChatItemFeature::class.java)
}

@Category("Chat")
@Tooltip(
    [
        "Encrypts messages with a prefix",
        "Example: /p @Lorem ipsum dolor sit amet, consectetur adipiscing elit"
    ]
)
object MessageEncryptionFeature : Feature() {
    @Persistent
    private var hasSentWarning: Boolean = false

    @Value("Encryption Key")
    @Tooltip(["The key to use when encrypting messages"])
    private var encryptionKey: String = ""
        set(value) {
            field = value
            hasSentWarning = false
            secret = pad(encryptionKey)
        }

    @Value("Salt Length", intMin = 4, intMax = 16)
    @Tooltip(["Salt length in bytes"])
    private var ivLength: Int = 4

    @Value("Encrypt Prefix")
    @Tooltip(["The prefix used when you want to encrypt a message"])
    private var prefix: String = "@"

    @Value("Replace invalid messages")
    @Tooltip(["Replaces messages that could not be decrypted with an error"])
    private var replaceBadMessages: Boolean = true

    private var secret: ByteArray =
        pad(encryptionKey)

    private fun encrypt(string: String): String {
        val cipher = Cipher.getInstance(AES)
        val secret = SecretKeySpec(secret, "AES")
        val iv = ByteArray(ivLength)
        RANDOM.nextBytes(iv)

        cipher.init(
            Cipher.ENCRYPT_MODE,
            secret,
            if (ivLength == 16)
                GCMParameterSpec(128, iv)
            else
                GCMParameterSpec(128, iv.copyOf(16))
        )

        cipher.updateAAD(HEADER)

        val encrypted = cipher.doFinal(string.trim().toByteArray())

        return buildString {
            append(MESSAGE_START)
            encode(iv)
            encode(encrypted)
            append(MESSAGE_END)
        }
    }

    private fun decrypt(message: String): String {
        val bytes = decode(message)

        val cipher = Cipher.getInstance(AES)
        val secret = SecretKeySpec(secret, "AES")
        val iv = bytes.copyOfRange(0, ivLength)

        cipher.init(
            Cipher.DECRYPT_MODE,
            secret,
            if (ivLength == 16)
                GCMParameterSpec(128, iv)
            else
                GCMParameterSpec(128, iv.copyOf(16))
        )

        cipher.updateAAD(HEADER)

        val decrypted = cipher.doFinal(bytes, ivLength, bytes.size - ivLength)

        return decrypted.decodeToString()
    }

    @Subscribe
    private fun ChatSentEvent.on() {
        if (prefix.isEmpty()) return

        val index = indexOfPrefix(message)
        if (index == -1) {
            if (message.contains(prefix)) {
                isCanceled = true
                McUtils.sendChat(message.replace(Regex("\\\\.")) { it.groupValues[1] })
            }

            return
        }

        if (secret.isEmpty()) {
            if (!hasSentWarning) {
                FUY_PREFIX {
                    +"WARNING: You must set a secret before you can encrypt messages...".red
                }.send()

                hasSentWarning = true
                isCanceled = true
            }

            return
        }

        val messageStart = index + prefix.length - 1

        val result = message.replaceRange(
            messageStart..<message.length,
            encrypt(message.substring(messageStart + 1))
        )

        isCanceled = true

        if (result.length < 256)
            McUtils.sendChat(result)
        else
            FUY_PREFIX {
                +"That message is too".gray
                +" long!".aqua
                +" and cannot be encrypted!".gray
            }.send()
    }

    @Subscribe(priority = EventPriority.HIGHEST)
    private fun CommandSentEvent.on() {
        if (prefix.isEmpty()) return

        val parts = command.split(' ', limit = 2)
        if (parts.size < 2) return

        val index = indexOfPrefix(parts[1])
        if (index == -1) {
            if (parts[1].contains(prefix)) {
                isCanceled = true
                Commands.execute(parts[0] + " " + parts[1].replace(Regex("\\\\(.)")) { it.groupValues[1] })
            }

            return
        }

        if (secret.isEmpty()) {
            if (!hasSentWarning) {
                FUY_PREFIX {
                    +"WARNING: You must set a secret before you can encrypt messages...".red
                }.send()

                hasSentWarning = true
                isCanceled = true
            }

            return
        }

        val messageStart = index + prefix.length - 1

        isCanceled = true
        Commands.execute(
            parts[0] + " " + parts[1].replaceRange(
                messageStart..<parts[1].length,
                encrypt(parts[1].substring(messageStart + 1))
            )
        )
    }

    @Subscribe(priority = EventPriority.HIGHEST)
    private fun ChatMessageReceivedEvent.on() {
        //Unfortunately, we need to avoid conflicts with ChatItemFeature
        val message = originalStyledText.unwrap()

        var changed = false

        val modified = message.iterate { part, changes ->
            val start = part.text.indexOf(MESSAGE_START)
            val end = part.text.indexOf(MESSAGE_END)

            if (start == -1 || end == -1 || start >= end)
                return@iterate IterationDecision.CONTINUE

            try {
                val decrypted = decrypt(part.text.substring(start + 2, end))

                changed = true

                changes.remove(part)

                changes.add(
                    StyledTextPart(
                        part.text.replaceRange(start..end, decrypted),
                        part.partStyle.style
                            .withUnderlined(true)
                            .withHoverEvent(
                                HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Text.component {
                                        +"This was an encrypted message".gray
                                    }
                                )
                            ),
                        null,
                        null
                    )
                )
            } catch (ex: Exception) {
                if (replaceBadMessages) {
                    changes.remove(part)
                    changes.add(
                        StyledTextPart(
                            "Could not decrypt message!",
                            Style.EMPTY
                                .withColor(ChatFormatting.RED)
                                .withUnderlined(true)
                                .withHoverEvent(
                                    HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Text.component {
                                            +"Could not decrypt '".gray
                                            +part.component
                                            +"'".gray
                                        }
                                    )
                                ),
                            null,
                            null
                        )
                    )
                }

                Client.info("Failed to decrypt message", ex)
            }

            IterationDecision.CONTINUE
        }

        if (changed) {
            setMessage(modified)

            ITEM_FEATURE.onChatReceived(this)
        }
    }

    private fun indexOfPrefix(message: String): Int {
        var cursor = -1
        var index = 0
        val reader = StringReader(message)
        while (reader.canRead()) {
            val c = reader.read()
            cursor++

            when (c) {
                '\\' -> {
                    if (reader.canRead()) {
                        reader.skip()
                        cursor++
                    }

                    index = 0
                }

                prefix[index] -> {
                    index++

                    if (index >= prefix.length)
                        break
                }

                else -> {
                    index = 0
                }
            }
        }

        return if (index >= prefix.length)
            cursor
        else
            -1
    }
}

private fun StringBuilder.encode(bytes: ByteArray) {
    append(
        EncodedByteBuffer.fromBytes(
            Array<UnsignedByte>(bytes.size) {
                UnsignedByte.of(bytes[it])
            }
        ).toUtf16String()
    )
}

private fun decode(string: String): ByteArray {
    val uBytes = EncodedByteBuffer.fromUtf16String(string).bytes

    return ByteArray(uBytes.size) {
        uBytes[it].toByte()
    }
}

private fun pad(string: String): ByteArray {
    if (string.isEmpty())
        return ByteArray(0)

    val bytes = string.toByteArray()
    val i = bytes.size

    return bytes.copyOf(
        max(2.0.pow(32 - Integer.numberOfLeadingZeros(i - 1)).toInt(), 16)
    )
}
