// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.logic;

import com.google.common.collect.Maps;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.RenderSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.font.Font;
import org.terasology.engine.rendering.assets.font.FontMeshBuilder;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.cameras.Camera;
import org.terasology.engine.world.WorldProvider;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.nui.Color;
import org.terasology.nui.HorizontalAlign;

import java.util.Arrays;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

@RegisterSystem(RegisterMode.CLIENT)
public class FloatingTextRenderer extends BaseComponentSystem implements RenderSystem {
    private static final Logger logger = LoggerFactory.getLogger(FloatingTextRenderer.class);

    private static final int PIXEL_PER_METER = 250;

    private static final float METER_PER_PIXEL = 1.0f / PIXEL_PER_METER;

    @In
    private EntityManager entityManager;

    @In
    private AssetManager assetManager;

    @In
    private WorldProvider worldProvider;

    @In
    private Camera camera;

    private Font font;
    private Material underlineMaterial;
    private Map<EntityRef, Map<Material, Mesh>> entityMeshCache = Maps.newHashMap();

    @Override
    public void initialise() {
        this.font = assetManager.getAsset("engine:NotoSans-Regular-Large", Font.class).get();
        this.underlineMaterial = assetManager.getAsset("engine:UIUnderline", Material.class).get();
    }

    private void render(Iterable<EntityRef> floatingTextEntities) {
        Vector3fc cameraPosition = camera.getPosition();

        Matrix4f model = new Matrix4f();
        Matrix4f modelView = new Matrix4f();
        Vector3f worldPos = new Vector3f();
        for (EntityRef entity : floatingTextEntities) {
            FloatingTextComponent floatingText = entity.getComponent(FloatingTextComponent.class);
            LocationComponent location = entity.getComponent(LocationComponent.class);

            if (location == null) {
                logger.warn("location component is not defined can't render text: {}", floatingText.text); //NOPMD
                continue;
            }

            location.getWorldPosition(worldPos);
            if (!worldProvider.isBlockRelevant(worldPos) || !worldPos.isFinite()) {
                continue;
            }

            String[] linesOfText = floatingText.text.split("\n");
            Color baseColor = floatingText.textColor;
            Color shadowColor = floatingText.textShadowColor;
            boolean underline = false;

            int textWidth = 0;
            for (String singleLine : linesOfText) {
                if (font.getWidth(singleLine) > textWidth) {
                    textWidth = font.getWidth(singleLine);
                }
            }

            FontMeshBuilder meshBuilder = new FontMeshBuilder(underlineMaterial);

            Map<Material, Mesh> meshMap = entityMeshCache.get(entity);
            if (meshMap == null) {
                meshMap = meshBuilder
                        .createTextMesh(font, Arrays.asList(linesOfText), textWidth, HorizontalAlign.CENTER, baseColor,
                                shadowColor, underline);
                entityMeshCache.put(entity, meshMap);
            }

            if (floatingText.isOverlay) {
                glDisable(GL_DEPTH_TEST);
            }

            float scale = METER_PER_PIXEL * floatingText.scale;

            model.setTranslation(worldPos.sub(cameraPosition));


            modelView.set(camera.getViewMatrix()).mul(model)
                    .m00(1.0f).m10(0.0f).m20(0.0f)
                    .m01(0.0f).m11(1.0f).m21(0.0f)
                    .m02(0.0f).m12(0.0f).m22(1.0f);
            modelView.scale(scale, -scale, scale);
            modelView.translate(-textWidth / 2.0f, 0.0f, 0.0f);

            for (Map.Entry<Material, Mesh> meshMapEntry : meshMap.entrySet()) {
                Mesh mesh = meshMapEntry.getValue();
                Material material = meshMapEntry.getKey();
                material.enable();
                material.bindTextures();
                material.setFloat4("croppingBoundaries", Float.MIN_VALUE, Float.MAX_VALUE,
                        Float.MIN_VALUE, Float.MAX_VALUE);
                material.setMatrix4("modelViewMatrix", modelView);
                material.setMatrix4("projectionMatrix", camera.getProjectionMatrix());
                material.setFloat2("offset", 0.0f, 0.0f);
                material.setFloat("alpha", 1.0f);
                mesh.render();
            }

            // Revert to default state
            if (floatingText.isOverlay) {
                glEnable(GL_DEPTH_TEST);
            }
        }
    }

    private void diposeMeshMap(Map<Material, Mesh> meshMap) {
        for (Map.Entry<Material, Mesh> meshMapEntry : meshMap.entrySet()) {
            Mesh mesh = meshMapEntry.getValue();
            mesh.dispose();
            // Note: Material belongs to font and must not be disposed
        }
    }

    @Override
    public void renderAlphaBlend() {
        render(entityManager.getEntitiesWith(FloatingTextComponent.class, LocationComponent.class));
    }

    @ReceiveEvent(components = FloatingTextComponent.class)
    public void onDisplayNameChange(OnChangedComponent event, EntityRef entity) {
        disposeCachedMeshOfEntity(entity);
    }

    @ReceiveEvent(components = FloatingTextComponent.class)
    public void onNameTagOwnerRemoved(BeforeDeactivateComponent event, EntityRef entity) {
        disposeCachedMeshOfEntity(entity);
    }

    private void disposeCachedMeshOfEntity(EntityRef entity) {
        Map<Material, Mesh> meshMap = entityMeshCache.remove(entity);
        if (meshMap != null) {
            diposeMeshMap(meshMap);
        }
    }

    @Override
    public void shutdown() {
        entityMeshCache.values().forEach(this::diposeMeshMap);
        entityMeshCache.clear();
    }
}
