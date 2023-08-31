package com.busted_moments.core.json.codecs;

import com.busted_moments.core.json.Codec;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@Codec.Definition(Duration.class)
public class DurationCodec extends Codec<Duration, Number> {
   @Override
   public @Nullable Double write(Duration value, Class<?> type, Type... typeArgs) {
      return value.toSeconds();
   }

   @Override
   public @Nullable Duration read(@NotNull Number value, Class<?> type, Type... typeArgs) {
      return Duration.of(value, TimeUnit.SECONDS);
   }

   @Override
   public Duration fromString(String string, Class<?> type, Type... typeArgs)    {
      return Duration.of(Double.parseDouble(string), TimeUnit.SECONDS);
   }

   @Override
   public String toString(Duration value, Class<?> type, Type... typeArgs) {
      return Double.toString(value.toSeconds());
   }
}
