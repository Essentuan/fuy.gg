package com.busted_moments.core.http.models.wynncraft.guild.banner;

import com.busted_moments.core.http.api.guild.Guild;
import com.busted_moments.core.json.BaseModel;
import com.busted_moments.core.json.Json;
import com.busted_moments.core.util.iterators.Iter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class BannerModel extends BaseModel implements Guild.Banner {
   @Key private Color base = Color.WHITE;
   @Key private int tier = 0;
   @Key private Structure structure = Structure.DEFAULT;
   @Key private List<BaseLayer> layers = new ArrayList<>();

   public BannerModel() {}

   @Override
   protected void onPreLoad(Json json) {
      if (json.has("baseColor"))
         json.set("base", json.getString("baseColor"));

      if (json.has("bannerTier"))
         json.set("tier", json.getInteger("bannerTier"));
   }

   @Override
   public Color color() {
      return base;
   }

   @Override
   public int tier() {
      return tier;
   }

   @Override
   public Structure structure() {
      return structure;
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
      return Iter.immutable(layers.iterator()).cast();
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
   public boolean containsAll(@NotNull Collection<?> c) {
      return new HashSet<>(layers).containsAll(c);
   }
}
