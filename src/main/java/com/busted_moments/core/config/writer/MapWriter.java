package com.busted_moments.core.config.writer;

import com.busted_moments.core.collector.LinkedMapCollector;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.config.Writer;
import com.busted_moments.core.toml.Toml;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Map;

@Config.Writer(Map.class)
@SuppressWarnings({"rawtypes", "unchecked"})
public class MapWriter extends Writer<Map, Toml> {
   @Override
   public @Nullable Toml write(Map value, Class<?> type, Type... typeArgs) throws Exception {
      Class<?> keyType = (Class<?>) typeArgs[0];
      Class<?> valueType = (Class<?>) typeArgs[1];

      Writer keyWriter = get(keyType);
      Type[] keyArgs = getTypeArgs(typeArgs[0]);

      Writer valueWriter = get(valueType);
      Type[] valueArgs = getTypeArgs(typeArgs[1]);

      return (Toml) value.entrySet()
              .stream()
              .collect(new Toml.Collector<Map.Entry>(
                 entry -> {
                    try {
                       return keyWriter.toString(entry.getKey(), keyType, keyArgs);
                    } catch (Exception e) {
                       throw new RuntimeException(e);
                    }
                 },
                 entry -> {
                    try {
                       return valueWriter.write(entry.getValue(), valueType, valueArgs);
                    } catch (Exception e) {
                       throw new RuntimeException(e);
                    }
                 }
              ));
   }

   @Override
   public @Nullable Map read(@NotNull Toml value, Class<?> type, Type... typeArgs) throws Exception {
      Class<?> keyType = (Class<?>) typeArgs[0];
      Class<?> valueType = (Class<?>) typeArgs[1];

      Writer keyWriter = get(keyType);
      Type[] keyArgs = getTypeArgs(typeArgs[0]);

      Writer valueWriter = get(valueType);
      Type[] valueArgs = getTypeArgs(typeArgs[1]);

      return value.entrySet()
              .stream()
              .collect(new LinkedMapCollector<>(
                      entry -> {
                         try {
                            return keyWriter.fromString(entry.getKey(), keyType, keyArgs);
                         } catch (Exception e) {
                            throw new RuntimeException(e);
                         }
                      },
                      entry -> {
                         try {
                            return valueWriter.read(entry.getValue(), valueType, valueArgs);
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
