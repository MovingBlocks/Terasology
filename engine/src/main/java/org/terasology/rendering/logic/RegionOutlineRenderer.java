/*
 * Copyright 2016 MovingBlocks
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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.joml.geom.AABBf;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.block.BlockRegion;

import java.nio.FloatBuffer;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex3f;

/**
 * Renderes region outlines for all entities with  {@link RegionOutlineComponent}s.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class RegionOutlineRenderer extends BaseComponentSystem implements RenderSystem {

    @In
    private AssetManager assetManager;

    @In
    private WorldRenderer worldRenderer;

    @In
    private EntityManager entityManager;


    private Material material;

    private Map<EntityRef, RegionOutlineComponent> entityToRegionOutlineMap = Maps.newLinkedHashMap();

    @Override
    public void initialise() {
        Preconditions.checkArgument(!Strings.isNullOrEmpty("engine:white"));
        this.material = assetManager.getAsset("engine:white", Material.class).get();
    }


    @ReceiveEvent
    public void onRegionOutlineComponentActivation(OnActivatedComponent event, EntityRef entity,
                                                   RegionOutlineComponent component) {
        entityToRegionOutlineMap.put(entity, component);
    }

    @ReceiveEvent
    public void onRegionOutlineComponentDeactivation(BeforeDeactivateComponent event, EntityRef entity,
                                                     RegionOutlineComponent component) {
        entityToRegionOutlineMap.remove(entity);
    }


    @Override
    public void renderOverlay() {
        if (entityToRegionOutlineMap.isEmpty()) {
            return; // skip everything if there is nothing to do to avoid possibly costly draw mode changes
        }
        glDisable(GL_DEPTH_TEST);
        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();

        FloatBuffer tempMatrixBuffer44 = BufferUtils.createFloatBuffer(16);
        FloatBuffer tempMatrixBuffer33 = BufferUtils.createFloatBuffer(12);

        material.setFloat("sunlight", 1.0f, true);
        material.setFloat("blockLight", 1.0f, true);
        material.setMatrix4("projectionMatrix", worldRenderer.getActiveCamera().getProjectionMatrix());
        Vector3f worldPos = new Vector3f();

        Vector3f worldPositionCameraSpace = new Vector3f();
        worldPos.sub(cameraPosition, worldPositionCameraSpace);

        Matrix4f matrixCameraSpace = new Matrix4f().translationRotateScale(worldPositionCameraSpace, new Quaternionf(), 1.0f);

        Matrix4f modelViewMatrix = new Matrix4f(worldRenderer.getActiveCamera().getViewMatrix()).mul(matrixCameraSpace);
        Matrix3f normalMatrix = new Matrix3f();
        modelViewMatrix.get(tempMatrixBuffer44);
        modelViewMatrix.normal(normalMatrix).get(tempMatrixBuffer33);

        material.setMatrix4("worldViewMatrix", tempMatrixBuffer44, true);
        material.setMatrix3("normalMatrix", tempMatrixBuffer33, true);

        for (RegionOutlineComponent regionOutline : entityToRegionOutlineMap.values()) {
            material.setFloat3("colorOffset", regionOutline.color.rf(), regionOutline.color.gf(), regionOutline.color.bf(), true);
            drawRegionOutline(regionOutline);
        }

        glEnable(GL_DEPTH_TEST);
    }

    private void drawRegionOutline(RegionOutlineComponent regionComponent) {
        if (regionComponent.corner1 == null || regionComponent.corner2 == null) {
            return;
        }

        BlockRegion region = new BlockRegion(regionComponent.corner1).union(regionComponent.corner2);
        AABBf bounds = region.getBounds(new AABBf());
        // 4 lines along x axis:
        glBegin(GL11.GL_LINES);
        glVertex3f(bounds.minX, bounds.minY, bounds.minZ);
        glVertex3f(bounds.maxX, bounds.minY, bounds.minZ);
        glEnd();

        glBegin(GL11.GL_LINES);
        glVertex3f(bounds.minX, bounds.maxY, bounds.minZ);
        glVertex3f(bounds.maxX, bounds.maxY, bounds.minZ);
        glEnd();

        glBegin(GL11.GL_LINES);
        glVertex3f(bounds.minX, bounds.minY, bounds.maxZ);
        glVertex3f(bounds.maxX, bounds.minY, bounds.maxZ);
        glEnd();

        glBegin(GL11.GL_LINES);
        glVertex3f(bounds.minX, bounds.maxY, bounds.maxZ);
        glVertex3f(bounds.maxX, bounds.maxY, bounds.maxZ);
        glEnd();


        // 4 lines along y axis
        glBegin(GL11.GL_LINES);
        glVertex3f(bounds.minX, bounds.minY, bounds.minZ);
        glVertex3f(bounds.minX, bounds.maxY, bounds.minZ);
        glEnd();

        glBegin(GL11.GL_LINES);
        glVertex3f(bounds.maxX, bounds.minY, bounds.minZ);
        glVertex3f(bounds.maxX, bounds.maxY, bounds.minZ);
        glEnd();

        glBegin(GL11.GL_LINES);
        glVertex3f(bounds.minX, bounds.minY, bounds.maxZ);
        glVertex3f(bounds.minX, bounds.maxY, bounds.maxZ);
        glEnd();

        glBegin(GL11.GL_LINES);
        glVertex3f(bounds.maxX, bounds.minY, bounds.maxZ);
        glVertex3f(bounds.maxX, bounds.maxY, bounds.maxZ);
        glEnd();

        // 4 lines along z axis:
        glBegin(GL11.GL_LINES);
        glVertex3f(bounds.minX, bounds.minY, bounds.minZ);
        glVertex3f(bounds.minX, bounds.minY, bounds.maxZ);
        glEnd();

        glBegin(GL11.GL_LINES);
        glVertex3f(bounds.maxX, bounds.minY, bounds.minZ);
        glVertex3f(bounds.maxX, bounds.minY, bounds.maxZ);
        glEnd();

        glBegin(GL11.GL_LINES);
        glVertex3f(bounds.minX, bounds.maxY, bounds.minZ);
        glVertex3f(bounds.minX, bounds.maxY, bounds.maxZ);
        glEnd();

        glBegin(GL11.GL_LINES);
        glVertex3f(bounds.maxX, bounds.maxY, bounds.minZ);
        glVertex3f(bounds.maxX, bounds.maxY, bounds.maxZ);
        glEnd();

    }

    @Override
    public void renderOpaque() {
    }

    @Override
    public void renderAlphaBlend() {
    }

    @Override
    public void renderShadows() {
    }
}
