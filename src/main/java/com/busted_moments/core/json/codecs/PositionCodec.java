package com.busted_moments.core.json.codecs;

import com.busted_moments.core.json.Codec;
import com.busted_moments.core.json.Json;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@Codec.Definition(Position.class)
public class PositionCodec extends Codec<Position, Json> {
   @Override
   public @Nullable Json write(Position value, Class<?> type, Type... typeArgs) throws Exception {
      return Json.of("x", value.x())
              .set("y", value.y())
              .set("z", value.z());
   }

   @Override
   public @Nullable Position read(@NotNull Json value, Class<?> type, Type... typeArgs) throws Exception {
      return new Vec3(
              value.getDouble("x"),
              value.getDouble("y"),
              value.getDouble("z")
      );
   }

   @Override
   public Position fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      return read(Json.parse(string), type, typeArgs);
   }

   @Override
   public String toString(Position value, Class<?> type, Type... typeArgs) throws Exception {
      Json result = write(value, type, typeArgs);

      return result == null ? null : result.toString();
   }
}
