package com.busted_moments.core.render.screen.widgets;

import com.busted_moments.client.mixin.ScreenAccessor;
import com.busted_moments.core.render.Renderer;
import com.busted_moments.core.render.screen.HoverEvent;
import com.busted_moments.core.render.screen.Screen;
import com.busted_moments.core.render.screen.ScreenElement;
import com.busted_moments.core.render.screen.Widget;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class ItemStackWidget<This extends ItemStackWidget<This>> extends Widget<This> {
   private Supplier<ItemStack> item;

   public ItemStackWidget() {
      super();

      setSize(16, 16);
   }

   public Supplier<ItemStack> getItemSupplier() {
      return item;
   }

   public This tooltip() {
      return onHover((x, y, entry) -> new TooltipImpl().setItem(getItemSupplier()).setPosition(x, y).build(), HoverEvent.POST);
   }

   private class TooltipImpl extends Tooltip<TooltipImpl> {

      @Override
      public Screen.Element getElement() {
         return ItemStackWidget.this.getElement();
      }
   }

   public This setItem(ItemStack stack) {
      return setItem(() -> stack);
   }

   public This setItem(Supplier<ItemStack> item) {
      this.item = item;

      return getThis();
   }

   @Override
   protected void onRender(@NotNull PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, int mouseX, int mouseY, float partialTick) {
      Renderer.gui_item(
              poseStack,
              bufferSource,
              item.get(),
              getX(),
              getY()
      );
   }

   public static abstract class Tooltip<This extends Tooltip<This>> extends ScreenElement<This> {
      public Tooltip() {

      }

      public Tooltip(ItemStackWidget<?> widget) {
         setItem(widget.getItemSupplier());
      }

      private Supplier<ItemStack> item;

      public Supplier<ItemStack> getItemSupplier() {
         return item;
      }

      public This setItem(ItemStack stack) {
         return setItem(() -> stack);
      }

      public This setItem(Supplier<ItemStack> item) {
         this.item = item;

         return getThis();
      }

      @Override
      public void render(@NotNull PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, int mouseX, int mouseY, float partialTick) {
         ((ScreenAccessor) getElement()).callRenderTooltip(poseStack, item.get(), (int) getX(), (int) getY());
      }
   }
}
