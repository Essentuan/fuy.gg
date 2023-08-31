package com.busted_moments.client.models.war;

import com.busted_moments.core.text.TextBuilder;
import com.busted_moments.core.util.NumUtil;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.ChatFormatting.*;

public class Tower implements Collection<Tower.Update> {
   private static final Pattern TOWER_REGEX = Pattern.compile("\\[(?<guild>.+)\\] (?<territory>.+) Tower - . (?<health>.+) \\((?<defense>.+)%\\) - .{1,2} (?<damagemin>.+)-(?<damagemax>.+) \\((?<attackspeed>.+)x\\)");

   final List<Update> UPDATES = new ArrayList<>();

   public Tower(Stats starting) {
      UPDATES.add(new Update(new Date(), starting, starting));
   }

   public Stats getInitialStats() {
      return UPDATES.get(0).before;
   }

   public Stats getStats() {
      return UPDATES.get(size() - 1).after;
   }

   @Override
   public int size() {
      return UPDATES.size();
   }

   @Override
   public boolean isEmpty() {
      return UPDATES.isEmpty();
   }

   @Override
   public boolean contains(Object o) {
      return UPDATES.contains(o);
   }

   @NotNull
   @Override
   public Iterator<Update> iterator() {
      return UPDATES.iterator();
   }

   @NotNull
   @Override
   public Object @NotNull [] toArray() {
      return UPDATES.toArray();
   }

   @NotNull
   @Override
   public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
      return UPDATES.toArray(a);
   }

   @Override
   public boolean add(Update update) {
      return UPDATES.add(update);
   }

   public boolean add(Stats stats) {
      return add(new Update(new Date(), getStats(), stats));
   }


   @Override
   public boolean remove(Object o) {
      return UPDATES.remove(o);
   }

   @Override
   public boolean containsAll(@NotNull Collection<?> c) {
      return new HashSet<>(UPDATES).containsAll(c);
   }

   @Override
   public boolean addAll(@NotNull Collection<? extends Update> c) {
      return UPDATES.addAll(c);
   }

   @Override
   public boolean removeAll(@NotNull Collection<?> c) {
      return UPDATES.removeAll(c);
   }

   @Override
   public boolean retainAll(@NotNull Collection<?> c) {
      return UPDATES.retainAll(c);
   }

   @Override
   public void clear() {
      UPDATES.clear();
   }

   public record Stats(long health, float defense, int damageMax, int damageMin, float attackSpeed) {
      public long ehp() {
         return (long) Math.floor(health / (1 - defense / 100));
      }

      public StyledText toText() {
         return TextBuilder.of("\u2665 ", DARK_RED)
                 .append(NumUtil.format(health), DARK_RED)
                 .append(" (", GRAY)
                 .append(defense + "%", GOLD)
                 .append(") -", GRAY)
                 .append(" \u2620 ", RED)
                 .append(NumUtil.format(damageMin) + "-", RED)
                 .append(NumUtil.format(damageMax), RED)
                 .append(" (", GRAY)
                 .append(attackSpeed + "x", AQUA)
                 .append(")", GRAY).build();
      }

      public static Optional<Stats> from(StyledText component) {
         Matcher matcher = component.getMatcher(TOWER_REGEX, PartStyle.StyleType.NONE);

         if (!matcher.matches()) return Optional.empty();

         return Optional.of(new Stats(
                 Long.parseLong(matcher.group("health")),
                 Float.parseFloat(matcher.group("defense")),
                 Integer.parseInt(matcher.group("damagemax")),
                 Integer.parseInt(matcher.group("damagemin")),
                 Float.parseFloat(matcher.group("attackspeed"))
         ));
      }
   }

   public record Update(Date date, Stats before, Stats after) {
      public <T extends Number> T difference(Function<Stats, T> stat, Function<Number, T> extractor) {
         Number n1 = stat.apply(before);
         Number n2 = stat.apply(after);

         return extractor.apply(Math.floor(n1.doubleValue() - n2.doubleValue()));
      }
   }
}
