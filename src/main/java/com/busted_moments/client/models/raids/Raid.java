package com.busted_moments.client.models.raids;

import com.busted_moments.client.models.raids.events.RaidEvent;
import com.busted_moments.client.models.raids.events.RoomEvent;
import com.busted_moments.client.models.raids.rooms.Room;
import com.busted_moments.client.util.PlayerUtil;
import com.busted_moments.core.events.EventListener;
import com.busted_moments.core.json.template.JsonTemplate;
import com.busted_moments.core.text.TextBuilder;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.TimeUnit;
import com.wynntils.mc.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static net.minecraft.ChatFormatting.*;

public class Raid extends JsonTemplate implements EventListener {
   @Entry
   private RaidType type;

   @Entry
   private List<Room> rooms;

   private int current = -1;

   @Entry
   private Date start;
   @Entry
   private Date end;

   public Raid(RaidType type) {
      this.type = type;

      this.rooms = Stream.of(type.rooms())
              .map(Room.Builder::build)
              .peek(r-> r.onComplete(room -> {
                  new RoomEvent.Complete(this, room).post();
                  next();
              }))
              .toList();
   }

   public RaidType type() {
      return type;
   }

   public Optional<Duration> duration() {
      if (start == null) return Optional.empty();
      else if (end == null) return Optional.of(Duration.of(start, new Date()));

      return Optional.of(Duration.of(start, end));
   }

   public Room room() {
      if (inProgress())
         return rooms.get(current);
      else return null;
   }

   public List<Room> listRooms() {
      return rooms;
   }

   public boolean inProgress() {
      return current != -1;
   }

   public boolean completed() {
      return end != null;
   }

   public void start() {
      if (start != null) return;

      start = new Date();
      REGISTER_EVENTS();

      new RaidEvent.Start(this).post();

      next();
   }

   private void complete() {
      if (end != null) return;

      end = new Date();
      UNREGISTER_EVENTS();

      current = -1;

      new RaidEvent.Complete(this).post();
   }

   private void fail() {
      if (end != null) return;

      end = new Date();
      UNREGISTER_EVENTS();

      int room = current;
      current = -1;

      rooms.get(room).complete();

      new RaidEvent.Fail(this).post();
   }

   @SubscribeEvent
   public void onTick(TickEvent event) {
      if (inProgress() && PlayerUtil.isNear(type().failPos(), 20))
         fail();
   }

   private void next() {
      if (start == null || end != null) return;
      current++;

      if (rooms.size() <= current) {
         complete();
         return;
      }

      room().start();

      new RoomEvent.Start(this, room()).post();
   }

   void close() {
      UNREGISTER_EVENTS();
      listRooms().forEach(EventListener::UNREGISTER_EVENTS);
   }

   public Raid() {}

   @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
   public static String format(Optional<Duration> optional) {
      if (optional.isEmpty()) return "--:--";
      Duration duration = optional.orElseThrow();

      int minutes = (int) (duration.getPart(TimeUnit.MINUTES) + (duration.getPart(TimeUnit.HOURS) * 60));
      int seconds = (int) duration.getPart(TimeUnit.SECONDS);
      int mills = (int) (duration.getPart(TimeUnit.MILLISECONDS) / 10);

      return ((minutes < 10) ? "0" + minutes : Integer.toString(minutes)) +
              ":" + ((seconds < 10) ? "0" + seconds : Integer.toString(seconds)) +
              "." + ((mills < 10) ? "0" + mills : Integer.toString(mills));
   }

   public static TextBuilder format(Raid raid) {
      return TextBuilder.of(raid.type().title(), GOLD, UNDERLINE, BOLD).line().line()
              .append(raid.listRooms(), (room, builder) -> {
                 if (room instanceof Room.Label)
                    builder.append(room.title());
                 else {
                    builder.append(room.title(), LIGHT_PURPLE)
                            .append(": ", LIGHT_PURPLE)
                            .append(Raid.format(room.duration()), AQUA);
                 }
              })
              .line().line()
              .append("Total: ", LIGHT_PURPLE)
              .append(Raid.format(raid.duration()), AQUA);
   }
}
