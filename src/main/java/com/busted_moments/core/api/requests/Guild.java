package com.busted_moments.core.api.requests;

import com.busted_moments.core.api.internal.GetRequest;
import com.busted_moments.core.api.internal.RateLimit;
import com.busted_moments.core.api.requests.player.Player;
import com.busted_moments.core.json.Json;
import com.busted_moments.core.json.template.JsonTemplate;
import com.busted_moments.core.time.TimeUnit;
import com.busted_moments.core.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Guild extends JsonTemplate implements Collection<Guild.Member> {
   @Entry private String name;
   @Entry private String prefix;
   @Entry private Map<UUID, Member> members;
   @Entry private int level;
   @Entry private long xp;
   @Entry private long xp_required;
   @Entry private double level_progress;
   @Entry private Banner banner;
   @Entry private Date dateCreated;

   private Member owner;

   @Override
   public JsonTemplate load(Json json) {
      super.load(json);

      stream().filter(member -> member.getRank() == Rank.OWNER)
              .findFirst().ifPresent(member -> owner = member);

      return this;
   }

   public String getName() {
      return name;
   }

   public String getPrefix() {
      return prefix;
   }

   public Member getOwner() {
      return owner;
   }

   public int getLevel() {
      return level;
   }

   public long getXp() {
      return xp;
   }

   public long getRequired() {
      return xp_required;
   }

   public double getProgress() {
      return level_progress;
   }

   public Banner getBanner() {
      return banner;
   }

   public Date getDateCreated() {
      return dateCreated;
   }

   public Member get(UUID uuid) {
      return members.get(uuid);
   }

   public Member get(Member member) {
      return get(member.uuid);
   }

   public Member get(Player player) {
      return get(player.getUuid());
   }

   @Override
   public int size() {
      return members.size();
   }

   @Override
   public boolean isEmpty() {
      return members.isEmpty();
   }

   @Override
   @SuppressWarnings("SuspiciousMethodCalls")
   public boolean contains(Object o) {
      if (o instanceof UUID uuid) return members.containsKey(uuid);
      else if (o instanceof Member member) return members.containsKey(member.uuid);

      return members.containsValue(o);
   }

   public boolean contains(UUID uuid) {
      return members.containsKey(uuid);
   }

   public boolean contains(Member member) {
      return contains(member.uuid);
   }

   public boolean contains(Player player) {
      return contains(player.getUuid());
   }

   @NotNull
   @Override
   public Iterator<Member> iterator() {
      return members.values().iterator();
   }

   @NotNull
   @Override
   public Object @NotNull [] toArray() {
      return members.values().toArray();
   }

   @NotNull
   @Override
   public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
      return members.values().toArray(a);
   }

   @Override
   public boolean add(Member member) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean remove(Object o) {
      return members.values().remove(o);
   }

   @Override
   public boolean containsAll(@NotNull Collection<?> c) {
      return members.values().containsAll(c);
   }

   @Override
   public boolean addAll(@NotNull Collection<? extends Member> c) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean removeAll(@NotNull Collection<?> c) {
      return members.values().removeAll(c);
   }

   @Override
   public boolean retainAll(@NotNull Collection<?> c) {
      return members.values().retainAll(c);
   }

   @Override
   public void clear() {
      members.clear();
   }

   public static class Member extends JsonTemplate {
      @Entry private String username;
      @Entry private UUID uuid;
      @Entry private Rank rank;
      @Entry private long contributed;
      @Entry private Date joined;
      @Entry @Nullable private String world;

      public String getUsername() {
         return username;
      }

      public UUID getUuid() {
         return uuid;
      }

      public Rank getRank() {
         return rank;
      }

      public long getContributed() {
         return contributed;
      }

      public Date getJoinedAt() {
         return joined;
      }

      public Optional<String> getWorld() {
         return Optional.ofNullable(world);
      }
   }

   public enum Rank {
      OWNER("Owner"),
      CHIEF("Chief"),
      STRATEGIST("Strategist"),
      CAPTAIN("Captain"),
      RECRUITER("Recruiter"),
      RECRUIT("Recruit");

      private final String prettyPrint;

      Rank(String string) {
         this.prettyPrint = string;
      }

      public int countStars() {
         List<Rank> ranks = Arrays.stream(values()).toList();

         return Math.abs(ranks.indexOf(this) - ranks.size()) - 1;
      }

      @SuppressWarnings("UnnecessaryUnicodeEscape")
      public String getStars() {
         return StringUtil.nCopies("\u2605", countStars());
      }

      public String prettyPrint() {
         return this.prettyPrint;
      }
   }

   public static class Banner extends JsonTemplate implements List<Banner.Layer> {
      @Entry
      private Color baseColor;
      @Entry
      private int bannerTier;
      @Entry
      private List<Layer> layers;

      public Color getBaseColor() {
         return baseColor;
      }

      public int getTier() {
         return bannerTier;
      }

      @Override
      public int size() {
         return layers.size();
      }

      @Override
      public boolean isEmpty() {
         return layers.isEmpty();
      }

      @Override
      public boolean contains(Object o) {
         return layers.contains(o);
      }

      @NotNull
      @Override
      public Iterator<Layer> iterator() {
         return layers.iterator();
      }

      @NotNull
      @Override
      public Object @NotNull [] toArray() {
         return layers.toArray();
      }

      @NotNull
      @Override
      public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
         return layers.toArray(a);
      }

      @Override
      public boolean add(Layer layer) {
         return layers.add(layer);
      }

      @Override
      public boolean remove(Object o) {
         return layers.remove(o);
      }

      @Override
      public boolean containsAll(@NotNull Collection<?> c) {
         return new HashSet<>(layers).containsAll(c);
      }

      @Override
      public boolean addAll(@NotNull Collection<? extends Layer> c) {
         return layers.addAll(c);
      }

      @Override
      public boolean addAll(int index, @NotNull Collection<? extends Layer> c) {
         return layers.addAll(index, c);
      }

      @Override
      public boolean removeAll(@NotNull Collection<?> c) {
         return layers.removeAll(c);
      }

      @Override
      public boolean retainAll(@NotNull Collection<?> c) {
         return layers.retainAll(c);
      }

      @Override
      public void clear() {
         layers.clear();
      }

      @Override
      public Layer get(int index) {
         return layers.get(index);
      }

      @Override
      public Layer set(int index, Layer element) {
         return layers.set(index, element);
      }

      @Override
      public void add(int index, Layer element) {
         layers.add(index, element);
      }

      @Override
      public Layer remove(int index) {
         return layers.remove(index);
      }

      @Override
      public int indexOf(Object o) {
         return layers.indexOf(o);
      }

      @Override
      public int lastIndexOf(Object o) {
         return layers.lastIndexOf(o);
      }

      @NotNull
      @Override
      public ListIterator<Layer> listIterator() {
         return layers.listIterator();
      }

      @NotNull
      @Override
      public ListIterator<Layer> listIterator(int index) {
         return layers.listIterator(index);
      }

      @NotNull
      @Override
      public List<Layer> subList(int fromIndex, int toIndex) {
         return layers.subList(fromIndex, toIndex);
      }

      public static class Layer extends JsonTemplate {
         @Entry
         private Pattern pattern;
         @Entry
         private Color color;

         public Pattern getPattern() {
            return pattern;
         }

         public Color getColor() {
            return color;
         }
      }

      public enum Color {
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

      public enum Pattern {
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
   }

   @com.busted_moments.core.api.internal.Request.Definition(route = "https://thesimpleones.net/api/guild?q=%s", ratelimit = RateLimit.NONE, cache_length = 5)
   public static class Request extends GetRequest<Guild> {
      public Request(String guild) {
         super(guild);
      }

      @Override
      protected Guild get(Json json) {
         return json.wrap(Guild::new);
      }
   }
}
