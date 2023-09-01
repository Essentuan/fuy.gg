package com.busted_moments.client.features.war.territorymenu;

import com.busted_moments.client.models.territory.eco.TerritoryEco;
import com.busted_moments.client.models.territory.eco.TerritoryScanner;
import com.busted_moments.client.util.SoundUtil;
import com.busted_moments.core.render.FontRenderer;
import com.busted_moments.core.render.Renderer;
import com.busted_moments.core.render.screen.ClickEvent;
import com.busted_moments.core.render.screen.HoverEvent;
import com.busted_moments.core.render.screen.Screen;
import com.busted_moments.core.render.screen.Widget;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.wynn.ContainerUtils;
import me.shedaniel.clothconfig2.impl.EasingMethod;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.busted_moments.client.util.Textures.TerritoryMenu.*;

public class TerritoryMenuScreen extends Screen.Element {
   public static final Pattern GUILD_MANAGE_MENU = Pattern.compile("^(?<guild>.+): Manage$");
   public static final Pattern TERRITORY_MENU_PATTERN = Pattern.compile("^(?<guild>.+): Territories$");

   private static final float ITEM_SCALE = 1.35F;

   public static final int BACK = 18;
   public static final int LOADOUTS = 36;

   private final int CONTAINER_ID;

   private VerticalScrollbar scrollbar;
   private SearchBox search;
   private Item back_button;
   private Item loadouts_button;
   private FilterMenu filter_menu;

   private List<TerritoryEco> territories = null;

   private final TerritoryScanner scanner;
   private boolean IS_CLICKING = false;

   private final Multimap<Filter, Entry> counts = MultimapBuilder.hashKeys().arrayListValues().build();

   public TerritoryMenuScreen(int id) {
      super(Component.literal("Manage Territories"));

      CONTAINER_ID = id;
      scanner = new TerritoryScanner(CONTAINER_ID);

      scanner.onUpdate(e -> {
         territories = e.stream().toList();

         rebuild();
      });
   }

   @Override
   protected void init() {
      super.init();

      new SearchBox(0, 0, 150, 20, string -> rebuild(), this)
              .setScale(0.8F).perform(box -> {
                 this.search = box;

                 box.setX((this.width / 2) - box.getWidth() / 2);
                 box.setY((this.height / 2) - (FOREGROUND.height()) / 2 - box.getHeight() / 2 - 2);
              })
              .then(VerticalScrollbar::new)
              .perform(slider -> {
                 scrollbar = slider;

                 slider.setX((this.width / 2F) + FOREGROUND.width() / 2F);
                 slider.setY((this.height / 2F) - FOREGROUND.height() / 2F);
              })
              .offset(6.5F, -9F)
              .setSize(9, 156)
              .setTexture(SCROLLBAR)
              .setScrollIntensity(60)
              .setEasing(EasingMethod.EasingMethodImpl.QUINTIC)
              .onScroll(scroll -> {
                 TerritoryPosition position = getTerritoryPosition(getTerritories());
                 var entries = getWidgets().stream().filter(widget -> widget instanceof Entry).toList();

                 for (int i = 0; i < entries.size(); i++) ((Entry) entries.get(i)).update(i, position);
              })
              .then(Item::new)
              .setScale(1.05F)
              .setItem(() -> scanner.getContents().isEmpty() ? ItemStack.EMPTY : scanner.getContents().get(BACK))
              .perform(item -> {
                 back_button = item;

                 item.setX(2 + (this.width / 2F) - BACKGROUND.width() / 2F);
                 item.setY((this.height / 2F) - item.getHeight() / 2F);
              }).onClick(click(BACK, true))
              .tooltip()
              .onHover((x, y, entry) -> new Rect().setPosition(entry.getX(), entry.getY())
                              .setSize(entry.getWidth(), entry.getHeight())
                              .setFill(255, 255, 255, 127).build(),
                      HoverEvent.PRE
              ).then(Item::new)
              .setScale(1.05F)
              .setItem(() -> scanner.getContents().isEmpty() ? ItemStack.EMPTY : scanner.getContents().get(LOADOUTS))
              .perform(item -> {
                 loadouts_button = item;

                 item.setX(2.75F + (this.width / 2F) - BACKGROUND.width() / 2F);
                 item.setY(((this.height / 2F) + BACKGROUND.height() / 2F) - item.getHeight() - 3);
              }).onClick(click(LOADOUTS, false))
              .tooltip()
              .onHover((x, y, entry) -> new Rect().setPosition(entry.getX() - 0.5F, entry.getY())
                              .setSize(entry.getWidth(), entry.getHeight())
                              .setFill(255, 255, 255, 127).build(),
                      HoverEvent.PRE
              ).then(() -> new FilterMenu() {
                 @Override
                 public Screen.Element getElement() {
                    return TerritoryMenuScreen.this;
                 }
              }).onUpdate(m -> rebuild())
              .setCounts(counts)
              .perform(menu -> {
                 this.filter_menu = menu;

                 menu.setX((this.width / 2F) + BACKGROUND.width() / 2F);
                 menu.setY((this.height / 2F) - (menu.getHeight() + 6) / 2F);
              }).offset(-5, 0F).build();
   }

   private List<TerritoryEco> getTerritories() {
      if (territories == null) return null;

      String search;
      String[] split;

      if (this.search != null && !this.search.getTextBoxInput().isBlank()) {
         search = cleanupSearch(this.search.getTextBoxInput());
         split = search.split(" ");
      } else {
         search = null;
         split = null;
      }

      return territories.stream()
              .filter(territory -> {
                 if (filter_menu.strict() && filter_menu.disjointed(Filter.getFilters(territory))) return false;

                 if (search == null) return true;

                 if (getAcronym(territory).toLowerCase().startsWith(search)) return true;

                 String[] parts = cleanupSearch(territory).split(" ");

                 if (split.length > parts.length) return false;

                 int offset = -1;

                 for (int i = 0; i < parts.length; i++) {
                    if (offset == -1 && parts[i].contains(split[0])) offset = i;
                    if (offset == -1) continue;

                    int searchIndex = i - offset;
                    if (searchIndex >= split.length) break;

                    if (!parts[i].contains(split[i - offset])) offset = -1;
                 }

                 return offset != -1;
              })
              .toList();
   }

   private void rebuild() {
      if (search == null) return;

      clear();

      search.build();
      scrollbar.build();
      back_button.build();
      loadouts_button.build();
      filter_menu.build();

      var territories = getTerritories();

      if (territories == null || territories.isEmpty()) return;

      new Mask()
              .perform(mask -> {
                 mask.setX((this.width / 2F) - FOREGROUND.width() / 2F);
                 mask.setY((this.height / 2F) - FOREGROUND.height() / 2F);
              })
              .offset(-4.5F, -7F)
              .build();

      TerritoryPosition position = getTerritoryPosition(territories);

      counts.clear();

      for (int i = 0; i < territories.size(); i++)
         new Entry(territories.get(i))
                 .update(i, position)
                 .tooltip()
                 .setScale(ITEM_SCALE)
                 .build();

      new ClearMask().build();
   }

   private TerritoryPosition getTerritoryPosition(List<TerritoryEco> territories) {
      float x = 6.5F + (width / 2F) - FOREGROUND.width() / 2F;
      float y = 9F + (height / 2F) - FOREGROUND.height() / 2F;

      int cols = (FOREGROUND.width() - SCROLLBAR.width() - 2) / (int) Entry.WIDTH;
      int rows = getRows(territories, cols);
      int maxRows = (FOREGROUND.height() - 2) / (int) Entry.HEIGHT;

      float scroll = (float) (scrollbar.getScroll() * Math.max((rows - maxRows) * Entry.HEIGHT, Entry.HEIGHT));

      return new TerritoryPosition(x, y, cols, scroll);
   }

   private <T> ClickEvent.Handler<T> click(int slot, boolean sound) {
      return (x, y, button, ignored) -> {
         if (IS_CLICKING) return false;
         TerritoryHelperMenuFeature.OPEN_TERRITORY_MENU = false;
         IS_CLICKING = true;

         if (sound) SoundUtil.play(SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 1, 1);

         ContainerUtils.clickOnSlot(slot, CONTAINER_ID, button, scanner.getContents());

         return true;
      };
   }

   private int getRows(List<TerritoryEco> territories, int cols) {
      if (territories.size() < cols) return 1;

      return (int) Math.ceil(territories.size() / (double) cols);
   }

   @Override
   public void removed() {
      scanner.close();
      TerritoryHelperMenuFeature.GUILD_MENU_ID = -1;

      if (!IS_CLICKING) {
         ContainerUtils.closeContainer(CONTAINER_ID);
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      return super.mouseScrolled(mouseX, mouseY, delta) || scrollbar.scroll(delta);
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      filter_menu.onKeyDown(keyCode, scanCode, modifiers);

      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   private boolean IS_INSIDE = false;

   @Override
   protected void onRender(@NotNull PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, int mouseX, int mouseY, float partialTick) {
      new Texture(BACKGROUND)
              .center()
              .then(Texture::new)
              .setTexture(FOREGROUND)
              .center()
              .offset(-4.5F, -7F)
              .build();

      if (territories != null && territories.isEmpty()) {
         new Text("No Territories :(", 0, 0)
                 .setColor(CommonColors.WHITE)
                 .center()
                 .offset(-4.5F, -7F)
                 .build();
      } else if (!search.getTextBoxInput().isBlank()) {
         int count = 0;
         for (Screen.Widget<?> widget : getWidgets()) if (widget instanceof Entry) count++;

         if (count == 0) {
            new Text("Not Found", 0, 0)
                    .setColor(CommonColors.WHITE)
                    .center()
                    .offset(-4.5F, -7F)
                    .build();
         }
      }

      float x = 6.5F + (this.width / 2F) - FOREGROUND.width() / 2F;
      float y = 8F + (this.height / 2F) - FOREGROUND.height() / 2F;

      IS_INSIDE = mouseX >= x && mouseY >= y && mouseX < x + FOREGROUND.width() - 2 && mouseY < y + FOREGROUND.height() - 2;
   }

   private static String cleanupSearch(String string) {
      return string.replaceAll("[^\\w\\s]", "").toLowerCase();
   }

   private static String cleanupSearch(TerritoryEco territory) {
      return cleanupSearch(territory.getName());
   }

   private static String getAcronym(TerritoryEco eco) {
      return Stream.of(eco.getName().split("[ \\-]"))
              .map(string -> string.isBlank() ? "" : String.valueOf(string.charAt(0)))
              .collect(Collectors.joining());
   }

   public class Entry extends Item {
      private static final float WIDTH = (16 * ITEM_SCALE) + 4.9F;
      private static final float HEIGHT = (16 * ITEM_SCALE) + 4.9F;

      private final Set<Filter> filters;
      private final StyledText acronym;

      public Entry(TerritoryEco territory) {
         super(territory.getItem());

         this.filters = Filter.getFilters(territory);
         this.acronym = StyledText.fromString(getAcronym(territory));

         this.filters.forEach(filter -> counts.put(filter, this));
      }

      @SuppressWarnings("IntegerDivisionInFloatingPointContext")
      private Entry update(int i, TerritoryPosition position) {
         float item_size = 16 * ITEM_SCALE;

         setPosition(
                 position.x + (i % position.cols) * WIDTH + (WIDTH / 2F - item_size / 2F),
                 position.y + (i / position.cols) * HEIGHT - position.scroll - 1.5F + (HEIGHT / 2F - item_size / 2F)
         );

         if (getItemSupplier().get().getItem() == Items.MAP) offsetY(-1);

         return this;
      }

      @Override
      protected boolean onMouseDown(double mouseX, double mouseY, int button) {
         if (IS_INSIDE && isMouseOver(mouseX, mouseY) && !IS_CLICKING && button == GLFW.GLFW_MOUSE_BUTTON_1) {
            IS_CLICKING = true;
            scanner.select(getItemSupplier().get());
            SoundUtil.play(SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundSource.MASTER, 1, 1);

            return true;
         }

         return false;
      }

      private static final float item_size = 16 * ITEM_SCALE;
      private static final float x_offset = (Entry.WIDTH / 2F - item_size / 2F);
      private static final float y_offset = (Entry.HEIGHT / 2F - item_size / 2F);


      @Override
      public void render(@NotNull PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, int mouseX, int mouseY, float partialTick) {
         CustomColor color = null;

         if (IS_INSIDE && isMouseOver(mouseX, mouseY)) color = new CustomColor(255, 255, 255, 127);
         else {
            var iter = filters.iterator();

            while (iter.hasNext() && color == null) {
               var next = iter.next();

               if (filter_menu.isEnabled(next)) color = next.getColor();
            }
         }

         if (color != null)
            Renderer.fill(
                    poseStack,
                    getX() - x_offset,
                    getY() - y_offset + 1.5F - (getItemSupplier().get().getItem() == Items.MAP ? 1 : 0),
                    Entry.WIDTH,
                    Entry.HEIGHT,
                    color
            );

         super.render(poseStack, bufferSource, mouseX, mouseY, partialTick);

         poseStack.pushPose();
         poseStack.translate(0, 0, 300);

         Renderer.text(
                 poseStack, bufferSource,
                 acronym,
                 getX(), getY(), 0F,
                 CommonColors.ORANGE,
                 TextShadow.OUTLINE,
                 Math.min(1F, 16 * ITEM_SCALE / FontRenderer.getWidth(acronym, 0F))
         );

         poseStack.popPose();
         bufferSource.endBatch();
      }
   }

   private class Mask extends Widget<Mask> {
      @Override
      public Screen.Element getElement() {
         return TerritoryMenuScreen.this;
      }

      @Override
      protected void onRender(@NotNull PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, int mouseX, int mouseY, float partialTick) {
         Renderer.mask(poseStack, getX(), getY(), MASK);
      }
   }

   private class ClearMask extends Widget<ClearMask> {

      @Override
      public Screen.Element getElement() {
         return TerritoryMenuScreen.this;
      }

      @Override
      protected void onRender(@NotNull PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, int mouseX, int mouseY, float partialTick) {
         Renderer.clear_mask();
      }
   }

   private record TerritoryPosition(float x, float y, int cols, float scroll) {
   }
}
