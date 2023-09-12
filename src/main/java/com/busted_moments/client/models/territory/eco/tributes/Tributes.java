package com.busted_moments.client.models.territory.eco.tributes;

import com.busted_moments.client.models.territory.eco.types.ResourceType;
import com.busted_moments.client.util.ChatUtil;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.busted_moments.client.models.territory.eco.Patterns.PRODUCITON_PATTERN;
import static com.wynntils.utils.mc.McUtils.player;

public class Tributes implements Map<ResourceType, Tributes.Entry> {
   private static final Pattern ENTRY_PATTERN = Pattern.compile("^(?<guild>.+) \\[(?<prefix>.+)]");

   private final String guild;
   private final Map<ResourceType, Entry> tributes;

   Tributes(String guild, Map<ResourceType, Entry> tributes) {
      this.guild = guild;
      this.tributes = tributes;
   }

   public String guild() {
      return guild;
   }

   @Override
   public int size() {
      return tributes.size();
   }

   @Override
   public boolean isEmpty() {
      return tributes.isEmpty();
   }

   @Override
   public boolean containsKey(Object key) {
      return tributes.containsKey(key);
   }

   @Override
   public boolean containsValue(Object value) {
      return tributes.containsValue(value);
   }

   @Override
   public Entry get(Object key) {
      return tributes.get(key);
   }

   @Nullable
   @Override
   public Entry put(ResourceType key, Entry value) {
      return tributes.put(key, value);
   }

   @Override
   public Entry remove(Object key) {
      return tributes.remove(key);
   }

   @Override
   public void putAll(@NotNull Map<? extends ResourceType, ? extends Entry> m) {
      tributes.putAll(m);
   }

   @Override
   public void clear() {
      tributes.clear();
   }

   @NotNull
   @Override
   public Set<ResourceType> keySet() {
      return tributes.keySet();
   }

   @NotNull
   @Override
   public Collection<Entry> values() {
      return tributes.values();
   }

   @NotNull
   @Override
   public Set<Map.Entry<ResourceType, Entry>> entrySet() {
      return tributes.entrySet();
   }

   @Override
   public String toString() {
      return "Tributes{" +
              "guild='" + guild + '\'' +
              ", tributes=" + tributes +
              '}';
   }

   @Override
   public boolean equals(Object object) {
      if (this == object) return true;
      if (!(object instanceof Tributes tributes1)) return false;
      return Objects.equals(guild, tributes1.guild) && Objects.equals(tributes, tributes1.tributes);
   }

   @Override
   public int hashCode() {
      return Objects.hash(guild, tributes);
   }

   public static Optional<Tributes> from(ItemStack stack) {
      if (!(stack.getItem() instanceof BannerItem)) return Optional.empty();
      Matcher matcher = StyledText.fromComponent(stack.getDisplayName()).getMatcher(ENTRY_PATTERN, PartStyle.StyleType.NONE);
      if (!matcher.matches()) return Optional.empty();

      Tributes tributes = empty(matcher.group("guild").substring(1));

      for (Component component : stack.getTooltipLines(player(), TooltipFlag.NORMAL)) {
         String text = ChatUtil.strip(component).replace("Á", "");

         if (((matcher = PRODUCITON_PATTERN.matcher(text)).matches())) {
            ResourceType resource = ResourceType.of(matcher.group("resource"));
            long amount = Long.parseLong(matcher.group("production"));

            boolean sent = matcher.group("sign").equals("-");

            tributes.compute(resource, (r, entry) -> {
               if (sent) return Entry.addSent(entry, amount);
               else return Entry.addReceived(entry, amount);
            });
         }
      }

      if (tributes.size() == 0) return Optional.empty();
      else return Optional.of(tributes);
   }

   public static Tributes empty(String guild) {
      return new Tributes(guild, new HashMap<>());
   }

   public record Entry(long sent, long received) {
      private static final Entry EMPTY = new Entry(0, 0);

      static Entry addSent(@Nullable Entry other, long sent) {
         if (other == null) return new Entry(sent, 0);

         return new Entry(
                 sent + other.sent,
                 other.received
         );
      }

      static Entry setSent(@Nullable Entry other, long sent) {
         if (other == null) return new Entry(sent, 0);

         return new Entry(
                 sent,
                 other.received
         );
      }

      static Entry addReceived(@Nullable Entry other, long received) {
         if (other == null) return new Entry(0, received);

         return new Entry(
                 other.sent,
                 received + other.received
         );
      }

      static Entry setReceived(@Nullable Entry other, long received) {
         if (other == null) return new Entry(0, received);

         return new Entry(
                 other.sent,
                 received
         );
      }

      public static Entry empty() {
         return EMPTY;
      }
   }
}
