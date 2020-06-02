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
import org.terasology.math.JomlUtil;
import org.terasology.math.MatrixUtils;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.world.WorldRenderer;

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
        Vector3f cameraPosition = JomlUtil.from(worldRenderer.getActiveCamera().getPosition());

        FloatBuffer tempMatrixBuffer44 = BufferUtils.createFloatBuffer(16);
        FloatBuffer tempMatrixBuffer33 = BufferUtils.createFloatBuffer(12);

        material.setFloat("sunlight", 1.0f, true);
        material.setFloat("blockLight", 1.0f, true);
        material.setMatrix4("projectionMatrix", new org.joml.Matrix4f(worldRenderer.getActiveCamera().getProjectionMatrix()).transpose());
        Vector3f worldPos = new Vector3f();

        Vector3f worldPositionCameraSpace = new Vector3f();
        worldPositionCameraSpace.sub(worldPos, cameraPosition);

        Matrix4f matrixCameraSpace = new Matrix4f(new Quat4f(0, 0, 0, 1), worldPositionCameraSpace, 1.0f);

        Matrix4f modelViewMatrix = MatrixUtils.calcModelViewMatrix(JomlUtil.from(worldRenderer.getActiveCamera().getViewMatrix()), matrixCameraSpace);
        MatrixUtils.matrixToFloatBuffer(modelViewMatrix, tempMatrixBuffer44);

        material.setMatrix4("worldViewMatrix", tempMatrixBuffer44, true);

        MatrixUtils.matrixToFloatBuffer(MatrixUtils.calcNormalMatrix(modelViewMatrix), tempMatrixBuffer33);
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

        Region3i region = Region3i.createBounded(regionComponent.corner1, regionComponent.corner2);
        Vector3f min = new Vector3f(region.minX() - 0.5f, region.minY() - 0.5f, region.minZ() - 0.5f);
        Vector3f max = new Vector3f(region.maxX() + 0.5f, region.maxY() + 0.5f, region.maxZ() + 0.5f);

        // 4 lines along x axis:
        glBegin(GL11.GL_LINES);
        glVertex3f(min.x(), min.y(), min.z());
        glVertex3f(max.x(), min.y(), min.z());
        glEnd();

        glBegin(GL11.GL_LINES);
        glVertex3f(min.x(), max.y(), min.z());
        glVertex3f(max.x(), max.y(), min.z());
        glEnd();

        glBegin(GL11.GL_LINES);
        glVertex3f(min.x(), min.y(), max.z());
        glVertex3f(max.x(), min.y(), max.z());
        glEnd();

        glBegin(GL11.GL_LINES);
        glVertex3f(min.x(), max.y(), max.z());
        glVertex3f(max.x(), max.y(), max.z());
        glEnd();


        // 4 lines along y axis
        glBegin(GL11.GL_LINES);
        glVertex3f(min.x(), min.y(), min.z());
        glVertex3f(min.x(), max.y(), min.z());
        glEnd();

        glBegin(GL11.GL_LINES);
        glVertex3f(max.x(), min.y(), min.z());
        glVertex3f(max.x(), max.y(), min.z());
        glEnd();

        glBegin(GL11.GL_LINES);
        glVertex3f(min.x(), min.y(), max.z());
        glVertex3f(min.x(), max.y(), max.z());
        glEnd();

        glBegin(GL11.GL_LINES);
        glVertex3f(max.x(), min.y(), max.z());
        glVertex3f(max.x(), max.y(), max.z());
        glEnd();

        // 4 lines along z axis:
        glBegin(GL11.GL_LINES);
        glVertex3f(min.x(), min.y(), min.z());
        glVertex3f(min.x(), min.y(), max.z());
        glEnd();

        glBegin(GL11.GL_LINES);
        glVertex3f(max.x(), min.y(), min.z());
        glVertex3f(max.x(), min.y(), max.z());
        glEnd();

        glBegin(GL11.GL_LINES);
        glVertex3f(min.x(), max.y(), min.z());
        glVertex3f(min.x(), max.y(), max.z());
        glEnd();

        glBegin(GL11.GL_LINES);
        glVertex3f(max.x(), max.y(), min.z());
        glVertex3f(max.x(), max.y(), max.z());
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
