package com.busted_moments.core.json;

import com.busted_moments.core.UnexpectedException;
import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.util.Priority;
import com.busted_moments.core.util.Reflection;
import com.essentuan.acf.util.Primitives;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.busted_moments.client.FuyMain.CLASS_SCANNER;

@SuppressWarnings({"rawtypes"})
public abstract class AbstractCodec<T, Out> extends Annotated {
   private static final Map<Class<?>, AbstractCodec> LOADED = new LinkedHashMap<>();

   private final Class<T> clazz;
   private final Priority priority;

   protected AbstractCodec() {
      super(Required(Definition.class));

      Definition annotation = getAnnotation(Definition.class);

      clazz = (Class<T>) annotation.value();
      priority = annotation.priority();
   }

   public @Nullable Out write(BaseModel.Entry entry) throws Exception {
      return write(entry.get(), entry.getField().getType(), new Annotations(entry.getField()), getTypeArgs(entry.getField().getGenericType()));
   }

   public abstract @Nullable Out write(T value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception;

   public @Nullable T read(@NotNull Out value, BaseModel.Entry entry) throws Exception {
      return read(value, entry.getField().getType(), new Annotations(entry.getField()), getTypeArgs(entry.getField().getGenericType()));
   }

   public abstract @Nullable T read(@NotNull Out value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception;

   public abstract T fromString(String string, Class<?> type, Type... typeArgs) throws Exception;

   public abstract String toString(T value, Class<?> type, Type... typeArgs) throws Exception;

   public Class<T> getType() {
      return clazz;
   }

   public Priority getPriority() {
      return priority;
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.TYPE)
   public @interface Definition {
      Class<?> value();

      Priority priority() default Priority.LOW;
   }

   public static AbstractCodec get(Class<?> type) {
      if (LOADED.isEmpty())
         load();

      type = Primitives.wrap(type);

      if (LOADED.containsKey(type)) {
         return LOADED.get(type);
      }

      for (AbstractCodec codec : LOADED.values()) {
         if (codec.getType().equals(type) || type.isAssignableFrom(codec.getType()) || codec.getType().isAssignableFrom(type)) {
            return codec;
         }
      }

      throw new RuntimeException();
   }

   protected static Class<?> getClass(Type type) {
      if (type instanceof Class<?> clazz)
         return clazz;
      else if (type instanceof ParameterizedType parameter)
         return getClass(parameter.getRawType());
      else
         throw new IllegalStateException("Unexpected value: " + type);
   }

   public static Type[] getTypeArgs(Type type) {
      return type instanceof ParameterizedType parameterizedType ? parameterizedType.getActualTypeArguments() : new Type[0];
   }

   private static AbstractCodec create(Class<? extends AbstractCodec> codec) {
      try {
         return codec.getDeclaredConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
         throw UnexpectedException.propagate(e);
      }
   }

   private static void load() {
      CLASS_SCANNER.getSubTypesOf(AbstractCodec.class).stream()
              .filter(clazz -> !Reflection.isAbstract(clazz))
              .map(AbstractCodec::create).sorted(Comparator.comparing(AbstractCodec::getPriority))
              .forEach(codec -> LOADED.put(codec.getType(), codec));

   }
}
