package com.busted_moments.client.models.content.triggers

import com.busted_moments.client.framework.text.StyleType
import com.busted_moments.client.models.content.Trigger
import com.wynntils.core.text.PartStyle
import com.wynntils.core.text.StyledText
import net.minecraft.core.Position
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import java.util.regex.Pattern

object Triggers {
    fun enters(start: Position, end: Position) =
        Trigger.Builder { BoxTrigger(start, end, true, it) }

    fun enters(start: Vec2, end: Vec2) =
        Trigger.Builder {
            BoxTrigger(
                Vec3(
                    start.x.toDouble(),
                    Double.MIN_VALUE,
                    start.y.toDouble()
                ),
                Vec3(
                    end.x.toDouble(),
                    Double.MAX_VALUE,
                    end.y.toDouble()
                ),
                true,
                it
            )
        }

    fun leaves(start: Position, end: Position) =
        Trigger.Builder { BoxTrigger(start, end, false, it) }
    
    fun leaves(start: Vec2, end: Vec2) =
        Trigger.Builder {
            BoxTrigger(
                Vec3(
                    start.x.toDouble(),
                    Double.MIN_VALUE,
                    start.y.toDouble()
                ),
                Vec3(
                    end.x.toDouble(),
                    Double.MAX_VALUE,
                    end.y.toDouble()
                ),
                false,
                it
            )
        }


    fun entersSphere(pos: Position, radius: Double) =
        Trigger.Builder { SphereTrigger(pos, radius, true, it) }

    fun leavesSphere(pos: Position, radius: Double) =
        Trigger.Builder { SphereTrigger(pos, radius, false, it) }

    fun title(text: StyledText) =
        Trigger.Builder { TitleTrigger(text, PartStyle.StyleType.DEFAULT, it) }

    fun title(text: String, style: StyleType = StyleType.NONE) =
        Trigger.Builder { TitleTrigger(text, style, it) }

    fun title(pattern: Pattern, style: StyleType = StyleType.NONE) =
        Trigger.Builder { TitleTrigger(pattern, style, it) }

    fun subtitle(text: StyledText) =
        Trigger.Builder { SubtitleTrigger(text, PartStyle.StyleType.DEFAULT, it) }

    fun subtitle(text: String, style: StyleType = StyleType.NONE) =
        Trigger.Builder { SubtitleTrigger(text, style, it) }

    fun subtitle(pattern: Pattern, style: StyleType = StyleType.NONE) =
        Trigger.Builder { SubtitleTrigger(pattern, style, it) }

    fun chatMessage(text: StyledText) =
        Trigger.Builder { ChatTrigger(text, PartStyle.StyleType.DEFAULT, it) }

    fun chatMessage(text: String, style: StyleType = StyleType.NONE) =
        Trigger.Builder { ChatTrigger(text, style, it) }

    fun chatMessage(pattern: Pattern, style: StyleType = StyleType.NONE) =
        Trigger.Builder { ChatTrigger(pattern, style, it) }

    fun entity(text: StyledText, type: EntityType<*>? = null) =
        Trigger.Builder { EntitySpawnTrigger(text, StyleType.DEFAULT, type, it) }

    fun entity(text: String, style: StyleType = StyleType.NONE, type: EntityType<*>? = null) =
        Trigger.Builder { EntitySpawnTrigger(text, style, type, it) }

    fun entity(pattern: Pattern, style: StyleType = StyleType.NONE, type: EntityType<*>? = null) =
        Trigger.Builder { EntitySpawnTrigger(pattern, style, type, it) }

    fun death() =
        Trigger.Builder { DeathTrigger(it) }

    fun counting(trigger: Trigger.Builder, count: Int) =
        Trigger.Builder { CountingTrigger(trigger, count, it) }
    
    fun immediate(): Trigger.Builder =
        ImmediateTrigger
    
    fun nothing(): Trigger.Builder =
        NothingTrigger
}