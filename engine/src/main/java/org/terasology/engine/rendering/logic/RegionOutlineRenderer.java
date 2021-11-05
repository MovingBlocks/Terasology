// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.logic;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.RenderSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.mesh.StandardMeshData;
import org.terasology.engine.rendering.assets.mesh.resource.AllocationType;
import org.terasology.engine.rendering.assets.mesh.resource.DrawingMode;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.joml.geom.AABBf;

import java.util.Map;


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

    private StandardMeshData meshData = new StandardMeshData(DrawingMode.LINES, AllocationType.STREAM);
    private Mesh mesh;

    private Material material;

    private Map<EntityRef, RegionOutlineComponent> entityToRegionOutlineMap = Maps.newLinkedHashMap();

    @Override
    public void initialise() {
        Preconditions.checkArgument(!Strings.isNullOrEmpty("engine:white"));
        this.material = assetManager.getAsset("engine:white", Material.class).get();
        mesh = Assets.generateAsset(meshData, Mesh.class);
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
        GL33.glDepthFunc(GL33.GL_ALWAYS);
        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();

        Vector3f worldPos = new Vector3f();
        Vector3f worldPositionCameraSpace = new Vector3f();
        worldPos.sub(cameraPosition, worldPositionCameraSpace);
        Matrix4f matrixCameraSpace = new Matrix4f().translationRotateScale(worldPositionCameraSpace, new Quaternionf(), 1.0f);
        Matrix4f modelViewMatrix = new Matrix4f(worldRenderer.getActiveCamera().getViewMatrix()).mul(matrixCameraSpace);
        material.setMatrix4("projectionMatrix", worldRenderer.getActiveCamera().getProjectionMatrix());
        material.setMatrix4("modelViewMatrix", modelViewMatrix, true);

        meshData.reallocate(0, 0);
        meshData.indices.rewind();
        meshData.position.rewind();
        meshData.color0.rewind();

        int index = 0;
        Vector3f pos = new Vector3f();
        for (RegionOutlineComponent regionOutline : entityToRegionOutlineMap.values()) {
            if (regionOutline.corner1 == null || regionOutline.corner2 == null) {
                continue;
            }

            BlockRegion region = new BlockRegion(regionOutline.corner1).union(regionOutline.corner2);
            AABBf bounds = region.getBounds(new AABBf());

            meshData.position.put(pos.set(bounds.minX, bounds.minY, bounds.minZ));
            meshData.position.put(pos.set(bounds.maxX, bounds.minY, bounds.minZ));
            meshData.position.put(pos.set(bounds.maxX, bounds.minY, bounds.maxZ));
            meshData.position.put(pos.set(bounds.minX, bounds.minY, bounds.maxZ));

            meshData.position.put(pos.set(bounds.minX, bounds.maxY, bounds.minZ));
            meshData.position.put(pos.set(bounds.maxX, bounds.maxY, bounds.minZ));
            meshData.position.put(pos.set(bounds.maxX, bounds.maxY, bounds.maxZ));
            meshData.position.put(pos.set(bounds.minX, bounds.maxY, bounds.maxZ));

            meshData.color0.put(regionOutline.color);
            meshData.color0.put(regionOutline.color);
            meshData.color0.put(regionOutline.color);
            meshData.color0.put(regionOutline.color);

            meshData.color0.put(regionOutline.color);
            meshData.color0.put(regionOutline.color);
            meshData.color0.put(regionOutline.color);
            meshData.color0.put(regionOutline.color);

            meshData.indices.putAll(new int[]{
                    // top loop
                    index, index + 1,
                    index + 1, index + 2,
                    index + 2, index + 3,
                    index + 3, index,

                    // connecting edges between top and bottom
                    index, index + 4,
                    index + 1, index + 5,
                    index + 2, index + 6,
                    index + 3, index + 7,

                    // bottom loop
                    index + 4, index + 5,
                    index + 5, index + 6,
                    index + 6, index + 7,
                    index + 7, index + 4,

            });

            index += 8;
        }
        material.enable();
        mesh.reload(meshData);
        mesh.render();
        GL33.glDepthFunc(GL33.GL_LEQUAL);
    }
}
