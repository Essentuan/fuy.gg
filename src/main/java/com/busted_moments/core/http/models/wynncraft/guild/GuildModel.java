package com.busted_moments.core.http.models.wynncraft.guild;

import com.busted_moments.core.http.api.guild.Guild;
import com.busted_moments.core.http.api.guild.Season;
import com.busted_moments.core.http.api.player.PlayerType;
import com.busted_moments.core.http.models.wynncraft.guild.banner.BannerModel;
import com.busted_moments.core.json.BaseModel;
import com.busted_moments.core.util.iterators.Iter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class GuildModel extends BaseModel implements Guild {
   @Key private String name;
   @Key private String prefix;
   @Key private Map<UUID, MemberModel> members;
   @Key private Date dateCreated;
   @Key private int level;
   @Key private long xp;
   @Key private long xp_required;
   @Key private int level_progress;
   @Key private int wars;
   @Key private ResultsModel season_results;
   @Key private BannerModel banner;

   private Multimap<Guild.Rank, Member> ranks;
   private Member owner;

   @Override
   protected void onLoad() {
      ranks = MultimapBuilder.enumKeys(Guild.Rank.class).arrayListValues().build();

      for (Member member : this)
         ranks.put(member.rank(), member);

      owner = Iterables.getFirst(ranks.get(Rank.OWNER), null);
   }

   @Override
   public boolean contains(UUID uuid) {
      return members.containsKey(uuid);
   }

   @Override
   public Member owner() {
      return owner;
   }

   @Override
   public Member get(UUID uuid) {
      return members.get(uuid);
   }

   @Override
   public Collection<Member> get(Rank rank) {
      return ranks.get(rank);
   }

   @Override
   public int level() {
      return level;
   }

   @Override
   public int progress() {
      return level_progress;
   }

   @Override
   public double xp() {
      return xp;
   }

   @Override
   public long required() {
      return xp_required;
   }

   @Override
   public long countWars() {
      return wars;
   }

   @Override
   public Date createdAt() {
      return dateCreated;
   }

   @Override
   public Banner banner() {
      return banner;
   }

   @Override
   public Map<Integer, Season.Entry> results() {
      return season_results;
   }

   @Override
   public String name() {
      return name;
   }

   @Override
   public String prefix() {
      return prefix;
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
   public boolean contains(Object o) {
      if (o instanceof UUID uuid)
         return contains(uuid);
      else if (o instanceof PlayerType player)
         return contains(player.uuid());
      else
         return false;
   }

   @NotNull
   @Override
   public Iterator<Member> iterator() {
      return Iter.immutable(members.values().iterator()).cast();
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
   public boolean containsAll(@NotNull Collection<?> c) {
      for (Object obj : c)
         if (!contains(obj))
            return false;

      return true;
   }
}
