package com.busted_moments.core.json.codecs;

import com.busted_moments.core.json.Codec;
import com.busted_moments.core.util.Priority;
import com.busted_moments.core.util.Reflection;
import net.minecraftforge.eventbus.api.EventPriority;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@Codec.Definition(value = Object.class, priority = Priority.LOWEST)
public class ObjectCodec extends Codec<Object, Object> {
   @Nullable
   @Override
   public Object write(Object value, Class<?> type, Type... typeArgs) throws Exception {
      return value;
   }

   @Override
   public @NotNull Object read(@NotNull Object value, Class<?> type, Type... typeArgs) throws Exception {
      return value;
   }

   @Override
   public Object fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      throw new UnsupportedOperationException();
   }

   @Override
   public String toString(Object value, Class<?> type, Type... typeArgs) throws Exception {
      throw new UnsupportedOperationException();
   }
};
