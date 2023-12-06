package com.busted_moments.core.http.api.guild;

import com.busted_moments.core.http.api.Printable;

public interface GuildType extends Printable {
   String name();
   String prefix();

   default int countTerritories() {
//      Territory.Owner guild = TerritoryObserver.getLatest().getOwner(name());
//
//      return guild == null ? 0 : guild.countTerritories();

      return 0;
   }

   @Override
   default String prettyPrint() {
      return name() + " [" + prefix() + "]";
   }

   static GuildType copyOf(GuildType type) {
      if (type instanceof GuildTypeImpl)
         return type;

      return new GuildTypeImpl(type.name(), type.prefix());
   }

   static GuildType valueOf(String name, String prefix) {
      return new GuildTypeImpl(name, prefix);
   }
}
