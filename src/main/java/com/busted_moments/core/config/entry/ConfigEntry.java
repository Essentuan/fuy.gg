package com.busted_moments.core.config.entry;

import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.config.Buildable;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.config.Writer;
import com.busted_moments.core.toml.Toml;
import com.busted_moments.core.util.Reflection;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class ConfigEntry<T> extends Annotated implements Buildable<ConfigEntryBuilder, AbstractConfigListEntry<?>> {
   private final Object ref;
   private final Field field;
   private Buildable<?, ?> parent;

   private Component title;
   private final Component[] tooltip;

   private final String key;

   private String section = null;
   private String category = "General";

   private T def;

   private Consumer<T> listener;

   public ConfigEntry(Component title, Object ref, Field field, Buildable<?, ?> parent, Validator<?>... validators) {
      super(field, validators, new Validator[]{
              Annotated.Placeholder(Config.Frozen.class),
              Annotated.Optional(new Config.Tooltip() {

                 @Override
                 public Class<? extends Annotation> annotationType() {
                    return Config.Tooltip.class;
                 }

                 @Override
                 public String[] value() {
                    return null;
                 }
              })
      });

      this.ref = Reflection.isStatic(field) ? null : ref;
      this.field = field;

      field.setAccessible(true);

      this.parent = parent;
      this.key = "%s.%s".formatted(ref.getClass().getSimpleName(), field.getName()).toLowerCase();
      this.title = title;

      String[] lines = getAnnotation(Config.Tooltip.class, Config.Tooltip::value);

      this.tooltip = (lines != null) ? Stream.of(lines).map(Component::literal).toArray(Component[]::new) : null;
   }

   public Component getTitle() {
      return title;
   }

   public void setTitle(Component component) {
      this.title = component;
   }

   public @NotNull String getCategory() {
      return category;
   }

   public void setCategory(@NotNull String category) {
      this.category = category;
   }


   public @Nullable String getSection() {
      return section;
   }

   public void setSection(String section) {
      this.section = section;
   }

   public @Nullable Component[] getTooltip() {
      return tooltip;
   }

   public Field getField() {
      return field;
   }

   @SuppressWarnings("unchecked")
   public Class<T> getType() {
      return (Class<T>) getField().getType();
   }

   @Override
   public String getKey() {
      return "%s.%s".formatted(getParent().getKey(), key).toLowerCase();
   }

   public T getDefault() {
      return def;
   }

   public void setDefault(T def) {
      this.def = def;
   }

   public Buildable<?, ?> getParent() {
      return parent;
   }

   public void setListener(Consumer<T> consumer) {
      this.listener = consumer;
   }

   protected <R extends AbstractFieldBuilder<?, ?, ?>> R create(BiFunction<Component, T, R> entry) {
      return entry.apply(getTitle(), get());
   }

   protected abstract AbstractFieldBuilder<T, ?, ?> create(ConfigEntryBuilder builder);

   @Override
   public AbstractConfigListEntry<?> build(ConfigEntryBuilder builder) {
      var res = create(builder);

      res.setSaveConsumer(this::set);
      res.setDefaultValue(getDefault());

      if (getTooltip() != null) {
         res.setTooltip(getTooltip());
      }

      var element = res.build();

      element.setEditable(!hasAnnotation(Config.Frozen.class));

      return element;
   }

   @SuppressWarnings("unchecked")
   public void set(Object obj) {
      try {
         field.set(ref, obj);

         listener.accept((T) obj);
      } catch (IllegalAccessException e) {
         throw new RuntimeException(e);
      }
   }

   @SuppressWarnings("unchecked")
   public T get() {
      try {
         return (T) field.get(ref);
      } catch (IllegalAccessException e) {
         throw new RuntimeException(e);
      }
   }

   @SuppressWarnings("unchecked")
   public void save(Toml toml) {
      if (get() != null) {
         try {
            toml.set(getKey(), Writer.get(getType()).write(this));
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }
   }

   @SuppressWarnings("unchecked")
   protected @Nullable T from(Toml toml) {
      try {
         return (T) Writer.get(getType()).read(toml.get(getKey()), this);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   public void load(Toml object) {
      if (!object.containsKey(getKey())) return;

      T result = from(object);

      set(result == null ? getDefault() : result);
   }

   public static Optional<ConfigEntry<?>> of(Object ref, Field field, Buildable<?, ?> parent) {
      if (field.isAnnotationPresent(Config.Value.class)) {
         return Optional.of(ValueType.create(ref, field, parent));
      } else if (field.isAnnotationPresent(Config.Array.class)) {
         return Optional.of(ListType.create(ref, field, parent));
      } else if (field.isAnnotationPresent(Config.Slider.class)) {
         return Optional.of(SliderType.create(ref, field, parent));
      } else if (field.isAnnotationPresent(Config.Dropdown.class)) {
         return Optional.of(new DropdownEntry(field.getAnnotation(Config.Dropdown.class), ref, field, parent));
      } else if (field.isAnnotationPresent(Config.Hidden.class)) {
         return Optional.of(new HiddenEntry(field.getAnnotation(Config.Hidden.class), ref, field, parent));
      }

      return Optional.empty();
   }
}
