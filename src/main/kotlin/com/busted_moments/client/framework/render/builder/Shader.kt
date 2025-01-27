package com.busted_moments.client.framework.render.builder

import net.essentuan.esl.color.Color
import net.minecraft.client.renderer.ShaderProgram
import java.util.function.Supplier

interface Shader {
    operator fun invoke(instance: ShaderProgram?)

    var color: Color
}