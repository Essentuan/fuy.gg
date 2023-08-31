package com.busted_moments.core.config;

import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.config.entry.ConfigEntry;
import com.busted_moments.core.util.Priority;
import com.busted_moments.core.util.Reflection;
import com.essentuan.acf.util.Primitives;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.busted_moments.client.FuyMain.CLASS_SCANNER;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class Writer<T, Out> extends Annotated {
   private static final Map<Class<?>, Writer> LOADED = new LinkedHashMap<>();

   private final Class<T> clazz;
   private final Priority priority;

   public Writer() {
      super(Required(Config.Writer.class));

      Config.Writer annotation = getAnnotation(Config.Writer.class);

      clazz = (Class<T>) annotation.value();
      priority = annotation.priority();
   }

   public @Nullable Out write(ConfigEntry<T> entry) throws Exception {
      return this.write(entry.get(), entry.getType(), getTypeArgs(entry.getField().getGenericType()));
   }

   public abstract @Nullable Out write(T value, Class<?> type, Type... typeArgs) throws Exception;

   public @Nullable T read(@NotNull Out value, ConfigEntry entry) throws Exception{
      return read(value, entry.getType(), getTypeArgs(entry.getField().getGenericType()));
   }

   public abstract @Nullable T read(@NotNull Out value, Class<?> type, Type... typeArgs) throws Exception;

   public abstract T fromString(String string, Class<?> type, Type... typeArgs) throws Exception;

   public abstract String toString(T value, Class<?> type, Type... typeArgs) throws Exception;

   public Class<T> getType() {
      return clazz;
   }

   public Priority getPriority() {
      return priority;
   }

   public static Writer get(Class<?> type) {
      if (LOADED.isEmpty()) load();

      type = Primitives.wrap(type);

      if (LOADED.containsKey(type)) {
         return LOADED.get(type);
      }

      for (Writer codec : LOADED.values()) {
         if (codec.getType().equals(type) || type.isAssignableFrom(codec.getType()) || codec.getType().isAssignableFrom(type)) {
            return codec;
         }
      }

      throw new RuntimeException();
   }

   protected static Type[] getTypeArgs(Type type) {
      return type instanceof ParameterizedType parameterizedType ? parameterizedType.getActualTypeArguments() : new Type[0];
   }

   private static Writer create(Class<? extends Writer> codec) {
      try {
         return codec.getDeclaredConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
         throw new RuntimeException(e);
      }
   }

   private static void load() {
      CLASS_SCANNER.getSubTypesOf(Writer.class).stream()
              .filter(clazz -> !Reflection.isAbstract(clazz))
              .map(Writer::create).sorted(Comparator.comparing(Writer::getPriority))
              .forEach(codec -> LOADED.put(codec.getType(), codec));
   }
}