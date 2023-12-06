package com.busted_moments.core.http.api.guild;

import com.busted_moments.core.util.iterators.Iter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

class EmptyGuildList implements Guild.List {
   @Override
   public GuildType byName(String name) {
      return null;
   }

   @Override
   public GuildType byPrefix(String prefix) {
      return null;
   }

   @Override
   public boolean containsName(String name) {
      return false;
   }

   @Override
   public boolean containsPrefix(String prefix) {
      return false;
   }

   @Override
   public int size() {
      return 0;
   }

   @Override
   public boolean isEmpty() {
      return true;
   }

   @Override
   public boolean contains(Object o) {
      return false;
   }

   @NotNull
   @Override
   public Iterator<GuildType> iterator() {
      return Iter.empty();
   }

   @NotNull
   @Override
   public Object @NotNull [] toArray() {
      return new Object[0];
   }

   @NotNull
   @Override
   public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
      return (T[]) Array.newInstance(a.getClass().componentType(), 0);
   }

   @Override
   public boolean containsAll(@NotNull Collection<?> c) {
      return c.isEmpty();
   }

   static final Guild.List EMPTY = new EmptyGuildList();
}
