package com.busted_moments.client.screen.territories.search;

import com.busted_moments.client.features.war.WarCommon;
import com.busted_moments.client.models.territory.eco.GuildEco;
import com.busted_moments.client.models.territory.eco.TerritoryEco;
import com.busted_moments.client.screen.territories.TerritoryScreen;
import com.busted_moments.client.screen.territories.filter.Filter;
import com.busted_moments.client.screen.territories.filter.FilterMenu;
import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.UnexpectedException;
import com.busted_moments.core.render.screen.widgets.SearchBoxWidget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.StringReader;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class TerritorySearch<This extends TerritorySearch<This>> extends SearchBoxWidget<This> {
   protected List<TerritoryEco> territories = null;
   protected String input = "";
   protected String search = "";
   protected List<Criteria.Compiled> criteria = new ArrayList<>();

   private static Field HIGHLIGHTED_FIELD;

   private State state = State.STRIPPED;

   private String strippedText = "";
   private int strippedCursor = 0;
   private int strippedHighlightedCursor = 0;

   private int maxWidth;

   protected TerritorySearch(int x, int y, int width, int height, TextboxScreen textboxScreen) {
      super(x, y, width, height, null, textboxScreen);

      this.maxWidth = super.getMaxTextWidth();
   }

   @Override
   protected void onUpdate(String text) {
      criteria.clear();

      StringBuilder partBuilder = new StringBuilder();
      StringBuilder inputBuilder = new StringBuilder();
      StringBuilder searchBuilder = new StringBuilder();
      StringReader reader = new StringReader(ChatUtil.strip(text));

      boolean inString = false;

      Runnable processor = () -> {
         if (partBuilder.isEmpty())
            return;

         String part = partBuilder.toString();
         partBuilder.setLength(0);

         try {
            Optional<Criteria.Compiled> optional = Criteria.valueOf(part);
            if (optional.isPresent()) {
               Criteria.Compiled compiled = optional.get();

               criteria.add(compiled);
               inputBuilder.append(compiled.styled());

               return;
            }
         } catch (Exception e) {
            inputBuilder.append(ChatFormatting.RED).append(part).append(ChatFormatting.RESET);

            return;
         }

         inputBuilder.append(part);

         if (!searchBuilder.isEmpty())
            searchBuilder.append(' ');

         searchBuilder.append(part);
      };

      while (reader.canRead()) {
         char c = reader.read();

         if (c == '"') {
            inString = !inString;
            inputBuilder.append(c);
         } else if (c == ' ' && !inString) {
            processor.run();

            inputBuilder.append(c);
         } else
            partBuilder.append(c);
      }

      processor.run();
      input = inputBuilder.toString();
      search = searchBuilder.toString();
   }

   private void process() {
      GuildEco guild = guild();

      if (guild == null) {
         territories = List.of();

         return;
      }

      String search;
      String[] split;

      if (!this.search.isBlank()) {
         search = cleanupSearch(this.search);
         split = search.split(" ");
      } else {
         search = null;
         split = null;
      }

      this.territories = guild.stream()
              .filter(territory -> {
                 if (filters().strict() && filters().disjointed(Filter.getFilters(territory)))
                    return false;

                 if (!matches(split, territory))
                    return false;

                 for (var predicate : criteria)
                    if (!predicate.test(territory))
                       return false;

                 return true;
              })
              .toList();
   }

   private boolean matches(String[] split, TerritoryEco territory) {
      if (search == null)
         return true;

      if (WarCommon.getAcronym(territory).toLowerCase().startsWith(search))
         return true;

      String[] parts = cleanupSearch(territory).split(" ");

      if (split.length > parts.length)
         return false;

      int offset = -1;

      for (int i = 0; i < parts.length; i++) {
         if (offset == -1 && parts[i].contains(split[0])) offset = i;
         if (offset == -1)
            continue;

         int searchIndex = i - offset;
         if (searchIndex >= split.length)
            break;

         if (!parts[i].contains(split[i - offset])) offset = -1;
      }

      return offset != -1;
   }

   @Override
   public void render(@NotNull GuiGraphics graphics, MultiBufferSource.BufferSource bufferSource, int mouseX, int mouseY, float partialTick) {
      with(State.FORMATTED, () -> super.render(graphics, bufferSource, mouseX, mouseY, partialTick));
   }

   @Override
   protected void renderText(PoseStack poseStack, String renderedText, int renderedTextStart, String firstPortion, String highlightedPortion, String lastPortion, Font font, int firstWidth, int highlightedWidth, int lastWidth, boolean defaultText) {
      StyledText firstPortionText = StyledText.fromString(defaultText ? DEFAULT_TEXT.getString() : firstPortion);
      String firstPortionFormats = inheritFormats(firstPortionText);

      FontRenderer.getInstance()
              .renderAlignedTextInBox(
                      poseStack,
                      defaultText ? StyledText.fromComponent(DEFAULT_TEXT) : firstPortionText,
                      this.getX() + textPadding,
                      this.getX() + this.width - textPadding - lastWidth - highlightedWidth,
                      this.getY() + VERTICAL_OFFSET,
                      0,
                      defaultText ? CommonColors.LIGHT_GRAY : CommonColors.WHITE,
                      HorizontalAlignment.LEFT,
                      TextShadow.NORMAL);

      if (defaultText) return;

      StyledText highlightedPortionText = StyledText.fromString(highlightedPortion);
      String highlightedPortionFormats = inheritFormats(highlightedPortionText);

      FontRenderer.getInstance()
              .renderAlignedHighlightedTextInBox(
                      poseStack,
                      highlightedPortionText,
                      this.getX() + textPadding + firstWidth,
                      this.getX() + this.width - textPadding - lastWidth,
                      this.getY() + VERTICAL_OFFSET,
                      this.getY() + VERTICAL_OFFSET,
                      0,
                      CommonColors.BLUE,
                      CommonColors.WHITE,
                      HorizontalAlignment.LEFT,
                      VerticalAlignment.TOP);

      FontRenderer.getInstance()
              .renderAlignedTextInBox(
                      poseStack,
                      StyledText.fromString(firstPortionFormats + highlightedPortionFormats + lastPortion),
                      this.getX() + textPadding + firstWidth + highlightedWidth,
                      this.getX() + this.width - textPadding,
                      this.getY() + VERTICAL_OFFSET,
                      0,
                      defaultText ? CommonColors.LIGHT_GRAY : CommonColors.WHITE,
                      HorizontalAlignment.LEFT,
                      TextShadow.NORMAL);

      drawCursor(
              poseStack,
              this.getX()
                      + font.width(renderedText.substring(0, Math.min(cursorPosition, renderedText.length())))
                      + textPadding
                      - 2,
              this.getY() + VERTICAL_OFFSET,
              VerticalAlignment.TOP,
              false);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return with(State.STRIPPED, () -> super.mouseClicked(mouseX, mouseY, button));
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      return with(State.STRIPPED, () -> super.mouseDragged(mouseX, mouseY, button, dragX, dragY));
   }

   @SuppressWarnings("SameParameterValue")
   private <T> T with(State state, Supplier<T> supplier) {
      var ref = new Object() {
         T result;
      };

      with(state, (Runnable) () -> ref.result = supplier.get());

      return ref.result;
   }

   @SuppressWarnings("SameParameterValue")
   private void with(State state, Runnable runnable) {
      State old = this.state;

      setState(state);

      runnable.run();

      setState(old);
   }

   public This setMaxWidth(int maxWidth) {
      this.maxWidth = maxWidth;

      return getThis();
   }

   @Override
   public int getMaxTextWidth() {
      return maxWidth;
   }

   private void strip() {
      this.textBoxInput = strippedText;
      this.cursorPosition = strippedCursor;

      setHighlightedPosition(strippedHighlightedCursor);
   }

   private void format() {
      strippedText = textBoxInput;
      strippedCursor = cursorPosition;
      strippedHighlightedCursor = getHighlightedPosition();

      this.textBoxInput = input;

      this.cursorPosition = adjustCursor(input, strippedCursor);

      if (strippedHighlightedCursor != strippedCursor)
         setHighlightedPosition(adjustCursor(input, strippedHighlightedCursor));
      else
         setHighlightedPosition(cursorPosition);
   }

   protected void setState(State state) {
      if (this.state == state)
         return;

      this.state = state;

      if (state == State.STRIPPED)
         strip();
      else
         format();
   }

   private Field getHighlightedField() {
      if (HIGHLIGHTED_FIELD == null) {
         try {
            HIGHLIGHTED_FIELD = TextInputBoxWidget.class.getDeclaredField("highlightPosition");
            HIGHLIGHTED_FIELD.setAccessible(true);
         } catch (NoSuchFieldException e) {
            throw UnexpectedException.propagate(e);
         }
      }

      return HIGHLIGHTED_FIELD;
   }

   protected int getHighlightedPosition() {
      try {
         return (int) getHighlightedField().get(this);
      } catch (IllegalAccessException e) {
         throw UnexpectedException.propagate(e);
      }
   }

   protected void setHighlightedPosition(int cursor) {
      try {
         getHighlightedField().set(this, cursor);
      } catch (IllegalAccessException e) {
         throw UnexpectedException.propagate(e);
      }
   }

   private static String cleanupSearch(String string) {
      return string.replaceAll("[^\\w\\s]", "").toLowerCase();
   }

   private static String cleanupSearch(TerritoryEco territory) {
      return cleanupSearch(territory.getName());
   }

   private static int adjustCursor(String input, int originalCursor) {
      int cursor = 0;
      int offsets = 0;

      StringReader stringReader = new StringReader(input);

      while (stringReader.canRead() && (cursor - offsets) < originalCursor) {
         cursor++;

         if (stringReader.read() != ChatUtil.COLOR_CHAR || !stringReader.canRead())
            continue;

         if (ChatFormatting.getByCode(stringReader.read()) != null) {
            offsets += 2;
            cursor++;
         }
      }

      return cursor;
   }

   private static String inheritFormats(StyledText text) {
      StyledTextPart lastPart = text.getLastPart();
      if (lastPart == null)
         return "";

      return lastPart.getPartStyle().asString(null, PartStyle.StyleType.DEFAULT);
   }

   public List<TerritoryEco> territories() {
      process();

      return territories;
   }

   protected abstract @Nullable GuildEco guild();

   protected abstract FilterMenu filters();

   @Override
   public abstract TerritoryScreen<?> getElement();

   public enum State {
      STRIPPED,
      FORMATTED
   }
}
