package com.busted_moments.client.models.content.triggers

import com.busted_moments.client.framework.text.StyleType
import com.busted_moments.client.models.content.Trigger
import com.wynntils.core.text.StyledText
import java.util.regex.Pattern

abstract class TextTrigger(
    private val predicate: Any,
    val style: StyleType,
    val handler: () -> Unit
) : Trigger {
    protected fun test(text: StyledText) {
        when  {
            predicate is StyledText && text == predicate ->
                handler()
            
            predicate is String && text.equalsString(predicate, style) ->
                handler()
            
            predicate is Pattern && text.matches(predicate, style) ->
                handler()
        }
    }
}
