package com.busted_moments.client.framework.render.builder

import net.essentuan.esl.color.Color
import net.minecraft.client.renderer.ShaderInstance
import java.util.function.Supplier

interface Shader {
    operator fun invoke(instance: ShaderInstance?) {
        this { instance }
    }

    operator fun invoke(supplier: Supplier<ShaderInstance?>)

    var color: Color
}