package com.busted_moments.client.framework.config.entries.value

import com.busted_moments.client.framework.artemis.esl
import com.wynntils.utils.colors.CustomColor
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder
import net.essentuan.esl.color.Color
import net.essentuan.esl.encoding.StringBasedEncoder
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Type
import kotlin.reflect.KProperty

class ColorValue(
    kotlin: KProperty<Color?>,
    annotation: Value
) : Value.Entry<Color>(kotlin, annotation) {
    private val useAlpha: Boolean = annotation.alpha

    @Suppress("UNCHECKED_CAST")
    override fun Color.open(builder: ConfigEntryBuilder): AbstractFieldBuilder<Color, *, *> {
        return builder.startColorField(title, asInt()).setAlphaMode(useAlpha) as AbstractFieldBuilder<Color, *, *>
    }

    override fun mutate(value: Any?): Color? {
        val color = (value as? me.shedaniel.math.Color) ?: return null

        Color

        return CustomColor(
            color.red,
            color.green,
            color.blue,
            color.alpha
        ).esl
    }
}

object ColorEncoder : StringBasedEncoder<Color>() {
    override fun decode(obj: String, type: Class<*>, element: AnnotatedElement, vararg typeArgs: Type): Color =
        CustomColor.fromHexString(obj).esl

    override fun encode(obj: Color, type: Class<*>, element: AnnotatedElement, vararg typeArgs: Type): String =
        obj.asHex()
}