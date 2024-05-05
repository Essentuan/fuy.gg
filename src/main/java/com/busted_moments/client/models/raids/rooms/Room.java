package com.busted_moments.client.models.raids.rooms;

import com.busted_moments.client.models.raids.rooms.types.InsideType;
import com.busted_moments.client.models.raids.rooms.types.RadiusType;
import com.busted_moments.client.models.raids.rooms.types.SubtitleType;
import com.busted_moments.client.models.raids.rooms.types.TitleType;
import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.events.EventListener;
import com.busted_moments.core.json.AbstractCodec;
import com.busted_moments.core.json.Annotations;
import com.busted_moments.core.json.BaseModel;
import com.busted_moments.core.json.Json;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.toml.Toml;
import com.busted_moments.core.util.Priority;
import com.busted_moments.core.util.Reflection;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.busted_moments.client.Client.CLASS_SCANNER;

public abstract class Room extends BaseModel implements EventListener {
   @Key String title = null;

   @Key
   @Null
   Date start = null;
   @Key
   @Null
   Date end = null;

   Consumer<Room> onComplete;

   protected Room(String title) {
      this.title = title;
   }

   public String title() {
      return title;
   }

   public void start() {
      this.start = new Date();
      REGISTER_EVENTS();
   }

   public void complete() {
      if (end != null) return;
      UNREGISTER_EVENTS();

      end = new Date();
      onComplete.accept(this);
   }

   public void onComplete(Consumer<Room> consumer) {
      this.onComplete = consumer;
   }

   public @org.jetbrains.annotations.Nullable Date getStart() {
      return start;
   }

   public @org.jetbrains.annotations.Nullable Date getEnd() {
      return end;
   }

   public Optional<Duration> duration() {
      if (start == null) return Optional.empty();
      else if (end == null) return Optional.of(Duration.of(start, new Date()));

      return Optional.of(Duration.of(start, end));
   }

   public static RadiusType.Builder radius(String title) {
      return new RadiusType.Builder(title);
   }

   public static InsideType.Builder inside(String title) {
      return new InsideType.Builder(title);
   }

   public static TitleType.Builder title(String title) {
      return new TitleType.Builder(title);
   }

   public static SubtitleType.Builder subtitle(String title) {
      return new SubtitleType.Builder(title);
   }

   public static Builder label(String label, ChatFormatting... formattings) {
      return new Label(ChatUtil.with(formattings).append(label).toString());
   }

   public static Builder empty() {
      return label("");
   }

   private Room() {
   }

   @Override
   protected void onSave(Json json) {
      json.set("type", getClass().getSimpleName());
   }

   private static Map<String, Class<? extends Room>> TYPES = CLASS_SCANNER.getSubTypesOf(Room.class)
           .stream()
           .filter(Predicate.not(Reflection::isAbstract))
           .collect(Collectors.toUnmodifiableMap(
                   Class::getSimpleName,
                   Function.identity()
           ));

   @AbstractCodec.Definition(value = Room.class, priority = Priority.HIGH)
   public static class Codec extends AbstractCodec<Room, Map<String, ?>> {
      @Override
      public @Nullable Map<String, ?> write(Room value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
         return value.toJson();
      }

      @Override
      public @Nullable Room read(@NotNull Map<String, ?> value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
         Class<? extends Room> clazz;
         Json data;

         if (value instanceof Toml toml) {
            clazz = TYPES.get(toml.getString("type"));
            data = Json.of(toml);
         } else if (value instanceof Json json) {
            clazz = TYPES.get(json.getString("type"));
            data = json;
         } else throw new RuntimeException();

         return data.wrap(clazz);
      }

      @Override
      public Room fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
         return read(Json.parse(string), type, Annotations.empty(), typeArgs);
      }

      @Override
      public String toString(Room value, Class<?> type, Type... typeArgs) throws Exception {
         var res = write(value, type, Annotations.empty(), typeArgs);
         return res == null ? null : res.toString();
      }
   }

   public static class Label extends Room implements Builder {
      public Label(String title) {
         super(title);
      }

      @Override
      public void start() {
         onComplete.accept(this);
      }


      @Override
      public Room build() {
         return this;
      }

      public Label() {
         super(null);
      }
   }

   public interface Builder {
      Room build();
   }
}
