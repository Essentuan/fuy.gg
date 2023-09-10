package com.busted_moments.client.screen.territories;

import com.busted_moments.client.features.war.TerritoryHelperMenuFeature;
import com.busted_moments.client.models.territory.eco.TerritoryEco;
import com.busted_moments.client.models.territory.eco.TerritoryScanner;
import com.busted_moments.client.models.territory.eco.tributes.TributeModel;
import com.busted_moments.client.models.territory.eco.types.ResourceType;
import com.busted_moments.client.screen.territories.filter.Filter;
import com.busted_moments.client.screen.territories.filter.FilterMenu;
import com.busted_moments.client.util.ContainerHelper;
import com.busted_moments.client.util.SoundUtil;
import com.busted_moments.core.render.FontRenderer;
import com.busted_moments.core.render.Renderer;
import com.busted_moments.core.render.screen.ClickEvent;
import com.busted_moments.core.render.screen.HoverEvent;
import com.busted_moments.core.render.screen.Screen;
import com.busted_moments.core.render.screen.Widget;
import com.busted_moments.core.text.TextBuilder;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.TimeUnit;
import com.busted_moments.core.util.NumUtil;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.wynn.ContainerUtils;
import me.shedaniel.clothconfig2.impl.EasingMethod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.busted_moments.client.util.Textures.TerritoryMenu.*;
import static net.minecraft.ChatFormatting.*;

public abstract class TerritoryScreen<Scanner extends TerritoryScanner> extends Screen.Element {
   private static final float ITEM_SCALE = 1.35F;
   private static final int BACK_SLOT = 18;

   private final int CONTAINER_ID;

   private VerticalScrollbar scrollbar;
   private SearchBox search;
   private FilterMenu filter_menu;

   protected List<Screen.Widget<?>> baseWidgets = List.of();

   protected List<TerritoryEco> territories = null;

   protected final Scanner scanner;
   protected boolean BUSY = false;

   private final Multimap<Filter, AbstractEntry> counts = MultimapBuilder.hashKeys().arrayListValues().build();

   private List<FormattedCharSequence> GUILD_OUTPUT = List.of();
   private float GUILD_OUTPUT_WIDTH = 0;
   private float GUILD_OUTPUT_HEIGHT = 0;
   private float GUILD_OUTPUT_OFFSET_X = 0;
   private float GUILD_OUTPUT_OFFSET_Y = 0;

   private final boolean showProduction;
   private final boolean playBackSound;
   private final boolean showPercents;

   public TerritoryScreen(int id, boolean showProduction, boolean playBackSound, boolean showPercents) {
      super(Component.literal("Manage Territories"));

      this.showProduction = showProduction;
      this.playBackSound = playBackSound;
      this.showPercents = showPercents;

      CONTAINER_ID = id;
      scanner = scanner(CONTAINER_ID);

      scanner.onUpdate(e -> {
         territories = e.stream().toList();

         rebuild();
      });

      FabricLoader.getInstance().getModContainer("legendarytooltips").ifPresent(container -> {
         GUILD_OUTPUT_OFFSET_Y = 2F;
         GUILD_OUTPUT_OFFSET_X = 6.5F;
      });
   }

   protected abstract Scanner scanner(int container);

   protected abstract AbstractEntry entry(TerritoryEco territory);

   protected abstract Pattern title();

   protected abstract void build();

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
                 var entries = getWidgets().stream().filter(widget -> AbstractEntry.class.isAssignableFrom(widget.getClass())).toList();

                 for (int i = 0; i < entries.size(); i++) ((AbstractEntry) entries.get(i)).update(i, position);
              })
              .then(() -> item(BACK_SLOT, playBackSound, true))
              .setScale(1.05F)
              .perform(item -> {
                 item.setX(2 + (this.width / 2F) - BACKGROUND.width() / 2F);
                 item.setY((this.height / 2F) - item.getHeight() / 2F);
              })
              .then(() -> new FilterMenu() {
                 @Override
                 public Screen.Element getElement() {
                    return TerritoryScreen.this;
                 }
              }).onUpdate(m -> rebuild())
              .setCounts(counts)
              .perform(menu -> {
                 this.filter_menu = menu;

                 menu.setX((this.width / 2F) + BACKGROUND.width() / 2F);
                 menu.setY((this.height / 2F) - (menu.getHeight() + 6) / 2F);
              }).offset(-5, 0F).build();

      build();

      baseWidgets = List.copyOf(getWidgets());
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

      baseWidgets.forEach(Screen.Widget::build);

      var territories = getTerritories();

      if (territories == null || territories.isEmpty()) {
         GUILD_OUTPUT = List.of();
         return;
      }

      new Mask()
              .perform(mask -> {
                 mask.setX((this.width / 2F) - FOREGROUND.width() / 2F);
                 mask.setY((this.height / 2F) - FOREGROUND.height() / 2F);
              })
              .offset(-4.5F, -7F)
              .build();

      TerritoryPosition position = getTerritoryPosition(territories);

      counts.clear();

      Map<ResourceType, Long> production = new HashMap<>();
      Map<ResourceType, TerritoryEco.Storage> storage = new HashMap<>();
      Map<ResourceType, Long> costs = new HashMap<>();

      Map<ResourceType, Long> tributes = TributeModel.getNetTributes();

      for (TerritoryEco entry : this.territories) {
         for (ResourceType resource : ResourceType.values()) {
            production.compute(resource, (r, total) -> {
               var prod = entry.getProduction(resource);
               if (total == null) return prod;

               return total + prod;
            });

            storage.compute(resource, (r, total) -> entry.getStorage(resource).add(total));
            costs.compute(resource, (r, total) -> {
               var cost = entry.getCost(resource);
               if (total == null) return cost;

               return total + cost;
            });
         }
      }

      tributes.forEach((resource, x) -> {
         if (x < 0) costs.compute(resource, (r, old) -> {
            if (old == null) return Math.abs(x);

            return old + Math.abs(x);
         });
         else {
            production.compute(resource, (r, old) -> {
               if (old == null) return x;

               return old + x;
            });
         }
      });

      for (int i = 0; i < territories.size(); i++) {
         var entry = territories.get(i);

         entry(entry)
                 .update(i, position)
                 .tooltip()
                 .setScale(ITEM_SCALE)
                 .build();

      }

      var output = TextBuilder.of("Guild Output", WHITE, BOLD).line()
              .append("Total resource output", GRAY).line()
              .append("and overall costs", GRAY).line().line()
              .append(List.of(ResourceType.values()), (resource, builder) -> {
                 String symbol = resource.getPrettySymbol();

                 var color = resource.getColor();
                 TerritoryEco.Storage s = storage.get(resource);

                 builder
                         .append(symbol)
                         .append("+", color)
                         .append(NumUtil.format(production.getOrDefault(resource, 0L)), color).space()
                         .append(resource.getName(), color)
                         .append(" per hour").line();

                 long gained = tributes.getOrDefault(resource, 0L);

                 if (gained > 0) builder
                         .append(symbol)
                         .append("(", color)
                         .append(NumUtil.format(gained), color)
                         .append(" from Tributes)", color)
                         .line();

                 builder
                         .append(symbol)
                         .append(NumUtil.format(s.stored()), color)
                         .append("/", color)
                         .append(NumUtil.format(s.capacity()))
                         .append(" in storage");
              }).line().line()
              .append("Overall Cost (per hour):", GREEN).line()
              .append(List.of(ResourceType.values()), (resource, builder) -> {
                 builder.append("- ", GREEN)
                         .append(resource.getSymbol(), GRAY)
                         .appendIf(() -> !resource.getSymbol().isEmpty(), " ")
                         .append(NumUtil.format(costs.getOrDefault(resource, 0L)), GRAY).space()
                         .append(resource.getName(), GRAY).space();

                 var prod = production.getOrDefault(resource, 0L);
                 var cost = costs.getOrDefault(resource, 0L);

                 long difference = prod - cost;
                 long percent = (long) ((cost / Math.max(prod, 1D)) * 100);

                 ChatFormatting headroomColor;
                 ChatFormatting percentColor;

                 if (difference < 0) {
                    headroomColor = RED;
                    percentColor = RED;
                 } else {
                    headroomColor = BLUE;
                    percentColor = DARK_GRAY;
                 }


                 builder.append("(", headroomColor)
                         .appendIf(() -> difference >= 0, "+", headroomColor)
                         .append(NumUtil.truncate(difference), headroomColor)
                         .append(")", headroomColor).space();

                 if (showPercents) builder
                         .append("(", percentColor)
                         .append((int) percent, percentColor)
                         .append("%)", percentColor);
              }).build();

      GUILD_OUTPUT = FontRenderer.split(output, 0)
              .stream()
              .map(StyledText::getComponent)
              .map(Component::getVisualOrderText)
              .toList();
      GUILD_OUTPUT_WIDTH = FontRenderer.getWidth(output, 0) + 16;
      GUILD_OUTPUT_HEIGHT = FontRenderer.getHeight(output, 0) - 8;


      new ClearMask().build();
   }

   private TerritoryPosition getTerritoryPosition(List<TerritoryEco> territories) {
      float x = 6.5F + (width / 2F) - FOREGROUND.width() / 2F;
      float y = 9F + (height / 2F) - FOREGROUND.height() / 2F;

      int cols = (FOREGROUND.width() - SCROLLBAR.width() - 2) / (int) AbstractEntry.WIDTH;
      int rows = getRows(territories, cols);
      int maxRows = (FOREGROUND.height() - 2) / (int) AbstractEntry.HEIGHT;

      float scroll = (float) (scrollbar.getScroll() * Math.max((rows - maxRows) * AbstractEntry.HEIGHT, AbstractEntry.HEIGHT));

      return new TerritoryPosition(x, y, cols, scroll);
   }

   protected Item item(int slot, boolean sound, boolean cancel) {
      return item(slot, getClickHandler(slot, sound, cancel));
   }

   protected Item item(int slot, ClickEvent.Handler<Item> onclick) {
      return new Item()
              .setItem(() -> scanner.getContents().isEmpty() ? ItemStack.EMPTY : scanner.getContents().get(slot))
              .onClick(onclick)
              .tooltip()
              .onHover((x, y, entry) -> new Rect().setPosition(entry.getX() - 0.5F, entry.getY())
                              .setSize(entry.getWidth(), entry.getHeight())
                              .setFill(255, 255, 255, 127).build(),
                      HoverEvent.PRE
              );
   }
   
   private boolean WAITING = false;
   private Date LAST_CLICK = new Date();
   
   protected boolean click(int slot, int button) {
      if (BUSY || WAITING || Duration.since(LAST_CLICK).lessThan(150, TimeUnit.MILLISECONDS) || !ContainerHelper.Click(slot, button, title())) return false;
      WAITING = true;
      LAST_CLICK = new Date();
      
      return true;
   }
   
   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public void onMenuSetContents(ContainerSetContentEvent.Pre event) {
      if (event.getContainerId() != CONTAINER_ID) return;
      WAITING = false;
   }
   
   protected <T> ClickEvent.Handler<T> getClickHandler(int slot, boolean sound, boolean cancel) {
      return (x, y, button, ignored) -> {
         if (!click(slot, button)) return false;
         TerritoryHelperMenuFeature.OPEN_TERRITORY_MENU = false;
         BUSY = cancel;

         if (sound) SoundUtil.play(SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 1, 1);

         return true;
      };
   }

   private int getRows(List<TerritoryEco> territories, int cols) {
      if (territories.size() < cols) return 1;

      return (int) Math.ceil(territories.size() / (double) cols);
   }

   @Override
   public void close() {
      scanner.close();
      TerritoryHelperMenuFeature.OPEN_TERRITORY_MENU = false;

      if (!BUSY) ContainerUtils.closeContainer(CONTAINER_ID);
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
      final int[] position = new int[2];

      new Texture(BACKGROUND)
              .center()
              .perform(tex -> {
                 position[0] = (int) tex.getX();
                 position[1] = height / 2;
              })
              .then(Texture::new)
              .setTexture(FOREGROUND)
              .center()
              .offset(-4.5F, -7F)
              .build();

      renderTooltip(
              poseStack,
              GUILD_OUTPUT,
              (int) (position[0] - GUILD_OUTPUT_OFFSET_X - GUILD_OUTPUT_WIDTH - 4),
              (int) (position[1] - GUILD_OUTPUT_OFFSET_Y - GUILD_OUTPUT_HEIGHT / 2)
      );

      if (territories != null && territories.isEmpty()) {
         new Text("No Territories :(", 0, 0)
                 .setColor(CommonColors.WHITE)
                 .center()
                 .offset(-4.5F, -7F)
                 .build();
      } else if (!search.getTextBoxInput().isBlank()) {
         if (
                 getWidgets().stream()
                         .filter(widget -> AbstractEntry.class.isAssignableFrom(widget.getClass()))
                         .findAny().isEmpty()
         ) {
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

   public abstract class AbstractEntry extends Item {
      public static final float WIDTH = (16 * ITEM_SCALE) + 4.9F;
      public static final float HEIGHT = (16 * ITEM_SCALE) + 4.9F;

      public static final float ITEM_SIZE = 16 * ITEM_SCALE;
      public static final float X_OFFSET = (WIDTH / 2F - ITEM_SIZE / 2F);
      public static final float Y_OFFSET = (HEIGHT / 2F - ITEM_SIZE / 2F);

      private final TerritoryEco eco;

      private final Set<Filter> filters;
      private final StyledText acronym;

      private final List<ResourceType> production = new ArrayList<>();

      public AbstractEntry(TerritoryEco territory) {
         super(territory.getItem());

         this.eco = territory;

         this.filters = Filter.getFilters(territory);
         this.acronym = StyledText.fromString(getAcronym(territory));

         this.filters.forEach(filter -> counts.put(filter, (AbstractEntry) this));

         for (ResourceType resource : ResourceType.values()) {
            long prod = territory.getBaseProduction(resource);

            if (resource == ResourceType.EMERALDS) {
               if (prod > 9000) production.add(resource);
            } else if (prod > 0) production.addAll(Collections.nCopies((int) Math.ceil((prod / 900D) / 4D), resource));
         }
      }

      public TerritoryEco getTerritory() {
         return eco;
      }


      @SuppressWarnings("IntegerDivisionInFloatingPointContext")
      protected AbstractEntry update(int i, TerritoryPosition position) {
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
         if (IS_INSIDE && isMouseOver(mouseX, mouseY) && !BUSY && button == GLFW.GLFW_MOUSE_BUTTON_1) {
            click();

            return true;
         }

         return false;
      }

      protected abstract void click();

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
                    getX() - X_OFFSET,
                    getY() - Y_OFFSET + 1.5F - (getItemSupplier().get().getItem() == Items.MAP ? 1 : 0),
                    AbstractEntry.WIDTH,
                    AbstractEntry.HEIGHT,
                    color
            );

         super.render(poseStack, bufferSource, mouseX, mouseY, partialTick);

         poseStack.pushPose();
         poseStack.translate(0, 0, 300);

         float labelSize = 0.9F;

         if (showProduction) {
            int prodLines = (int) Math.ceil(production.size() / 2D);

            if (prodLines > 1) labelSize = 0.85F;

            float originX = (getX() + ITEM_SIZE / 2) - 1;
            float prodY = getY() + (ITEM_SIZE / 2) - ((Production.SIZE * prodLines) / 2F);

            int row = 0;
            int col = 0;

            int cols = 1;

            for (var iter = production.iterator(); iter.hasNext(); ) {
               var next = iter.next();
               if (col == 0) {
                  if (iter.hasNext()) cols = 2;
                  else cols = 1;
               }

               float prodX = originX + (col * Production.SIZE) - (cols * Production.SIZE) / 2F;

               Renderer.texture(
                       poseStack,
                       prodX,
                       prodY + (row * Production.SIZE),
                       next.getTexture()
               );

               col++;

               if (col == 2) {
                  col = 0;
                  row++;
               }
            }
         } else labelSize = 1F;

         Renderer.text(
                 poseStack, bufferSource,
                 acronym,
                 getX(), getY(), 0F,
                 CommonColors.ORANGE,
                 TextShadow.OUTLINE,
                 Math.min(labelSize, 16 * ITEM_SCALE / FontRenderer.getWidth(acronym, 0F))
         );

         poseStack.popPose();
         bufferSource.endBatch();
      }
   }

   private class Mask extends Widget<TerritoryScreen<Scanner>.Mask> {
      @Override
      public Screen.Element getElement() {
         return TerritoryScreen.this;
      }

      @Override
      protected void onRender(@NotNull PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, int mouseX, int mouseY, float partialTick) {
         Renderer.mask(poseStack, getX(), getY(), MASK);
      }
   }

   private class ClearMask extends Widget<TerritoryScreen<Scanner>.ClearMask> {

      @Override
      public Screen.Element getElement() {
         return TerritoryScreen.this;
      }

      @Override
      protected void onRender(@NotNull PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, int mouseX, int mouseY, float partialTick) {
         Renderer.clear_mask();
      }
   }

   protected record TerritoryPosition(float x, float y, int cols, float scroll) {
   }
}
