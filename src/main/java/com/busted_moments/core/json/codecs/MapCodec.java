package com.busted_moments.core.json.codecs;

import com.busted_moments.core.collector.LinkedMapCollector;
import com.busted_moments.core.json.Codec;
import com.busted_moments.core.json.Json;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.stream.Collectors;

@Codec.Definition(Map.class)
@SuppressWarnings({"rawtypes", "unchecked"})
public class MapCodec extends Codec<Map, Json> {
   @Override
   public @Nullable Json write(Map value, Class<?> type, Type... typeArgs) throws Exception {
      Class<?> keyType = (Class<?>) typeArgs[0];
      Class<?> valueType = (Class<?>) typeArgs[1];

      Codec keyCodec = get(keyType);
      Type[] keyArgs = getTypeArgs(typeArgs[0]);

      Codec valueCodec = get(valueType);
      Type[] valueArgs = getTypeArgs(typeArgs[1]);

      return (Json) value.entrySet()
              .stream()
              .collect(new Json.Collector<Map.Entry>(
                 entry -> {
                    try {
                       return keyCodec.toString(entry.getKey(), keyType, keyArgs);
                    } catch (Exception e) {
                       throw new RuntimeException(e);
                    }
                 },
                 entry -> {
                    try {
                       return valueCodec.write(entry.getValue(), valueType, valueArgs);
                    } catch (Exception e) {
                       throw new RuntimeException(e);
                    }
                 }
              ));
   }

   @Override
   public @Nullable Map read(@NotNull Json value, Class<?> type, Type... typeArgs) throws Exception {
      Class<?> keyType = (Class<?>) typeArgs[0];
      Class<?> valueType = (Class<?>) typeArgs[1];

      Codec keyCodec = get(keyType);
      Type[] keyArgs = getTypeArgs(typeArgs[0]);

      Codec valueCodec = get(valueType);
      Type[] valueArgs = getTypeArgs(typeArgs[1]);

      return value.entrySet()
              .stream()
              .collect(new LinkedMapCollector<>(
                      entry -> {
                         try {
                            return keyCodec.fromString(entry.getKey(), keyType, keyArgs);
                         } catch (Exception e) {
                            throw new RuntimeException(e);
                         }
                      },
                      entry -> {
                         try {
                            return valueCodec.read(entry.getValue(), valueType, valueArgs);
                         } catch (Exception e) {
                            throw new RuntimeException(e);
                         }
                      }
              ));
   }

   @Override
   public Map fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      throw new UnsupportedOperationException();
   }

   @Override
   public String toString(Map value, Class<?> type, Type... typeArgs) throws Exception {
      throw new UnsupportedOperationException();
   }
}
