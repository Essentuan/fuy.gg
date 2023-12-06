package com.busted_moments.core.http.models.wynncraft.guild.list;

import com.busted_moments.core.http.api.guild.Guild;
import com.busted_moments.core.http.api.guild.GuildType;
import com.busted_moments.core.json.BaseModel;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class GuildList extends BaseModel implements Guild.List {
   @Key("root") private Set<Entry> guilds;

   private final Map<String, GuildType> nameMap = new HashMap<>();
   private final Map<String, GuildType> prefixMap = new HashMap<>();

   @Override
   protected void onLoad() {
      for (Entry entry : guilds) {
         nameMap.putIfAbsent(entry.name, entry);
         prefixMap.putIfAbsent(entry.prefix, entry);
      }
   }

   @Override
   public GuildType byName(String name) {
      return nameMap.get(name);
   }

   @Override
   public GuildType byPrefix(String prefix) {
      return prefixMap.get(prefix);
   }

   @Override
   public boolean containsName(String name) {
      return nameMap.containsKey(name);
   }

   @Override
   public boolean containsPrefix(String prefix) {
      return nameMap.containsKey(prefix);
   }

   @Override
   public int size() {
      return guilds.size();
   }

   @Override
   public boolean isEmpty() {
      return guilds.isEmpty();
   }

   @Override
   public boolean contains(Object o) {
      if (o instanceof String string)
         return containsName(string) || containsPrefix(string);

      return guilds.contains(o);
   }

   @NotNull
   @Override
   public Iterator<GuildType> iterator() {
      return new Iter();
   }

   @NotNull
   @Override
   public Object @NotNull [] toArray() {
      return guilds.toArray();
   }

   @NotNull
   @Override
   public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
      return guilds.toArray(a);
   }

   @Override
   public boolean containsAll(@NotNull Collection<?> c) {
      return guilds.containsAll(c);
   }

   private static class Entry extends BaseModel implements GuildType {
      @Key private String name;
      @Key private String prefix;

      public Entry() {}

      @Override
      public String name() {
         return name;
      }

      @Override
      public String prefix() {
         return prefix;
      }
   }

   private class Iter implements Iterator<GuildType> {
      private final Iterator<Entry> base = guilds.iterator();

      @Override
      public boolean hasNext() {
         return base.hasNext();
      }

      @Override
      public GuildType next() {
         return base.next();
      }
   }
}
