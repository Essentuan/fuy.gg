package com.busted_moments.client.screen.territories.filter;

import com.busted_moments.client.features.keybinds.Keybind;
import com.busted_moments.client.features.war.TerritoryHelperMenuFeature;
import com.busted_moments.client.util.SoundUtil;
import com.busted_moments.core.render.FontRenderer;
import com.busted_moments.core.render.screen.Widget;
import com.busted_moments.core.text.TextBuilder;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.text.StyledText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;

public abstract class FilterMenu extends Widget<FilterMenu> {
   final static StyledText LEGEND = getLegend(null, Set.of(Filter.values()), null);

   private static Set<Filter> selected = new HashSet<>(List.of(Filter.values()));

   private Multimap<Filter, ?> counts = null;

   private Consumer<FilterMenu> ON_UPDATE = m -> {
   };

   public FilterMenu() {
      if (TerritoryHelperMenuFeature.resetFiltersOnExit()) selected = new HashSet<>(List.of(Filter.values()));
      setSize(
              FontRenderer.getWidth(FilterMenu.LEGEND, 0),
              FontRenderer.getHeight(FilterMenu.LEGEND, 0)
      );

      selected.remove(Filter.STRICT_MODE);
   }

   public boolean isEnabled(Filter filter) {
      return selected.contains(filter);
   }

   public boolean disjointed(Set<Filter> filters) {
      return Collections.disjoint(selected, filters);
   }

   public boolean strict() {
      return selected.contains(Filter.STRICT_MODE);
   }

   public FilterMenu onUpdate(Consumer<FilterMenu> consumer) {
      this.ON_UPDATE = consumer;

      return this;
   }

   public FilterMenu setCounts(Multimap<Filter, ?> counts) {
      this.counts = counts;

      return this;
   }

   private Optional<Filter> getHovered(double mouseY) {
      int line = ((int) (mouseY - getY()) / FontRenderer.lineHeight()) / 2;

      if (isHovered() && line > 0)
         return Optional.of(Filter.values()[line - 1]);

      return Optional.empty();
   }

   private void toggle(Filter filter) {
      if (selected.contains(filter)) selected.remove(filter);
      else selected.add(filter);
   }

   @Override
   protected boolean onMouseDown(double mouseX, double mouseY, int button) {
      if (button != GLFW.GLFW_MOUSE_BUTTON_1) return false;

      var optional = getHovered(mouseY);
      if (optional.isEmpty()) return false;

      var filter = optional.get();

      if (filter.equals(Filter.PLACEHOLDER)) return false;

      if (Keybind.isKeyDown(InputConstants.KEY_LSHIFT) || Keybind.isKeyDown(InputConstants.KEY_RSHIFT)) {
         selected.removeIf(Filter::isClickable);
         selected.add(filter);
      } else toggle(filter);

      SoundUtil.play(SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 1, 1);

      ON_UPDATE.accept(this);

      return true;
   }


   public void onKeyDown(int keyCode, int scanCode, int modifiers) {
      if (keyCode != InputConstants.KEY_S) return;

      toggle(Filter.STRICT_MODE);
      ON_UPDATE.accept(this);
   }

   @Override
   protected void onRender(@NotNull GuiGraphics graphics, MultiBufferSource.BufferSource bufferSource, int mouseX, int mouseY, float partialTick) {
      new TextBox(getLegend(counts, selected, getHovered(mouseY).orElse(null)), getX(), getY()).setFill(0, 0, 0, 127)
              .setPadding(3, 3, 3, 3)
              .dynamic()
              .build();
   }

   private static StyledText getLegend(Multimap<Filter, ?> counts, Set<Filter> selected, Filter hovered) {
      return TextBuilder.of("Filters", ChatFormatting.WHITE, ChatFormatting.BOLD)
              .line().line()
              .append(List.of(Filter.values()), (filter, builder) ->
                      builder.append("", ChatFormatting.RESET).append(filter.toText(counts, selected, hovered)).line()
              ).build();
   }
}
