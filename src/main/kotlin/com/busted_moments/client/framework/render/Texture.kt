package com.busted_moments.client.framework.render

import net.minecraft.resources.ResourceLocation

typealias Textures = com.wynntils.utils.render.Texture

interface Texture : Sizable {
    val resource: ResourceLocation

    companion object {
        operator fun invoke(
            name: String,
            width: Int,
            height: Int,
            key: String = "fuy",
            folder: String = "textures"
        ) = Texture(
            ResourceLocation(
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
    }
}

private data class Impl(
    override val resource: ResourceLocation,
    override val width: Float,
    override val height: Float
) : Texture