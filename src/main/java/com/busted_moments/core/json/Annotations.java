package com.busted_moments.core.json;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Annotations implements Map<Class<? extends Annotation>, Annotation> {
   private final AnnotatedElement element;

   private final Collection<Annotation> values;
   private final Set<Class<? extends Annotation>> keys;
   private final Set<Map.Entry<Class<? extends Annotation>, Annotation>> entries;

   public Annotations(AnnotatedElement element) {
      this.element = element;
      this.values = List.of(element.getAnnotations());
      this.keys = Set.copyOf(values.stream().map(Annotation::annotationType).collect(Collectors.toSet()));
      this.entries = Set.copyOf(values.stream()
              .map(annotation -> new Entry(annotation.annotationType(), annotation))
              .collect(Collectors.toSet()));
   }

   @Override
   public int size() {
      return element.getAnnotations().length;
   }

   @Override
   public boolean isEmpty() {
      return size() == 0;
   }

   public <T extends Annotation> boolean contains(Class<T> clazz) {
      return get(clazz) != null;
   }

   @Override
   public boolean containsKey(Object key) {
      return get(key) != null;
   }

   @Override
   public boolean containsValue(Object value) {
      return values().contains(value);
   }

   public <T extends Annotation> T get(Class<T> clazz) {
      return element.getAnnotation(clazz);
   }

   public <T extends Annotation> T get(Class<T> clazz, T defaultValue) {
      T ret = element.getAnnotation(clazz);

      return ret == null ? defaultValue : ret;
   }

   @Override
   public Annotation get(Object key) {
      if (key instanceof Class<?> clazz)
         return element.getAnnotation((Class<Annotation>) clazz);

      return null;
   }

   @Nullable
   @Override
   public Annotation put(Class<? extends Annotation> key, Annotation value) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Annotation remove(Object key) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void putAll(@NotNull Map<? extends Class<? extends Annotation>, ? extends Annotation> m) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void clear() {
      throw new UnsupportedOperationException();
   }

   @NotNull
   @Override
   public Set<Class<? extends Annotation>> keySet() {
      return keys;
   }

   @NotNull
   @Override
   public Collection<Annotation> values() {
      return values;
   }

   @NotNull
   @Override
   public Set<Map.Entry<Class<? extends Annotation>, Annotation>> entrySet() {
      return entries;
   }

   private record Entry(Class<? extends Annotation> key, Annotation value) implements Map.Entry<Class<? extends Annotation>, Annotation> {

      @Override
      public Class<? extends Annotation> getKey() {
         return key;
      }

      @Override
      public Annotation getValue() {
         return value;
      }

      @Override
      public Annotation setValue(Annotation value) {
         throw new UnsupportedOperationException();
      }
   }

   private static final Annotations EMPTY = new Annotations(new AnnotatedElement() {
      @Override
      public <T extends Annotation> T getAnnotation(@NotNull Class<T> annotationClass) {
         return null;
      }

      @Override
      public Annotation[] getAnnotations() {
         return new Annotation[0];
      }

      @Override
      public Annotation[] getDeclaredAnnotations() {
         return new Annotation[0];
      }
   });

   public static Annotations empty() {
      return EMPTY;
   }
}
