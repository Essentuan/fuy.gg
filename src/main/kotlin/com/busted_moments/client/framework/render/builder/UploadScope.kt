package com.busted_moments.client.framework.render.builder

import com.busted_moments.client.framework.render.Texture
import com.busted_moments.client.framework.render.Vec3f
import com.busted_moments.client.framework.render.helpers.Context
import com.busted_moments.client.framework.wynntils.esl
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.wynntils.utils.colors.CommonColors
import com.wynntils.utils.render.buffered.CustomRenderType
import net.essentuan.esl.color.Color
import net.essentuan.esl.tuples.numbers.FloatPair
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.ShaderInstance
import java.io.Closeable
import java.util.function.Supplier

interface UploadScope : Closeable {
    val pose: PoseStack

    fun with(type: RenderType): VertexBuilder

    operator fun RenderMode.unaryPlus()

    operator fun RenderMode.unaryMinus()

    fun endBatch()

    companion object {
        fun begin(stack: PoseStack, buffer: MultiBufferSource.BufferSource): UploadScope {
            UploadScopeImpl.begin(buffer, stack)

            return UploadScopeImpl
        }
    }
}

inline fun upload(
    buffer: MultiBufferSource.BufferSource,
    stack: PoseStack = PoseStack(),
    block: context(UploadScope) () -> Unit
) {
    UploadScope.begin(stack, buffer).use(block)
}

inline fun upload(ctx: Context, block: context(UploadScope) () -> Unit) {
    UploadScope.begin(ctx.pose, ctx.buffer).use(block)
}


inline fun upload(size: Int, stack: PoseStack = PoseStack(), block: context(UploadScope) () -> Unit) {
    val builder = ByteBufferBuilder(size)
    val buffer = MultiBufferSource.immediate(builder)
    val scope = UploadScope.begin(stack, buffer)

    try {
        block(scope)
    } finally {
        buffer.endBatch()
        builder.close()

        scope.close()
    }
}

context(UploadScope)
inline fun with(type: RenderType, block: context(VertexBuilder) () -> Unit) {
    val builder = with(type)

    try {
        block(builder)
    } finally {
        builder.end()
    }
}

context(UploadScope)
inline fun quad(crossinline block: context(VertexBuilder) () -> Unit) =
    with(CustomRenderType.POSITION_COLOR_QUAD, block)

context(UploadScope)
inline fun textured(texture: Texture, crossinline block: context(VertexBuilder) () -> Unit) =
    with(CustomRenderType.getPositionColorTextureQuad(texture.resource), block)

context(UploadScope)
inline fun batch(block: context(UploadScope) () -> Unit) {
    try {
        block(this@UploadScope)
    } finally {
        endBatch()
    }
}

private object UploadScopeImpl : UploadScope {
    private var buffer: MultiBufferSource.BufferSource? = null
    private var stack: PoseStack? = null

    override val pose: PoseStack
        get() = stack!!

    var renderModes = arrayOfNulls<RenderMode>(RenderMode.TOTAL)

    fun begin(buffer: MultiBufferSource.BufferSource, stack: PoseStack) {
        require(this.buffer == null) {
            "DrawContext\$close must be called before this context can be used again!"
        }

        this.buffer = buffer
        this.stack = stack
    }

    override fun with(type: RenderType): VertexBuilder {
        require(this.buffer != null) {
            "DrawContext\$begin must be called before this context can be used!"
        }

        VertexBuilderImpl.begin(buffer!!.getBuffer(type), stack!!)
        return VertexBuilderImpl
    }

    override fun RenderMode.unaryPlus() {
        require(renderModes[id] == null) {
            "$this is already enabled!"
        }

        renderModes[id] = this.also {
            endBatch()
            enable()
        }
    }

    override fun RenderMode.unaryMinus() {
        require(renderModes[id] != null) {
            "$this is not enabled!"
        }

        endBatch()
        renderModes[id]!!.disable()
        renderModes[id] = null
    }


    override fun endBatch() {
        buffer?.endBatch()
    }

    override fun close() {
        if (renderModes.any { it != null })
            endBatch()

        for (i in renderModes.indices) {
            renderModes[i]?.disable()
            renderModes[i] = null
        }

        buffer = null
        stack = null
    }

    object VertexBuilderImpl : VertexBuilder {
        override var color: Color = Defaults.color
        override var uv: FloatPair = Defaults.uv
        override var normal: Vec3f = Defaults.normal

        override val shader: Shader
            get() = Shaders

        var consumer: VertexConsumer? = null
        var stack: PoseStack? = null

        fun begin(builder: VertexConsumer, stack: PoseStack) {
            this.consumer = builder
            this.stack = stack

            Shaders.begin()
        }

        override fun begin(pos: Vec3f): Vertex {
            VertexImpl.begin(pos, color, uv, normal)

            return VertexImpl
        }

        fun upload(vertex: Vertex) {
            consumer?.addVertex(stack!!.last(), vertex.pos)
            consumer?.setColor(vertex.color.red, vertex.color.green, vertex.color.blue, vertex.color.alpha)
            consumer?.setUv(vertex.uv.first, vertex.uv.second)

            if (vertex.normal !== Defaults.normal)
                consumer?.setNormal(vertex.normal.x, vertex.normal.y, vertex.normal.z)
        }

        override fun end() {
            color = Defaults.color
            uv = Defaults.uv
            normal = Defaults.normal

            Shaders.end()
        }
    }

    object VertexImpl : Vertex {
        override var pos: Vec3f = Defaults.pos

        override var color: Color = Defaults.color
        override var uv: FloatPair = Defaults.uv
        override var normal: Vec3f = Defaults.normal

        fun begin(pos: Vec3f, color: Color, uv: FloatPair, normal: Vec3f) {
            require(this.pos === Defaults.pos) {
                "Vertex\$end must be called before this Vertex can be used again!"
            }

            this.pos = pos
            this.color = color
            this.uv = uv
            this.normal = normal
        }

        override fun end() {
            VertexBuilderImpl.upload(this)

            pos = Defaults.pos
            color = Defaults.color
            uv = Defaults.uv
            normal = Defaults.normal
        }
    }

    object Shaders : Shader {
        var startInstance: ShaderInstance? = null
        var startColor: FloatArray = RenderSystem.getShaderColor()

        override var color: Color = Defaults.color

        fun begin() {
            startInstance = RenderSystem.getShader()

            color = startColor.let {
                Color(it[0], it[1], it[2], it[3])
            }
        }

        override fun invoke(supplier: Supplier<ShaderInstance?>) {
            RenderSystem.setShader(supplier)
        }


        fun end() {
            RenderSystem.setShader { startInstance }
            startInstance = null

            RenderSystem.setShaderColor(startColor[0], startColor[1], startColor[2], startColor[3])
            color = Defaults.color
        }
    }

    object Defaults {
        val pos = Vec3f(0f, 0f, 0f)
        val color: Color = CommonColors.WHITE.esl
        val uv: FloatPair = FloatPair(1f, 1f)
        val normal: Vec3f = Vec3f(1f, 1f, 1f)
    }
}
