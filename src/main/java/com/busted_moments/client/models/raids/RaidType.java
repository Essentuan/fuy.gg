package com.busted_moments.client.models.raids;

import com.busted_moments.client.models.raids.rooms.Room;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.utils.type.IterationDecision;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.text.WordUtils;

import java.text.Normalizer;
import java.util.Optional;
import java.util.regex.Pattern;

public enum RaidType {
   NEST_OF_THE_GROOTSLANGS("Nest of The Grootslangs",
           -1977, 60, -5599,
           Room.inside("Room 1").box(
                   9315, 71, 3393,
                   9248, 0, 3463
           ),
           Room.inside("Room 2").box(
                   9315, 137, 3393,
                   9248, 79, 3463
           ),
           Room.inside("Room 3").box(
                   9315, 213, 3393,
                   9248, 160, 3463
           ),
           Room.empty(),
           Room.title("Boss").string("D C")
   ),
   THE_NEXUS_OF_LIGHT("Orphion's Nexus of Light",
           -731, 100, -6405,
            Room.inside("Room 1").rectangle(
                    12008, 2125,
                    11934, 2049
            ),
            Room.inside("Room 2").rectangle(
                    12008, 1867,
                    11934, 1788
            ),
            Room.inside("Room 3").rectangle(
                    12003, 1545,
                    11928, 1463
            ),
           Room.empty(),
           Room.label("Boss", ChatFormatting.GOLD, ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
           Room.empty(),
           Room.title("Phase 1").string("The Parasite"),
           Room.title("Phase 2").string("D C")
   ),
   THE_CANYON_COLOSSUS("The Canyon Colossus",
           665, 49, -4448,
           Room.inside("Room 1").rectangle(
                   11704, 3730,
                   11867, 3828
           ),
           Room.inside("Room 2").rectangle(
                   11704, 3934,
                   11867, 4031
           ),
           Room.inside("Room 3").rectangle(
                   11704, 4149,
                   11867, 4247
           ),
           Room.empty(),
           Room.label("Boss", ChatFormatting.GOLD, ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
           Room.empty(),
           Room.inside("Phase 1").rectangle(
                   10882, 4322,
                   11043, 4490
           ),
           Room.inside("Phase 2").rectangle(
                   11221, 4140,
                   11388, 4305
           ),
           Room.subtitle("Phase 3").string("to enter the Colossus"),
           Room.empty(),
           Room.title("Puzzle").string("D C")
   ),
   THE_NAMELESS_ANOMALY("The Nameless Anomaly",
           1120, 85, -853,
           Room.inside("Room 1").rectangle(
                   24848, -23920,
                   24991, -24047
           ),
           Room.inside("Room 2").rectangle(
                   24848, -23712,
                   24959, -23823
           ),
           Room.inside("Room 3").rectangle(
                   24848, -23504,
                   24959, -23615
           ),
           Room.empty(),
           Room.title("Boss").string("D C")
   );

   private final String title;
   private final String name;

   private final Position failPos;
   private final Room.Builder[] rooms;

   RaidType(String title, double x, double y, double z, Room.Builder... rooms) {
      this.title = title;
      this.name = WordUtils.capitalize(toString().toLowerCase().replaceAll("_", " "));

      this.failPos = new Vec3(x, y, z);
      this.rooms = rooms;
   }

   public String friendlyName() {
      return name;
   }

   public String title() {
      return title;
   }

   public Position failPos() {
      return failPos;
   }

   public Room.Builder[] rooms() {
      return rooms;
   }

   public static Optional<RaidType> from(Component text) {
      StyledText normalized = normalize(StyledText.fromComponent(text));

      for (RaidType type : values())
         if (normalized.equalsString(type.title(), PartStyle.StyleType.NONE))
            return Optional.of(type);

      return Optional.empty();
   }

   private static final Pattern NORMALIZE_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

   private static StyledText normalize(StyledText text) {
      return text.getNormalized().iterate((next, changes) -> {
         String string = next.getString(null, PartStyle.StyleType.NONE);
         string = Normalizer.normalize(string, Normalizer.Form.NFD);
         string = NORMALIZE_PATTERN.matcher(string).replaceAll("");

         changes.set(0, new StyledTextPart(
                 string,
                 next.getPartStyle().getStyle(),
                 null,
                 null
         ));

         return IterationDecision.CONTINUE;
      });
   }
}
