/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.rendering.logic;

import com.google.api.client.util.Maps;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.font.FontMeshBuilder;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.opengl.OpenGLUtil;
import org.terasology.world.WorldProvider;

import java.util.Arrays;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScaled;
import static org.lwjgl.opengl.GL11.glTranslated;


@RegisterSystem(RegisterMode.CLIENT)
public class FloatingTextRenderer extends BaseComponentSystem implements  RenderSystem {

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
        glDisable(GL_DEPTH_TEST);

        Vector3f cameraPosition = camera.getPosition();

        for (EntityRef entity : floatingTextEntities) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            if (location == null) {
                continue;
            }

            Vector3f worldPos = location.getWorldPosition();
            if (!worldProvider.isBlockRelevant(worldPos)) {
                continue;
            }

            FloatingTextComponent floatingText = entity.getComponent(FloatingTextComponent.class);

            String text = floatingText.text;
            Color baseColor = floatingText.textColor;
            Color shadowColor = floatingText.textShadowColor;
            boolean underline = false;
            int textWidth = font.getWidth(text);

            FontMeshBuilder meshBuilder = new FontMeshBuilder(underlineMaterial);

            Map<Material, Mesh> meshMap = entityMeshCache.get(entity);
            if (meshMap == null) {
                meshMap = meshBuilder
                        .createTextMesh(font, Arrays.asList(text), textWidth, HorizontalAlign.CENTER, baseColor,
                                shadowColor, underline);
                entityMeshCache.put(entity, meshMap);
            }
            glPushMatrix();

            float scale = METER_PER_PIXEL * floatingText.scale;

            glTranslated(worldPos.x - cameraPosition.x, worldPos.y - cameraPosition.y, worldPos.z - cameraPosition.z);
            OpenGLUtil.applyBillboardOrientation();
            glScaled(scale, -scale, scale);
            glTranslated(-textWidth / 2.0, 0.0, 0.0);
            for (Map.Entry<Material, Mesh> meshMapEntry : meshMap.entrySet()) {
                Mesh mesh = meshMapEntry.getValue();
                Material material = meshMapEntry.getKey();
                material.enable();
                material.bindTextures();
                material.setFloat4("croppingBoundaries", Float.MIN_VALUE, Float.MAX_VALUE,
                        Float.MIN_VALUE, Float.MAX_VALUE);
                material.setFloat2("offset", 0.0f, 0.0f);
                material.setFloat("alpha", 1.0f);
                mesh.render();
            }

            glPopMatrix();
        }
        glEnable(GL_DEPTH_TEST);
    }

    private void diposeMeshMap(Map<Material, Mesh> meshMap) {
        for (Map.Entry<Material, Mesh> meshMapEntry : meshMap.entrySet()) {
            Mesh mesh = meshMapEntry.getValue();
            mesh.dispose();
            // Note: Material belongs to font and must not be disposed
        }
    }

    @Override
    public void renderOpaque() {
    }

    @Override
    public void renderAlphaBlend() {
        render(entityManager.getEntitiesWith(FloatingTextComponent.class, LocationComponent.class));
    }

    @Override
    public void renderFirstPerson() {
    }

    @Override
    public void renderOverlay() {
    }

    @Override
    public void renderShadows() {
    }

    @ReceiveEvent(components = {FloatingTextComponent.class })
    public void onDisplayNameChange(OnChangedComponent event, EntityRef entity) {
        disposeCachedMeshOfEntity(entity);
    }


    @ReceiveEvent(components = {FloatingTextComponent.class })
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
