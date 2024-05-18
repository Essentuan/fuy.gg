package com.busted_moments.core.render;

import com.busted_moments.client.events.mc.MinecraftStartupEvent;
import com.busted_moments.core.text.TextBuilder;
import com.busted_moments.core.util.Reflection;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.map.BeaconBeamFeature;
import com.wynntils.features.map.WorldWaypointDistanceFeature;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.RenderLevelEvent;
import com.wynntils.mc.event.RenderTileLevelLastEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.marker.MarkerModel;
import com.wynntils.models.marker.type.MarkerProvider;
import com.wynntils.services.map.pois.WaypointPoi;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.CustomBeaconRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.*;

import java.lang.Math;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class BeaconRenderer {
   static final List<Beacon.Provider> MARKER_PROVIDERS = new ArrayList<>();

   static final WaypointPoi DUMMY_WAYPOINT = new WaypointPoi(() -> null, "");

   private static WorldWaypointDistanceFeature feature = null;

   static WorldWaypointDistanceFeature getFeature() {
      if (feature == null) feature = Managers.Feature.getFeatureInstance(WorldWaypointDistanceFeature.class);

      return feature;
   }

   private static BeaconBeamFeature BEACON_BEAM;
   private static CustomColor rainbow = CommonColors.WHITE;


   static CustomColor getCurrentRainbow() {
      return rainbow;
   }

   static Stream<Beacon> getBeacons() {
      return MARKER_PROVIDERS.stream()
              .filter(Beacon.Provider::isEnabled)
              .flatMap(Beacon.Provider::getBeacons);
   }

   @SubscribeEvent
   private static void onRenderLevelLast(RenderTileLevelLastEvent event) {
      List<Beacon> beacons = getBeacons().toList();
      if (beacons.isEmpty()) return;

      PoseStack poseStack = event.getPoseStack();
      MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

      for (Beacon beacon : beacons) {
         Position camera = event.getCamera().getPosition();
         Location location = beacon.location();

         double dx = location.x - camera.x();
         double dy = location.y - camera.y();
         double dz = location.z - camera.z();

         double distance = MathUtils.magnitude(dx, dz);
         int maxDistance = McUtils.options().renderDistance().get() * 16;

         if (distance > maxDistance) {
            double scale = maxDistance / distance;

            dx *= scale;
            dz *= scale;
         }

         float alpha = 1f;

         if (distance <= 7) {
            alpha = MathUtils.clamp(MathUtils.map((float) distance, 2f, 7f, 0f, 1f), 0f, 1f);
         }

         poseStack.pushPose();
         poseStack.translate(dx, dy, dz);

         CustomColor color = beacon.beaconColor() == CustomColor.NONE ? CommonColors.RED : beacon.beaconColor();

         float[] colorArray;
         if (color == CommonColors.RAINBOW) {
            colorArray = getCurrentRainbow().asFloatArray();
         } else {
            colorArray = color.asFloatArray();
         }

         CustomBeaconRenderer.renderBeaconBeam(
                 poseStack,
                 bufferSource,
                 net.minecraft.client.renderer.blockentity.BeaconRenderer.BEAM_LOCATION,
                 event.getPartialTick(),
                 1f,
                 McUtils.player().level().getGameTime(),
                 0,
                 1024,
                 colorArray,
                 alpha,
                 0.166f,
                 0.33f);

         poseStack.popPose();
      }

      bufferSource.endLastBatch();
   }

   @SubscribeEvent
   private static void onRenderLevelPost(RenderLevelEvent.Post event) {
      RENDERED_BEACONS.clear();

      List<Beacon> beacons = getBeacons().toList();
      if (beacons.isEmpty()) return;

      for (Beacon beacon : beacons) {
         Location location = beacon.location();
         Matrix4f projection = new Matrix4f(event.getProjectionMatrix());
         Camera camera = event.getCamera();
         Position cameraPos = camera.getPosition();

         // apply camera rotation
         Vector3f xp = new Vector3f(1, 0, 0);
         Vector3f yp = new Vector3f(0, 1, 0);
         Quaternionf xRotation = new Quaternionf().rotationAxis((float) Math.toRadians(camera.getXRot()), xp);
         Quaternionf yRotation = new Quaternionf().rotationAxis((float) Math.toRadians(camera.getYRot() + 180f), yp);
         projection.mul(new Matrix4f().rotation(xRotation));
         projection.mul(new Matrix4f().rotation(yRotation));

         // offset to put text to the center of the block
         float dx = (float) (location.x + 0.5 - cameraPos.x());
         float dy = (float) (location.y + 0.5 - cameraPos.y());
         float dz = (float) (location.z + 0.5 - cameraPos.z());

         if (location.y <= 0 || location.y > 255) {
            dy = 0;
         }

         double squaredDistance = dx * dx + dy * dy + dz * dz;

         double distance = Math.sqrt(squaredDistance);
         int maxDistance = McUtils.options().renderDistance().get() * 16;


         TextBuilder builder = beacon.label() == null ? TextBuilder.empty() : TextBuilder.of(beacon.label()).line();
         builder.append(Math.round((float) distance) + "m", ChatFormatting.RESET);

         // move the position to avoid ndc z leak past 1
         if (distance > maxDistance) {
            double posScale = maxDistance / distance;
            dx *= (float) posScale;
            dz *= (float) posScale;
         }

         RENDERED_BEACONS.add(new RenderedBeacon(distance, beacon, builder.build(), worldToScreen(new Vector3f(dx, dy, dz), projection)));
      }
   }

   private static final List<RenderedBeacon> RENDERED_BEACONS = new ArrayList<>();

   @SubscribeEvent
   private static void onRenderGuiPost(RenderEvent.Post event) {
      for (RenderedBeacon rendered : RENDERED_BEACONS) {
         if (getFeature().maxWaypointTextDistance.get() != 0 && getFeature().maxWaypointTextDistance.get() < rendered.distance)
            return;

         float backgroundWidth = FontRenderer.getWidth(rendered.label, 0);
         float backgroundHeight = FontRenderer.getHeight(rendered.label, 0);

         float displayPositionX;
         float displayPositionY;

         Vec2 intersectPoint = getBoundingIntersectPoint(rendered.screenCoordinates, event.getWindow());
         Texture icon = rendered.beacon.texture();
         float[] color = rendered.beacon.textureColor().asFloatArray();
         RenderSystem.setShaderColor(color[0], color[1], color[2], 1f);

         // The set waypoint is visible on the screen, so we render the icon + distance
         if (intersectPoint == null) {
            displayPositionX = (float) rendered.screenCoordinates.x;
            displayPositionY = (float) rendered.screenCoordinates.y;

            RenderUtils.drawScalingTexturedRect(
                    event.getPoseStack(),
                    icon.resource(),
                    displayPositionX - icon.width() / 2F,
                    displayPositionY - icon.height() - backgroundHeight / 2 - 3f,
                    0,
                    icon.width(),
                    icon.height(),
                    icon.width(),
                    icon.height());
            RenderSystem.setShaderColor(1, 1, 1, 1);

            float x = displayPositionX - (backgroundWidth / 2);
            float y = displayPositionY - (backgroundHeight / 2);

            RenderUtils.drawRect(
                    event.getPoseStack(),
                    CommonColors.BLACK.withAlpha(getFeature().backgroundOpacity.get()),
                    x - 2,
                    y - 2,
                    0,
                    backgroundWidth + 3,
                    backgroundHeight + 2
            );

            var buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

            Renderer.text(
                    event.getPoseStack(),
                    buffer,
                    rendered.label(),
                    x,
                    x + backgroundWidth,
                    y,
                    y + backgroundHeight,
                    0,
                    rendered.beacon.textColor(),
                    HorizontalAlignment.CENTER,
                    VerticalAlignment.TOP,
                    rendered.beacon.style(),
                    1
            );

            buffer.endBatch();
         } else {
            displayPositionX = intersectPoint.x;
            displayPositionY = intersectPoint.y;

            RenderUtils.drawScalingTexturedRect(
                    event.getPoseStack(),
                    icon.resource(),
                    displayPositionX - icon.width() / 2F,
                    displayPositionY - icon.height() / 2F,
                    0,
                    icon.width(),
                    icon.height(),
                    icon.width(),
                    icon.height());
            RenderSystem.setShaderColor(1, 1, 1, 1);

            // pointer position is determined by finding the point on circle centered around displayPosition
            double angle = Math.toDegrees(StrictMath.atan2(
                    displayPositionY - event.getWindow().getGuiScaledHeight() / 2F,
                    displayPositionX - event.getWindow().getGuiScaledWidth() / 2F))
                    + 90f;
            float radius = icon.width() / 2F + 8f;
            float pointerDisplayPositionX =
                    (float) (displayPositionX + radius * StrictMath.cos((angle - 90) * 3 / 180));
            float pointerDisplayPositionY =
                    (float) (displayPositionY + radius * StrictMath.sin((angle - 90) * 3 / 180));

            // apply rotation
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            poseStack.translate(pointerDisplayPositionX, pointerDisplayPositionY, 0);
            poseStack.mulPose(new Quaternionf().rotationXYZ(0, 0, (float) Math.toRadians(angle)));
            poseStack.translate(-pointerDisplayPositionX, -pointerDisplayPositionY, 0);

            MultiBufferSource.BufferSource bufferSource =
                    MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            DUMMY_WAYPOINT
                    .getPointerPoi()
                    .renderAt(poseStack, bufferSource, pointerDisplayPositionX, pointerDisplayPositionY, false, 1, 1, 50);
            bufferSource.endBatch();
            poseStack.popPose();
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   private static void onTick(TickEvent event) {

      if (BEACON_BEAM == null)
         BEACON_BEAM = Managers.Feature.getFeatureInstance(BeaconBeamFeature.class);

      rainbow = Reflection.get("currentRainbowColor", BeaconBeamFeature.class, BEACON_BEAM);
   }

   static Vec3 worldToScreen(Vector3f delta, Matrix4f projection) {
      Vector4f clipCoords = new Vector4f(delta.x(), delta.y(), delta.z(), 1.0f);
      projection.transform(clipCoords);

      // stands for Normalized Device Coordinates
      Vector3d ndc = new Vector3d(
              clipCoords.x() / clipCoords.w(), clipCoords.y() / clipCoords.w(), clipCoords.z() / clipCoords.w());

      Window window = McUtils.window();

      return new Vec3(
              (float) ((ndc.x + 1.0f) / 2.0f) * window.getGuiScaledWidth(),
              (float) ((1.0f - ndc.y) / 2.0f) * window.getGuiScaledHeight(),
              (float) ndc.z);
   }

   // draw a line from screen center to the target's screenspace coordinate
   // and find the intersect point on one of the screen's bounding
   static Vec2 getBoundingIntersectPoint(Vec3 position, Window window) {
      if (isInBound(position, window)) return null;
      Vec3 centerPoint = new Vec3(window.getGuiScaledWidth() / 2F, window.getGuiScaledHeight() / 2F, 0);

      // minecraft's origin point is top left corner
      // so positive Y is at the screen bottom
      float minX = (float) -(centerPoint.x - getFeature().horizontalBoundingDistance.get());
      float maxX = (float) centerPoint.x - getFeature().horizontalBoundingDistance.get();
      float minY = (float) -(centerPoint.y - getFeature().topBoundingDistance.get());
      float maxY = (float) centerPoint.y - getFeature().bottomBoundingDistance.get();

      // drag the origin point to center since indicator's screenspace position / rotation is in relation to it
      Vec3 centerRelativePosition = position.subtract(centerPoint);

      // invert xy axis if target is behind camera
      if (centerRelativePosition.z > 1) {
         centerRelativePosition = centerRelativePosition.multiply(-1, -1, 1);
      }

      // since center point is now the origin point, atan2 is used to get the angle, and angle is used to get the
      // line's slope
      double angle = StrictMath.atan2(centerRelativePosition.y, centerRelativePosition.x);
      double m = StrictMath.tan(angle);

      // trying to solve (y2 - y1) = m (x2 - x1) + c here
      // starting from origin point/screen center (x1, y1), end at one of the screen bounding
      // (y2 - y1) is the equivalent of the y position
      // (x2 - x1) is the equivalent of the x position
      // c is the line's y-intercept, but line pass through origin, so this will be 0
      // finalize to y = mx, or x = y/m

      if (centerRelativePosition.x > 0) {
         centerRelativePosition = new Vec3(maxX, maxX * m, 0);
      } else {
         centerRelativePosition = new Vec3(minX, minX * m, 0);
      }

      if (centerRelativePosition.y > maxY) {
         centerRelativePosition = new Vec3(maxY / m, maxY, 0);
      } else if (centerRelativePosition.y < minY) {
         centerRelativePosition = new Vec3(minY / m, minY, 0);
      }

      // bring the position back to normal screen space (top left origin point)
      return new Vec2(
              (float) (centerRelativePosition.x + centerPoint.x), (float) (centerRelativePosition.y + centerPoint.y));
   }

   static boolean isInBound(Position position, Window window) {
      return position.x() > 0
              && position.x() < window.getGuiScaledWidth()
              && position.y() > 0
              && position.y() < window.getGuiScaledHeight()
              && position.z() < 1;
   }

   static boolean HAS_PROCESSED = false;

   static synchronized void register(Beacon.Provider provider) {
      if (HAS_PROCESSED) Models.Marker.registerMarkerProvider(new PoiProvider(provider));

      MARKER_PROVIDERS.add(provider);
   }

   @SubscribeEvent
   @SuppressWarnings({"rawtypes", "unchecked"})
   private static void onGameStart(MinecraftStartupEvent event) throws NoSuchFieldException, IllegalAccessException {
      Field field = MarkerModel.class.getDeclaredField("markerProviders");
      field.setAccessible(true);

      List<MarkerProvider> providers = (List<MarkerProvider>) field.get(Models.Marker);

      providers.addAll(0, MARKER_PROVIDERS.stream()
              .map(PoiProvider::new)
              .toList());

      HAS_PROCESSED = true;
   }

   private record RenderedBeacon(double distance, Beacon beacon, StyledText label, Vec3 screenCoordinates) {
   }
}
