package com.busted_moments.core.json.codecs;

import com.busted_moments.core.json.AbstractCodec;
import com.busted_moments.core.json.Annotations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@AbstractCodec.Definition(ItemStack.class)
public class ItemStackCodec extends AbstractCodec<ItemStack, String> {
   @Override
   public @Nullable String write(ItemStack value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      return toString(value, type, typeArgs);
   }

   @Override
   public @Nullable ItemStack read(@NotNull String value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      return fromString(value, type, typeArgs);
   }

   @Override
   public ItemStack fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      return ItemStack.of(TagParser.parseTag(string));
   }

   @Override
   public String toString(ItemStack value, Class<?> type, Type... typeArgs) throws Exception {
      return value.save(new CompoundTag()).toString();
   }
}
