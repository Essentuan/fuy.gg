package com.busted_moments.core.config;

import com.busted_moments.client.FuyMain;
import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.config.entry.ConfigEntry;
import com.busted_moments.core.util.Priority;
import com.busted_moments.core.util.Reflection;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.busted_moments.client.FuyMain.CONFIG;

public abstract class Config extends Annotated implements Buildable<Void, Void> {
   private final java.util.List<ConfigEntry<?>> entries = new ArrayList<>();

   private final Multimap<Field, Consumer<Object>> listeners = MultimapBuilder.hashKeys().arrayListValues().build();

   protected static final Logger LOGGER = FuyMain.LOGGER;

   @SuppressWarnings("ExtractMethodRecommender")
   public Config(Validator<?>... validators) {
      super(new Validator<?>[]{
              Annotated.Placeholder(Category.class),
              Annotated.Placeholder(Section.class)
      }, validators);

      Class<?> clazz = this.getClass();

      List<Class<?>> classes = new ArrayList<>();

      while (clazz != Object.class) {
         classes.add(0, clazz);

         clazz = clazz.getSuperclass();
      }

      List<Method> methods = new ArrayList<>();

      classes.forEach(config -> {
         for (Field field : config.getDeclaredFields()) {
            ConfigEntry.of(this, field, this).ifPresent(entry -> {
               if (shouldIgnore(entry)) return;

               if (entry.hasAnnotation(Category.class)) entry.setCategory(entry.getAnnotation(Category.class, Category::value));
               else if (hasAnnotation(Category.class)) entry.setCategory(getAnnotation(Category.class, Category::value));

               if (entry.hasAnnotation(Section.class)) entry.setSection(entry.getAnnotation(Section.class, Section::value));
               else if (getSection() != null && !entry.hasAnnotation(Floating.class)) entry.setSection(getSection());

               entry.setListener(obj -> listeners.get(entry.getField()).forEach(consumer -> consumer.accept(obj)));

               entries.add(entry);
            });
         }

         methods.addAll(List.of(config.getDeclaredMethods()));
      });

      methods.forEach(method -> {
         if (method.isAnnotationPresent(Listener.class)) {
            Object ref = Reflection.isStatic(method) ? null : this;

            method.setAccessible(true);

            Listener listener = method.getAnnotation(Listener.class);

            (listener.config() == Config.class ? getEntry(listener.field()) : getEntry(listener.config(), listener.field()))
                    .ifPresent(entry -> listeners.put(entry.getField(), obj -> {
                       try {
                          method.invoke(ref, obj);
                       } catch (IllegalAccessException | InvocationTargetException e) {
                          throw new RuntimeException(e);
                       }
                    }));
         }
      });
   }
   protected String getCategory() {
      return hasAnnotation(Category.class) ? getAnnotation(Category.class, Category::value) : null;
   }

   protected String getSection() {
      return hasAnnotation(Section.class) ? getAnnotation(Section.class, Section::value) : null;
   }

   protected boolean shouldIgnore(ConfigEntry<?> entry) {
      return false;
   }

   void setInstances() {
      for (Field field : getClass().getDeclaredFields()) {
         if (field.isAnnotationPresent(Instance.class)) {
            field.setAccessible(true);

            try {
               field.set(Reflection.isStatic(field) ? null : this, CONFIG.getConfig(field.getType()));
            } catch (IllegalAccessException e) {
               throw new RuntimeException(e);
            }
         }
      }
   }

   @Override
   public String getKey() {
      return getClass().getSimpleName().toLowerCase();
   }

   @SuppressWarnings("unchecked")
   private <T> Optional<ConfigEntry<T>> getEntry(Predicate<ConfigEntry<?>> predicate) {
      for (ConfigEntry<?> entry : getEntries()) {
         if (predicate.test(entry)) {
            return Optional.of((ConfigEntry<T>) entry);
         }
      }

      return Optional.empty();
   }

   @SuppressWarnings("unchecked")
   public <T> Optional<ConfigEntry<T>> getEntry(Field field) {
      return getEntry(entry -> entry.getField().equals(field));
   }

   public <T> Optional<ConfigEntry<T>> getEntry(Class<? extends Config> clazz, String field) {
      return getEntry(entry -> entry.getField().getDeclaringClass().equals(clazz) && entry.getField().getName().equals(field));
   }

   @SuppressWarnings("unchecked")
   public <T> Optional<ConfigEntry<T>> getEntry(String name) {
      return getEntry(entry -> entry.getField().getName().equals(name));
   }


   public Collection<ConfigEntry<?>> getEntries() {
      return entries;
   }

   @Override
   public Void build(Void obj) {
      return null;
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.FIELD})
   public @interface Value {
      String value();
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.FIELD})
   public @interface Slider {
      String value();
   }


   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.FIELD})
   public @interface Array {
      String value();
   }


   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.FIELD})
   public @interface Dropdown {
      String title();

      Class<? extends Provider<?>> options();

      interface Provider<T> {
         Iterable<T> getOptions();

         @Nullable T get(String string) throws Throwable;

         default Component getName(T value) {
            return Component.literal(value.toString());
         }
      }
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.FIELD})
   public @interface Hidden {
      String value();
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.FIELD})
   public @interface Tooltip {
      String[] value();
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.FIELD})
   public @interface Frozen {
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.FIELD})
   public @interface Alpha {
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.TYPE, ElementType.FIELD})
   public @interface Section {
      String value();

      @SuppressWarnings("ClassExplicitlyAnnotation")
      record Impl(String section) implements Category {

         @Override
         public String value() {
            return section;
         }

         @Override
         public Class<? extends Annotation> annotationType() {
            return Category.class;
         }
      }
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.TYPE, ElementType.FIELD})
   public @interface Floating {}


   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.TYPE, ElementType.FIELD})
   public @interface Category {
      String value();

      @SuppressWarnings("ClassExplicitlyAnnotation")
      record Impl(String category) implements Category {

         @Override
         public String value() {
            return category;
         }

         @Override
         public Class<? extends Annotation> annotationType() {
            return Category.class;
         }
      }
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.TYPE})
   public @interface Writer {
      Class<?> value();
      Priority priority() default Priority.LOW;
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target({ElementType.METHOD})
   public @interface Listener {
      Class<? extends Config> config() default Config.class;

      String field();
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.FIELD)
   public @interface Instance {}
}
