package com.busted_moments.core.config.entry;

import com.busted_moments.core.config.Buildable;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.config.Config.Dropdown.Provider;
import com.busted_moments.core.render.FontRenderer;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("rawtypes")
public class DropdownEntry extends ConfigEntry<Object> {
   private final Provider provider;

   public DropdownEntry(Config.Dropdown config, Object ref, Field field, Buildable<?, ?> parent) {
      super(Component.literal(config.title()), ref, field, parent);

      try {
         var constructor = config.options().getConstructor();
         constructor.setAccessible(true);

         this.provider = new Provider(constructor.newInstance());
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   @SuppressWarnings("unchecked")
   public AbstractConfigListEntry<?> build(ConfigEntryBuilder builder) {
      float maxWidth = Math.min((StreamSupport.stream(provider.getOptions().spliterator(), false))
              .map(obj -> FontRenderer.getWidth(provider.getName(obj)))
              .max(Float::compare)
              .orElse(300F), 300F);

      var res = builder.startDropdownMenu(getTitle(),
              DropdownMenuBuilder.TopCellElementBuilder.of(provider.entry(get()), string -> {
                 try {
                    return provider.get(string);
                 } catch (Throwable t) {
                    return null;
                 }
              }, provider::getName),
              DropdownMenuBuilder.CellCreatorBuilder.ofWidth((int) maxWidth, provider::getName)
      );

      res.setSelections(provider.getOptions());

      res.setSaveConsumer(entry -> set(entry.value));

      res.setDefaultValue(() -> provider.entry(getDefault()));

      if (getTooltip() != null) {
         res.setTooltip(getTooltip());
      }

      var element = res.build();

      element.setEditable(!hasAnnotation(Config.Frozen.class));

      return element;
   }

   private static class Entry {
      private final Object value;
      private final String representation;

      public Entry(Object value, Component representation) {
         this.value = value;
         this.representation = representation.getString();
      }

      @Override
      public boolean equals(Object object) {
         if (this == object) return true;
         if (object == null || getClass() != object.getClass()) return false;
         Entry entry = (Entry) object;
         return Objects.equals(representation, entry.representation);
      }

      @Override
      public int hashCode() {
         return Objects.hash(representation);
      }
   }


   @SuppressWarnings("unchecked")
   private record Provider(Config.Dropdown.Provider provider) implements Config.Dropdown.Provider<Entry> {
      @Override
         public Iterable<Entry> getOptions() {
            return new Iterable<>() {
               @NotNull
               @Override
               public Iterator<Entry> iterator() {
                  var iter = provider.getOptions().iterator();

                  return new Iterator<>() {
                     @Override
                     public boolean hasNext() {
                        return iter.hasNext();
                     }

                     @Override
                     public Entry next() {
                        var next = iter.next();
                        return new Entry(next, provider.getName(next));
                     }

                     @Override
                     public void remove() {
                        iter.remove();
                     }
                  };
               }
            };
         }

         @Override
         public @Nullable Entry get(String string) throws Throwable {
            var value = provider.get(string);
            if (value == null) return null;

            return new Entry(value, provider.getName(value));
         }

      @Override
      public Component getName(Entry value) {
         return provider.getName(value.value);
      }

      public Entry entry(Object value) {
         return new Entry(value, provider.getName(value));
      }
   }

   @Override
   protected AbstractFieldBuilder<Object, ?, ?> create(ConfigEntryBuilder builder) {
      return null;
   }
}
