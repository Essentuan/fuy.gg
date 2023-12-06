package com.busted_moments.core.http.models.wynncraft.guild;

import com.busted_moments.core.http.api.guild.Guild;
import com.busted_moments.core.json.BaseModel;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class MemberModel extends BaseModel implements Guild.Member {
   @Key private String username;
   @Key private UUID uuid;
   @Key private Guild.Rank rank;
   @Key private long contributed;
   @Key private Date joined;
   @Key @Null private String world;

   @Override
   public String username() {
      return username;
   }

   @Override
   public UUID uuid() {
      return uuid;
   }

   @Override
   public Optional<String> world() {
      return Optional.ofNullable(world);
   }

   @Override
   public Guild.Rank rank() {
      return rank;
   }

   @Override
   public Date joinedAt() {
      return joined;
   }

   @Override
   public long contributed() {
      return contributed;
   }
}
