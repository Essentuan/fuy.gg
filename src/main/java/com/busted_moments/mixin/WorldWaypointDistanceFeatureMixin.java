package com.busted_moments.mixin;

import com.busted_moments.client.framework.marker.Marker;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.features.map.WorldWaypointDistanceFeature;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.RenderLevelEvent;
import com.wynntils.models.marker.type.MarkerInfo;
import com.wynntils.services.map.pois.WaypointPoi;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.type.TextShadow;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(value = WorldWaypointDistanceFeature.class, remap = false)
public abstract class WorldWaypointDistanceFeatureMixin implements Marker.Context {
   @Unique
   private final List<Marker.RenderedInfo> markers = new ArrayList<>();

   @Shadow @Final public Config<Integer> maxWaypointTextDistance;

   @Unique
   private PoseStack poseStack;
   @Unique
   private MultiBufferSource.BufferSource buffers;
   @Unique
   private DeltaTracker deltaTracker;
   @Unique
   private Window window;

   @Shadow protected abstract Vec2 getBoundingIntersectPoint(Vec3 position, Window window);

   @Shadow @Final public Config<Float> scale;

   @Shadow @Final public Config<Float> backgroundOpacity;

   @Shadow @Final public Config<TextShadow> textShadow;

   @Shadow @Final private static WaypointPoi DUMMY_WAYPOINT;

   @Shadow protected abstract Vec3 worldToScreen(Vector3f delta, Matrix4f projection);

   @Redirect(
           method = "onRenderLevelPost",
           at = @At(
                   value = "INVOKE",
                   target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
           )
   )
   private boolean add(List<?> instance, Object e) {
      return false;
   }

   @Inject(
           method = "onRenderLevelPost",
           at = @At("HEAD")
   )
   private void onRenderLevelPost(RenderLevelEvent.Post event, CallbackInfo ci) {
      markers.clear();
   }

   @Inject(
           method = "onRenderLevelPost",
           at = @At(
                   value = "INVOKE",
                   target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
           ),
           locals = LocalCapture.CAPTURE_FAILHARD
   )
   private void onMarkerAdd(
           RenderLevelEvent.Post event,
           CallbackInfo ci,
           List<MarkerInfo> markers,
           Iterator<MarkerInfo> var3,
           MarkerInfo marker,
           Location location,
           Matrix4f projection,
           Camera camera,
           Position cameraPos,
           Vector3f xp,
           Vector3f yp,
           Quaternionf xRotation,
           Quaternionf yRotation,
           float dx,
           float dy,
           float dz,
           double squaredDistance,
           double distance,
           int maxDistance,
           String distanceText
   ) {
      this.markers.add(
              new Marker.RenderedInfo(
                      distance,
                      distanceText,
                      marker,
                      this.worldToScreen(
                              new Vector3f(
                                      dx,
                                      dy,
                                      dz
                              ),
                              projection
                      )
              )
      );
   }

   @Inject(
           method = "onRenderGuiPost",
           at = @At("HEAD"),
           cancellable = true
   )
   private void onRenderGuiPost(RenderEvent.Post event, CallbackInfo ci) {
      poseStack = event.getPoseStack();
      buffers = event.getGuiGraphics().bufferSource();
      deltaTracker = event.getDeltaTracker();
      window = event.getWindow();

      Marker.Companion.render(this);

      ci.cancel();
   }

   @NotNull
   @Override
   public List<Marker.RenderedInfo> getBeacons() {
      return markers;
   }

   @Override
   public int getMaxWaypointDistance() {
      return maxWaypointTextDistance.get();
   }

   @Override
   public float getScale() {
      return scale.get();
   }

   @Override
   public float getOpacity() {
      return backgroundOpacity.get();
   }

   @NotNull
   @Override
   public TextShadow getTextStyle() {
      return textShadow.get();
   }

   @NotNull
   @Override
   public WaypointPoi getDummy() {
      return DUMMY_WAYPOINT;
   }

   @Override
   public Vec2 getIntersection(@NotNull Vec3 position, @NotNull Window window) {
      return getBoundingIntersectPoint(position, window);
   }

   @NotNull
   @Override
   public PoseStack getPose() {
      return poseStack;
   }

   @NotNull
   @Override
   public MultiBufferSource.BufferSource getBuffer() {
      return buffers;
   }

   @NotNull
   @Override
   public DeltaTracker getDeltaTracker() {
      return deltaTracker;
   }

   @NotNull
   @Override
   public Window getWindow() {
      return window;
   }
}
