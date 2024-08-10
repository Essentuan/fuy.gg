package com.busted_moments.client.framework.config.encoders

import com.busted_moments.client.framework.text.Text
import com.wynntils.core.text.StyledText
import net.essentuan.esl.encoding.StringBasedEncoder
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Type

object StyledTextEncoder : StringBasedEncoder<StyledText>() {
    override fun encode(
        obj: StyledText,
        flags: Set<Any>,
        type: Class<*>,
        element: AnnotatedElement,
        vararg typeArgs: Type
    ): String? =
        obj.string

    override fun decode(
        obj: String,
        flags: Set<Any>,
        type: Class<*>,
        element: AnnotatedElement,
        vararg typeArgs: Type
    ): StyledText? =
        Text(obj)
}