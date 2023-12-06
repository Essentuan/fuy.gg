package com.busted_moments.core.http.api.guild;

import com.busted_moments.core.http.AbstractRequest;
import com.busted_moments.core.http.GetRequest;
import com.busted_moments.core.http.RateLimit;
import com.busted_moments.core.http.api.Printable;
import com.busted_moments.core.http.api.player.PlayerType;
import com.busted_moments.core.http.models.wynncraft.guild.GuildModel;
import com.busted_moments.core.http.models.wynncraft.guild.list.GuildList;
import com.busted_moments.core.json.Json;
import com.busted_moments.core.time.ChronoUnit;
import net.fabricmc.loader.impl.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public interface Guild extends Collection<Guild.Member>, GuildType {
   boolean contains(UUID uuid);

   default boolean contains(PlayerType other) {
      return contains(other.uuid());
   }

   Member owner();

   Member get(UUID uuid);

   default Member get(PlayerType other) {
      return get(other.uuid());
   }

   Collection<Member> get(Rank rank);

   int level();
   int progress();
   double xp();
   long required();

   long countWars();

   Date createdAt();

   Banner banner();

   Map<Integer, Season.Entry> results();

   default boolean add(Member o) {
      throw new UnsupportedOperationException();
   }

   default boolean remove(Object o) {
      throw new UnsupportedOperationException();
   }

   default boolean addAll(Collection<? extends Member> c) {
      throw new UnsupportedOperationException();
   }

   default boolean removeAll(Collection<?> c) {
      throw new UnsupportedOperationException();
   }

   @Override
   default boolean removeIf(Predicate<? super Member> filter) {
      throw new UnsupportedOperationException();
   }

   default boolean retainAll(Collection<?> c) {
      throw new UnsupportedOperationException();
   }

   default void clear() {
      throw new UnsupportedOperationException();
   }

   interface Member extends PlayerType {
      Rank rank();

      Date joinedAt();

      long contributed();
   }

   interface Banner extends Collection<Banner.Layer> {
      Color color();

      int tier();

      Structure structure();

      default boolean isDefault() {
         return color() == Color.WHITE && tier() == 0 && isEmpty();
      }

      default boolean add(Layer o) {
         throw new UnsupportedOperationException();
      }

      default boolean remove(Object o) {
         throw new UnsupportedOperationException();
      }

      default boolean addAll(Collection<? extends Layer> c) {
         throw new UnsupportedOperationException();
      }

      default boolean removeAll(Collection<?> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      default boolean removeIf(Predicate<? super Layer> filter) {
         throw new UnsupportedOperationException();
      }

      default boolean retainAll(Collection<?> c) {
         throw new UnsupportedOperationException();
      }

      default void clear() {
         throw new UnsupportedOperationException();
      }

      interface Layer {
         Color color();

         Pattern pattern();
      }

      enum Structure {
         DEFAULT,
         DERNIC_BANNER,
         LIGHT_BANNER,
         CORRUPTED_BANNER,
         MOLTEN_BANNER,
         DESERT_BANNER,
         FUTURISTIC_BANNER,
         STEAMPUNK_BANNER,
         NATURE_BANNER,
         DECAY_BANNER,
         ICE_BANNER,
         HIVE_BANNER,
         JESTER_BANNER,
         BEACHSIDE_BANNER,
         OTHERWORLDLY_BANNER;

         private static final Map<String, Structure> lookup = new HashMap<>();

         static {
            for (Structure structure : values()) {
               lookup.put(
                       structure.name().toLowerCase().replace("_", ""),
                       structure
               );
            }
         }

         public static Structure of(String string) {
            return lookup.computeIfAbsent(string.toLowerCase(), s -> {
               try {
                  return Structure.valueOf(string);
               } catch (Exception e) {
                  return Structure.DEFAULT;
               }
            });
         }
      }

      enum Pattern {
         STRIPE_BOTTOM,
         STRIPE_TOP,
         STRIPE_LEFT,
         STRIPE_RIGHT,
         STRIPE_MIDDLE,
         STRIPE_CENTER,
         STRIPE_DOWNRIGHT,
         STRIPE_DOWNLEFT,
         STRIPE_SMALL,
         CROSS,
         STRAIGHT_CROSS,
         DIAGONAL_LEFT,
         DIAGONAL_RIGHT_MIRROR,
         DIAGONAL_LEFT_MIRROR,
         DIAGONAL_RIGHT,
         HALF_VERTICAL,
         HALF_VERTICAL_MIRROR,
         HALF_HORIZONTAL,
         HALF_HORIZONTAL_MIRROR,
         SQUARE_BOTTOM_LEFT,
         SQUARE_BOTTOM_RIGHT,
         SQUARE_TOP_LEFT,
         SQUARE_TOP_RIGHT,
         TRIANGLE_BOTTOM,
         TRIANGLE_TOP,
         TRIANGLES_BOTTOM,
         TRIANGLES_TOP,
         CIRCLE_MIDDLE,
         RHOMBUS_MIDDLE,
         BORDER,
         CURLY_BORDER,
         BRICKS,
         GRADIENT,
         GRADIENT_UP,
         CREEPER,
         SKULL,
         FLOWER,
         MOJANG,
         GLOBE,
         PIGLIN
      }

      enum Color {
         WHITE,
         ORANGE,
         MAGENTA,
         LIGHT_BLUE,
         YELLOW,
         LIME,
         PINK,
         GRAY,
         SILVER,
         CYAN,
         PURPLE,
         BLUE,
         BROWN,
         GREEN,
         RED,
         BLACK;
      }
   }

   enum Rank implements Printable {
      OWNER,
      CHIEF,
      STRATEGIST,
      CAPTAIN,
      RECRUITER,
      RECRUIT;

      private final String friendlyName;

      Rank() {
         this.friendlyName = StringUtil.capitalize(name().toLowerCase());
      }

      @Override
      public String prettyPrint() {
         return friendlyName;
      }

      public int countStars() {
         return Math.max(values().length - ordinal() - 1, 0);
      }
   }

   class Utils {
      private Utils() {
      }

      private static final java.util.List<Double> LEVELS = new ArrayList<>();

      static double required(int level) {
         if (level < 0)
            return 0;

         while (LEVELS.size() < level + 1)
            LEVELS.add(null);

         Double xp = LEVELS.get(level);
         if (xp != null)
            return xp;

         xp = level == 0 ? 0 : required(level - 1) + (20000 * Math.pow(1.15, level - 1D));
         LEVELS.set(level, xp);

         return xp;
      }
   }

   interface List extends Collection<GuildType> {
      default GuildType get(String string) {
         GuildType entry = byName(string);
         if (entry == null)
            return byPrefix(string);

         return entry;
      }

      default GuildType get(GuildType guild) {
         return get(guild.name());
      }

      default boolean contains(String string) {
         return containsName(string) || containsPrefix(string);
      }

      default boolean contains(GuildType guild) {
         return contains(guild.name());
      }

      GuildType byName(String name);

      GuildType byPrefix(String prefix);

      boolean containsName(String name);

      boolean containsPrefix(String prefix);

      default boolean add(GuildType o) {
         throw new UnsupportedOperationException();
      }

      default boolean remove(Object o) {
         throw new UnsupportedOperationException();
      }

      default boolean addAll(Collection<? extends GuildType> c) {
         throw new UnsupportedOperationException();
      }

      default boolean removeAll(Collection<?> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      default boolean removeIf(Predicate<? super GuildType> filter) {
         throw new UnsupportedOperationException();
      }

      default boolean retainAll(Collection<?> c) {
         throw new UnsupportedOperationException();
      }

      default void clear() {
         throw new UnsupportedOperationException();
      }

      static List empty() {
         return EmptyGuildList.EMPTY;
      }

      @AbstractRequest.Definition(
              route = "https://api.wynncraft.com/v3/guild/list/guild",
              ratelimit = RateLimit.NONE,
              cache_unit = ChronoUnit.HOURS
      )
      class Request extends GetRequest<List> {

         @Nullable
         @Override
         protected List get(Json json) {
            return json.wrap(GuildList::new);
         }
      }
   }

   @AbstractRequest.Definition(
           route = "https://thesimpleones.net/api/guild?q=%s",
           ratelimit = RateLimit.NONE,
           cache_length = 10
   )
   class Request extends GetRequest<Guild> {
      public Request(String guild) {
         super(guild);
      }

      @Nullable
      @Override
      protected Guild get(Json json) {
         return json.wrap(GuildModel::new);
      }
   }

   static GuildType valueOf(String string) {
      return new List.Request().await().orElseThrow().get(string);
   }
}