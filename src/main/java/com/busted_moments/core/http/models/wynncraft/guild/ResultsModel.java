package com.busted_moments.core.http.models.wynncraft.guild;

import com.busted_moments.core.http.api.guild.Season;
import com.busted_moments.core.json.BaseModel;
import com.busted_moments.core.json.Json;
import com.busted_moments.core.util.Range;
import com.busted_moments.core.util.iterators.Iter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ResultsModel extends BaseModel implements Map<Integer, Season.Entry> {
   String name = "Nobody";
   String prefix = "NONE";

   private final ArrayList<Entry> list = new ArrayList<>();

   private Set<Integer> keySet;
   private Collection<Season.Entry> values;
   private Set<Map.Entry<Integer, Season.Entry>> entries;

   @Override
   public BaseModel load(Json json) {
      for (Map.Entry<String, Object> entry : json.entrySet()) {
         int season = Integer.parseInt(entry.getKey());
         Entry result = new Entry((Json) entry.getValue());

         ensureCapacity(season + 1);

         list.set(season, result);
      }

      keySet = new KeySet();
      values = new Values();

      Set<Map.Entry<Integer, Season.Entry>> set = new LinkedHashSet<>(size());

      for (int i = 0; i < size(); i++)
         set.add(new MapEntry(i, get(i)));

      entries = Collections.unmodifiableSet(set);

      return this;
   }

   @Override
   public int size() {
      return list.size();
   }

   @Override
   public boolean isEmpty() {
      return list.isEmpty();
   }

   @Override
   public boolean containsKey(Object key) {
      return key instanceof Integer integer && integer < size() && integer >= 0;
   }

   @Override
   @SuppressWarnings("SuspiciousMethodCalls")
   public boolean containsValue(Object value) {
      return value instanceof Season.Entry entry && list.contains(entry);
   }

   @Override
   public Season.Entry get(Object key) {
      if (!(key instanceof Integer integer) || integer >= size() || integer < 0)
         return null;

      return list.get(integer);
   }

   @NotNull
   @Override
   public Set<Integer> keySet() {
      return keySet;
   }

   @NotNull
   @Override
   public Collection<Season.Entry> values() {
      return values;
   }

   @NotNull
   @Override
   public Set<Map.Entry<Integer, Season.Entry>> entrySet() {
      return entries;
   }

   @Override
   public Json toJson() {
      Json json = Json.empty();

      for (int i = 0; i < size(); i++)
         json.set(String.valueOf(i), list.get(i).toJson());

      return json;
   }

   private void ensureCapacity(int capacity) {
      list.ensureCapacity(capacity);

      while (list.size() < capacity)
         list.add(empty);
   }

   @Override
   public boolean equals(Object o) {
      return o instanceof ResultsModel other
              && (Objects.equals(name, other.name)
              && Objects.equals(prefix, other.prefix)
              && Objects.equals(list, other.list));
   }

   @Override
   public int hashCode() {
      return Objects.hash(name, prefix, list);
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder("SeasonResults{name=");
      builder.append(name)
              .append(", prefix=")
              .append(prefix)
              .append(", results={");

      for (int i = 0; i < size(); i++) {
         var entry = list.get(i);

         if (i > 0)
            builder.append(", ");

         builder.append(i)
                 .append("=")
                 .append('{')
                 .append("rating=")
                 .append(entry.rating)
                 .append(", territories=")
                 .append(entry.finalTerritories)
                 .append("}");
      }

      return builder.append("}}").toString();
   }

   private final Entry empty = new Entry(Json.empty());

   private class Entry implements Season.Entry {
      private final long rating;
      private final int finalTerritories;

      public Entry(Json json) {
         if (json == null)
            json = Json.empty();

         rating = json.getLong("rating", 0);
         finalTerritories = json.getInteger("finalTerritories", 0);
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
      public long rating() {
         return rating;
      }

      @Override
      public int territories() {
         return finalTerritories;
      }

      private boolean isEmpty() {
         return rating == 0 && finalTerritories == 0;
      }

      public Json toJson() {
         return Json.of("rating", rating).set("finalTerritories", finalTerritories);
      }

      @Override
      public boolean equals(Object object) {
         return this == object ||
                 (object instanceof Season.Entry other
                         && Objects.equals(name, other.name())
                         && Objects.equals(prefix, other.prefix())
                         && Objects.equals(rating, other.rating())
                         && Objects.equals(finalTerritories, other.territories()));
      }

      @Override
      public int hashCode() {
         return Objects.hash(name, prefix, rating, finalTerritories);
      }

      @Override
      public String toString() {
         return "Entry{" +
                 "name=" + name +
                 ", prefix=" + prefix +
                 ", rating=" + rating +
                 ", finalTerritories=" + finalTerritories +
                 '}';
      }
   }

   private class KeySet extends Range<Integer> {
      protected KeySet() {
         super(0, list.size() - 1, 1);
      }

      @Override
      protected Integer increment(Integer value) {
         return value + step;
      }
   }

   private class Values implements Collection<Season.Entry> {
      @Override
      public int size() {
         return list.size();
      }

      @Override
      public boolean isEmpty() {
         return list.isEmpty();
      }

      @Override
      public boolean contains(Object o) {
         return list.contains(o);
      }

      @NotNull
      @Override
      public Iterator<Season.Entry> iterator() {
         return Iter.immutable(list.iterator(), e -> e);
      }

      @NotNull
      @Override
      public Object[] toArray() {
         return list.toArray();
      }

      @NotNull
      @Override
      public <T> T[] toArray(@NotNull T[] a) {
         return list.toArray(a);
      }

      @Override
      public boolean add(Season.Entry entry) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(Object o) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean containsAll(@NotNull Collection<?> c) {
         return list.containsAll(c);
      }

      @Override
      public boolean addAll(@NotNull Collection<? extends Season.Entry> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean removeAll(@NotNull Collection<?> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean retainAll(@NotNull Collection<?> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void clear() {
         throw new UnsupportedOperationException();
      }

      @Override
      public int hashCode() {
         return list.hashCode();
      }

      @Override
      public boolean equals(Object obj) {
         return list.equals(obj);
      }

      @Override
      public String toString() {
         return list.toString();
      }
   }

   private record MapEntry(Integer index, Season.Entry entry) implements Map.Entry<Integer, Season.Entry> {
      @Override
      public Integer getKey() {
         return index;
      }

      @Override
      public Season.Entry getValue() {
         return entry;
      }

      @Override
      public Season.Entry setValue(Season.Entry value) {
         throw new UnsupportedOperationException();
      }
   }

   @Nullable
   @Override
   public Season.Entry put(Integer key, Season.Entry value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Season.Entry remove(Object key) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void putAll(@NotNull Map<? extends Integer, ? extends Season.Entry> m) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void clear() {
      throw new UnsupportedOperationException();
   }

   public static ResultsModel empty() {
      return new ResultsModel();
   }
}
