package com.busted_moments.client.framework.render

import com.busted_moments.client.framework.wynntils.wynntils
import com.busted_moments.client.framework.wynntils.esl
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.VertexFormat
import com.wynntils.utils.colors.CustomColor
import com.wynntils.utils.render.buffered.CustomRenderType
import net.essentuan.esl.color.Color
import net.minecraft.client.renderer.CoreShaders
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.resources.ResourceLocation

typealias Textures = com.wynntils.utils.render.Texture

interface Texture : Sizable {
    val resource: ResourceLocation

    fun render(
        poseStack: PoseStack,
        bufferSource: MultiBufferSource.BufferSource,
        x: Float,
        y: Float,
        z: Float = 0f,
        width: Float = this.width,
        height: Float = this.height,
        ux: Int = 0,
        uy: Int = 0,
        color: Color = CustomColor.NONE.esl
    ) {
        render(
            poseStack,
            bufferSource.getBuffer(CustomRenderType.getPositionColorTextureQuad(resource)),
            x,
            y,
            z,
            width,
            height,
            ux,
            uy,
            color.wynntils
        )
    }

    fun render(
        poseStack: PoseStack,
        x: Float,
        y: Float,
        z: Float = 0f,
        width: Float = this.width,
        height: Float = this.height,
        ux: Int = 0,
        uy: Int = 0,
        color: Color = CustomColor.NONE.esl
    ) {
        RenderSystem.setShader(CoreShaders.POSITION_TEX)
        RenderSystem.setShaderTexture(0, resource)
        val builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)

        render(
            poseStack,
            builder,
            x,
            y,
            z,
            width,
            height,
            ux,
            uy,
            color.wynntils
        )

        BufferUploader.drawWithShader(builder.build()!!)
    }

    companion object {
        val EMERALD = Texture("production/emerald.png", 8, 8)
        val ORE = Texture("production/ore.png", 8, 8)
        val WOOD = Texture("production/wood.png", 8, 8)
        val FISH = Texture("production/fish.png", 8, 8)
        val CROP = Texture("production/crop.png", 8, 8)

        operator fun invoke(
            name: String,
            width: Int,
            height: Int,
            key: String = "fuy",
            folder: String = "textures"
        ) = Texture(
            ResourceLocation.fromNamespaceAndPath(
                key,
                if (folder.isNotEmpty())
                    "$folder/$name"
                else
                    name
            ),
            width,
            height
        )

        operator fun invoke(
            location: ResourceLocation,
            width: Int,
            height: Int
        ): Texture = Impl(location, width.toFloat(), height.toFloat())

        private fun Texture.render(
            poseStack: PoseStack,
            builder: VertexConsumer,
            x: Float,
            y: Float,
            z: Float = 0f,
            width: Float = this.width,
            height: Float = this.height,
            ux: Int = 0,
            uy: Int = 0,
            color: CustomColor
        ) {
            val uxScale = 1f / this.width
            val uyScale = 1f / this.height
            val matrix = poseStack.last().pose()

            if (color !== CustomColor.NONE) {
                RenderSystem.enableBlend()
                RenderSystem.defaultBlendFunc()

                builder.addVertex(matrix, x, y + height, z).setUv(ux * uxScale, (uy + this.height) * uyScale).setColor(color.r, color.g, color.b, color.a)
                builder.addVertex(matrix, x + width, y + height, z).setUv((ux + this.width) * uxScale, (uy + this.height) * uyScale).setColor(color.r, color.g, color.b, color.a)
                builder.addVertex(matrix, x + width, y, z).setUv((ux + this.width) * uxScale, uy * uyScale).setColor(color.r, color.g, color.b, color.a)
                builder.addVertex(matrix, x, y, z).setUv(ux * uxScale, uy * uyScale).setColor(color.r, color.g, color.b, color.a)

                RenderSystem.disableBlend()
            } else {
                builder.addVertex(matrix, x, y + height, z).setUv(ux * uxScale, (uy + this.height) * uyScale).setColor(1f, 1f, 1f, 1f)
                builder.addVertex(matrix, x + width, y + height, z).setUv((ux + this.width) * uxScale, (uy + this.height) * uyScale).setColor(1f, 1f, 1f, 1f)
                builder.addVertex(matrix, x + width, y, z).setUv((ux + this.width) * uxScale, uy * uyScale).setColor(1f, 1f, 1f, 1f)
                builder.addVertex(matrix, x, y, z).setUv(ux * uxScale, uy * uyScale).setColor(1f, 1f, 1f, 1f)
            }
        }
    }
}

private data class Impl(
    override val resource: ResourceLocation,
    override val width: Float,
    override val height: Float
) : Texture

val Textures.texture: Texture
    get() = (this as Texture)