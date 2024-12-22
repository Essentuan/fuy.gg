package com.busted_moments.client.features

import com.busted_moments.client.Client
import com.busted_moments.client.Patterns
import com.busted_moments.client.events.EntityEvent
import com.busted_moments.client.features.LootrunDryStreakFeature.History.items
import com.busted_moments.client.framework.Entities.isInside
import com.busted_moments.client.framework.config.LegacyConfig
import com.busted_moments.client.framework.config.Storage
import com.busted_moments.client.framework.config.annotations.File
import com.busted_moments.client.framework.config.annotations.Persistent
import com.busted_moments.client.framework.config.annotations.Skip
import com.busted_moments.client.framework.config.entries.dropdown.Dropdown
import com.busted_moments.client.framework.config.entries.value.Value
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.features.Feature
import com.busted_moments.client.framework.sounds.Sounds
import com.busted_moments.client.framework.sounds.Sounds.play
import com.busted_moments.client.framework.text.FUY_PREFIX
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.text.Text.invoke
import com.busted_moments.client.framework.text.Text.matches
import com.busted_moments.client.framework.text.Text.send
import com.busted_moments.client.framework.text.TextPart
import com.busted_moments.client.framework.text.get
import com.busted_moments.client.framework.util.Numbers.escapeCommas
import com.busted_moments.client.framework.util.Numbers.toCommaString
import com.busted_moments.client.framework.wynntils.Ticks
import com.busted_moments.client.framework.wynntils.registry
import com.mojang.brigadier.StringReader
import com.wynntils.core.components.Models
import com.wynntils.core.text.StyledText
import com.wynntils.mc.event.PlayerInteractEvent
import com.wynntils.mc.event.TickEvent
import com.wynntils.models.containers.event.MythicFoundEvent
import com.wynntils.models.gear.type.GearTier
import com.wynntils.models.items.items.game.GearItem
import com.wynntils.models.items.items.game.InsulatorItem
import com.wynntils.models.items.items.game.SimulatorItem
import com.wynntils.models.worlds.event.WorldStateEvent
import com.wynntils.overlays.minimap.MinimapOverlay
import com.wynntils.utils.mc.McUtils.mc
import com.wynntils.utils.mc.McUtils.player
import net.essentuan.esl.json.Json
import net.essentuan.esl.model.annotations.Ignored
import net.essentuan.esl.other.Base64
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.Tag
import net.minecraft.nbt.TagParser
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.HoverEvent.ItemStackInfo
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.monster.Slime
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.phys.Vec3
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.ICancellableEvent
import org.joml.Vector2d
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

private const val MAX_DISTANCE = 0.5

object LootrunDryStreakFeature : Feature() {
    @Dropdown("Selected Sound")
    private var sound: SoundEvent = Sounds.MYTHIC_FOUND

    @Value("Source")
    private var source: SoundSource = SoundSource.AMBIENT

    @Value("Sound volume")
    private var volume: Float = 1.0f

    @Value("Speed")
    private var speed: Float = 1.0f

    @Persistent
    var dry: Int = 0
        private set

    val pulls: List<RewardPull>
        get() = History.items

    private var chest: Vector2d? = null
    private var count: Int = 0
    private var waitingForPull: Boolean = false
    private var started: Boolean = false

    private var onMap: Boolean = true

    private val processed = mutableSetOf<UUID>()

    @Subscribe
    private fun PlayerInteractEvent.InteractAt.on() {
        if (!onMap)
            return

        if (target !is Slime || distance(target) > 0.5)
            return

        started = true
        waitingForPull = true
    }

    @Subscribe
    private fun EntityEvent.SetData.on() {
        if (!onMap)
            return

        when (entity) {
            is Display.TextDisplay -> {
                Text(entity.text ?: return)
                    .split("\n")
                    .forEach {
                        it.matches {
                            Patterns.REWARD_PULLS {
                                chest = Vector2d(entity.x, entity.z)
                                count = group("pulls")!!.toInt()

                                return
                            }
                        }
                    }
            }
        }
    }

    @Subscribe
    private fun EntityEvent.Spawn.on() {
        if (!onMap)
            return

        when (entity) {
            is ItemEntity -> {
                if (entity.uuid !in processed && chest != null && distance(entity) < MAX_DISTANCE) {
                    if (waitingForPull) {
                        waitingForPull = false
                        dry += count

                        Ticks.schedule(5) {
                            waitingForPull = true
                            processed.clear()
                        }
                    }

                    processed += entity.uuid

                    if (entity.item.isMythic) {
                        val pull = RewardPull(dry, entity.item)
                        items.add(pull)

                        dry = 0

                        sound.play(
                            source = source,
                            volume = volume,
                            speed = speed
                        )

                        val name = Text(entity.item.hoverName)

                        FUY_PREFIX {
                            +"You have found a".lightPurple

                            if (name.stringWithoutFormatting[0].isVowel)
                                +"n"

                            +" "

                            for (part in name) {
                                +TextPart(part).underline.onHover(
                                    HoverEvent.Action.SHOW_ITEM,
                                    ItemStackInfo(entity.item)
                                )
                            }

                            +" after ".reset.lightPurple
                            +pull.pulls.toCommaString().escapeCommas().gold
                            +" pulls!".lightPurple
                        }.send()
                    }
                }
            }
        }
    }

    @Subscribe
    private fun WorldStateEvent.on() =
        reset()

    @Subscribe
    private fun TickEvent.on() {
        onMap = player().isInside(Vec3(-2883.0, -1000.0, -7608.0), Vec3(2930.0, 1000.0, 994.0))

        if (chest != null && Models.WorldState.onWorld() && started && distance(player()) >= 15)
            reset()
    }

    @Subscribe(EventPriority.HIGHEST)
    private fun MythicFoundEvent.on() {
        if (isLootrunEndReward)
            (this as ICancellableEvent).isCanceled = true
    }

    @Subscribe
    private fun LegacyConfig.ImportEvent.on() {
        val drystreakFeature = toml.getTable("\"lootrun dry streak\"")?.getTable("lootrundrystreakfeature")
        val pulls = drystreakFeature?.getList<HashMap<String, Any?>>("mythics") ?: emptyList()

        for (pull in pulls) {
            val string = pull["item"] as String
            val tag = TagParser.parseTag(if (string[0] == '{') string else Base64.decode(string))
            val item = ItemStack.parse(mc().registry, tag).getOrNull() ?: continue
            val display = tag.getCompound("tag").getCompound("display")

            item.set(
                DataComponents.LORE,
                ItemLore(
                    display
                        .getList("Lore", 8)
                        .asSequence()
                        .map<Tag, Component?> {
                            Component.Serializer.fromJson(StringReader(it.toString()).readString(), mc().registry)
                        }.filterNotNull()
                        .toMutableList()
                )
            )

            item.set(
                DataComponents.CUSTOM_NAME,
                Component.Serializer.fromJson(StringReader(display["Name"]!!.toString()).readString(), mc().registry)
                    ?: continue
            )

            items += RewardPull(
                (pull["pulls"] as Long).toInt(),
                item
            )
        }

        dry = drystreakFeature?.getLong("pullssincelastmythic")?.toInt() ?: 0
    }

    private fun reset() {
        if (chest != null && dry != 0)
            FUY_PREFIX {
                +"You've gone ".lightPurple
                +dry.toCommaString().escapeCommas().gold
                +" pulls without finding a ".lightPurple
                +"Mythic".darkPurple
                +".".lightPurple
            }.send()

        chest = null
        started = false
        waitingForPull = false
        count = 0
    }

    private val ItemStack.isMythic: Boolean
        get() =
            when (val item = Models.Item.getWynnItem(this).getOrNull()) {
                is GearItem -> item.gearTier == GearTier.MYTHIC
                is InsulatorItem -> true
                is SimulatorItem -> true
                else -> false
            }

    private val Char.isVowel: Boolean
        get() = when (this) {
            'a', 'e', 'i', 'o', 'u', 'y' -> true
            else -> false
        }

    private fun distance(entity: Entity): Double {
        return chest?.distance(Vector2d(entity.x, entity.z)) ?: 0.0
    }

    @Skip
    @File("pulls")
    private object History : Storage {
        @Persistent
        var items: MutableList<RewardPull> = mutableListOf()
    }
}

data class RewardPull(
    val pulls: Int,
    val name: StyledText,
    val lore: List<StyledText>
) : Json.Model {
    constructor(pulls: Int, item: ItemStack) : this(
        pulls,
        Text(item.hoverName),
        item.get(DataComponents.LORE)?.lines?.map { Text(it) } ?: emptyList()
    )

    @Ignored
    val item: ItemStack by lazy {
        val stack = ItemStack(Items.STONE)

        stack.set(
            DataComponents.CUSTOM_NAME,
            name.component
        )

        stack.set(
            DataComponents.LORE,
            ItemLore(lore.map { it.component })
        )

        stack
    }
}