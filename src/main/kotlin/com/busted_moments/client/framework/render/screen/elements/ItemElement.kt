package com.busted_moments.client.framework.render.screen.elements

import com.busted_moments.client.framework.render.Element
import com.busted_moments.client.framework.render.Renderer
import com.busted_moments.client.framework.render.Renderer.Companion.contains
import com.busted_moments.client.framework.render.Sizable
import com.busted_moments.client.framework.render.screen.Screen
import com.wynntils.utils.render.FontRenderer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import kotlin.experimental.ExperimentalTypeInference

private val AIR = ItemStack(Items.AIR)

abstract class ItemElement : Element<Screen.Context>(), Sizable {
    var stack: ItemStack = AIR
    var tooltip = true

    override val width: Float
        get() = 16f

    override val height: Float
        get() = 16f


    override fun draw(ctx: Screen.Context): Boolean {
        if (stack.item == Items.AIR)
            return false

        ctx.pose.pushPose()
        ctx.pose.translate(x, y, 0f)

        ctx.graphics.renderItem(stack, 0, 0)

        ctx.pose.popPose()

        if (tooltip && contains(ctx))
            ctx.graphics.renderTooltip(FontRenderer.getInstance().font, stack, ctx.mouseX.toInt(), ctx.mouseY.toInt())

        return true
    }
}

@OverloadResolutionByLambdaReturnType
@OptIn(ExperimentalTypeInference::class)
inline fun Renderer<Screen.Context>.item(
    crossinline block: ItemElement.(Screen.Context) -> Boolean
) {
    if (first)
        this += object : ItemElement() {
            override fun compute(ctx: Screen.Context): Boolean = block(ctx)
        }
}

@JvmName("itemUnit")
inline fun Renderer<Screen.Context>.item(
    crossinline block: ItemElement.(Screen.Context) -> Unit
) {
    if (first)
        this += object : ItemElement() {
            override fun compute(ctx: Screen.Context): Boolean {
                block(ctx)

                return true
            }
        }
}