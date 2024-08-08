package com.busted_moments.client.models.content.stages

import com.busted_moments.client.framework.text.StyleType
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.models.content.Stage
import com.busted_moments.client.models.content.Tracker
import com.busted_moments.client.models.content.Trigger
import com.busted_moments.client.models.content.triggers.Triggers
import com.wynntils.core.text.StyledText
import net.minecraft.core.Position
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec2
import java.util.regex.Pattern

object Stages {
    fun generic(name: String, trigger: Trigger.Builder, tracker: Tracker? = null) =
        Stage.Builder { GenericStage(name, trigger, tracker, it)  }
    
    fun multi(vararg options: Stage.Builder) =
        Stage.Builder { Multistage(options, it)  }
    
    fun enters(name: String, start: Position, end: Position, tracker: Tracker? = null) =
        generic(name, Triggers.enters(start, end), tracker)

    fun enters(name: String, start: Vec2, end: Vec2, tracker: Tracker? = null) =
        generic(name, Triggers.enters(start, end), tracker)

    fun leaves(name: String, start: Position, end: Position, tracker: Tracker? = null) =
        generic(name, Triggers.leaves(start, end), tracker)
    
    fun leaves(name: String, start: Vec2, end: Vec2, tracker: Tracker? = null) =
        generic(name, Triggers.leaves(start, end), tracker)

    fun entersSphere(name: String, pos: Position, radius: Double, tracker: Tracker? = null) =
        generic(name, Triggers.entersSphere(pos, radius), tracker)

    fun leavesSphere(name: String, pos: Position, radius: Double, tracker: Tracker? = null) =
        generic(name, Triggers.leavesSphere(pos, radius), tracker)

    fun title(name: String, text: StyledText, tracker: Tracker? = null) =
        generic(name, Triggers.title(text), tracker)

    fun title(name: String, text: String, style: StyleType, tracker: Tracker? = null) =
        generic(name, Triggers.title(text, style), tracker)

    fun title(name: String, pattern: Pattern, style: StyleType, tracker: Tracker? = null) =
        generic(name, Triggers.title(pattern, style), tracker)

    fun subtitle(name: String, text: StyledText, tracker: Tracker? = null) =
        generic(name, Triggers.subtitle(text), tracker)
    
    fun subtitle(name: String, text: String, style: StyleType, tracker: Tracker? = null) =
        generic(name, Triggers.subtitle(text, style), tracker)
    
    fun subtitle(name: String, pattern: Pattern, style: StyleType, tracker: Tracker? = null) =
        generic(name, Triggers.subtitle(pattern, style), tracker)

    fun chatMessage(name: String, text: StyledText, tracker: Tracker? = null) =
        generic(name, Triggers.chatMessage(text), tracker)

    fun chatMessage(name: String, text: String, style: StyleType, tracker: Tracker? = null) =
        generic(name, Triggers.chatMessage(text, style), tracker)

    fun chatMessage(name: String, pattern: Pattern, style: StyleType, tracker: Tracker? = null) =
        generic(name, Triggers.chatMessage(pattern, style), tracker)

    fun entity(name: String, text: StyledText, type: EntityType<*>? = null) =
        generic(name, Triggers.entity(text, type))

    fun entity(name: String, text: String, style: StyleType = StyleType.NONE, type: EntityType<*>? = null) =
        generic(name, Triggers.entity(text, style, type))

    fun entity(name: String, pattern: Pattern, style: StyleType = StyleType.NONE, type: EntityType<*>? = null) =
        generic(name, Triggers.entity(pattern, style, type))

    inline fun text(crossinline block: Text.Builder.() -> Unit): Stage.Builder =
        TextStage(Text(block))

    fun lazy(
        placeholder: String,
        trigger: Trigger.Builder,
        stage: Stage.Builder,
    ): Stage.Builder =
        Stage.Builder { LazyStage(placeholder, trigger, stage, it) }
}