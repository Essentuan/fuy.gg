package com.busted_moments.mixin;

import com.busted_moments.client.buster.TerritoryList;
import com.busted_moments.client.features.war.wynntils.GuildMapImprovementsFeature;
import com.busted_moments.client.features.war.wynntils.Link;
import com.busted_moments.client.features.war.wynntils.RenderDetails;
import com.busted_moments.client.framework.render.helpers.Context;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.models.territories.type.GuildResourceValues;
import com.wynntils.screens.maps.AbstractMapScreen;
import com.wynntils.screens.maps.GuildMapScreen;
import com.wynntils.services.map.pois.Poi;
import com.wynntils.services.map.pois.TerritoryPoi;
import com.wynntils.services.map.type.TerritoryDefenseFilterType;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.MapRenderer;
import com.wynntils.utils.type.BoundingBox;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = GuildMapScreen.class, remap = false)
public abstract class GuildMapScreenMixin extends AbstractMapScreen implements Context {
    @Shadow
    private boolean resourceMode;

    @Shadow
    private boolean territoryDefenseFilterEnabled;
    @Shadow
    private GuildResourceValues territoryDefenseFilterLevel;
    @Shadow
    private TerritoryDefenseFilterType territoryDefenseFilterType;
    @Unique
    private PoseStack poseStack;

    @Unique
    private DeltaTracker deltaTracker;

    @Unique
    private MultiBufferSource.BufferSource bufferSource;

    @Inject(
            method = "renderPois*",
            at = @At("HEAD"),
            cancellable = true
    )
    private void renderPois(
            List<Poi> pois,
            PoseStack poseStack,
            BoundingBox textureBoundingBox,
            float poiScale,
            int mouseX,
            int mouseY,
            CallbackInfo ci
    ) {
        if (!GuildMapImprovementsFeature.INSTANCE.getEnabled() || TerritoryList.INSTANCE.isEmpty())
            return;

        hovered = null;
        this.poseStack = poseStack;

        List<Poi> filteredPois = getRenderedPois(pois, textureBoundingBox, poiScale, mouseX, mouseY);

        bufferSource = McUtils.mc().renderBuffers().bufferSource();
        deltaTracker = McUtils.mc().getDeltaTracker();

        List<RenderDetails> territories = new ArrayList<>(filteredPois.size());
        List<Link> links = new ArrayList<>(filteredPois.size());

        for (int i = filteredPois.size() - 1; i >= 0; i--) {
            Poi poi = filteredPois.get(i);

            if (poi instanceof TerritoryPoi territory) {
                var buster = TerritoryList.INSTANCE.get(territory.getName());

                if (buster == null)
                    continue;

                var details = new RenderDetails(
                        territory,
                        mapCenterX,
                        mapCenterZ,
                        centerX,
                        centerZ,
                        resourceMode,
                        zoomRenderScale,
                        buster
                );

                details.render(poseStack, bufferSource);
                territories.add(details);

                for (var connection : buster.getConnections()) {
                    var link = new Link(poi.getName(), connection, territory);

                    if (link.getFrom().equals(poi.getName()))
                        links.add(link);
                }
            } else {
                float poiRenderX = MapRenderer.getRenderX(poi, mapCenterX, centerX, zoomRenderScale);
                float poiRenderZ = MapRenderer.getRenderZ(poi, mapCenterZ, centerZ, zoomRenderScale);

                poi.renderAt(
                        poseStack,
                        bufferSource,
                        poiRenderX,
                        poiRenderZ,
                        hovered == poi,
                        poiScale,
                        zoomRenderScale,
                        zoomLevel,
                        true
                );
            }
        }

        final int filterLevel;
        final TerritoryDefenseFilterType filterType;

        if (territoryDefenseFilterEnabled) {
            filterLevel = territoryDefenseFilterLevel.getLevel();
            filterType = territoryDefenseFilterType;
        } else {
            filterLevel = -1;
            filterType = null;
        }

        for (var link : links) {
            link.render(
                    this,
                    mapCenterX,
                    mapCenterZ,
                    centerX,
                    centerZ,
                    zoomRenderScale,
                    filterLevel,
                    filterType
            );
        }

        for (var territory : territories)
            territory.renderLabel(
                    this,
                    poseStack,
                    bufferSource,
                    hovered == territory.getPoi()
            );


        bufferSource.endBatch();

        ci.cancel();
    }

    @ModifyConstant(
            method = "renderTerritoryTooltip",
            constant = @Constant(stringValue = "Territory Defences: %s")
    )
    private static String defenses(String constant) {
        if (GuildMapImprovementsFeature.INSTANCE.getEnabled())
            return "⛨ " + constant;
        else
            return constant;
    }

    @NotNull
    @Override
    public PoseStack getPose() {
        return poseStack;
    }

    @NotNull
    @Override
    public MultiBufferSource.BufferSource getBuffer() {
        return bufferSource;
    }

    @Override
    public DeltaTracker getDeltaTracker() {
        return deltaTracker;
    }

    @NotNull
    @Override
    public Window getWindow() {
        return McUtils.mc().getWindow();
    }
}