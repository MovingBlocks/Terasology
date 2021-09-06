// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.logic;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;
import org.terasology.engine.config.Config;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.RenderSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.physics.components.shapes.BoxShapeComponent;
import org.terasology.engine.physics.components.shapes.CapsuleShapeComponent;
import org.terasology.engine.physics.components.shapes.CylinderShapeComponent;
import org.terasology.engine.physics.components.shapes.SphereShapeComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.mesh.StandardMeshData;
import org.terasology.engine.rendering.assets.mesh.resource.AllocationType;
import org.terasology.engine.rendering.assets.mesh.resource.DrawingMode;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.joml.geom.AABBf;
import org.terasology.nui.Color;

/**
 * Renders the bounding boxes of entities when the debug setting "renderEntityColliders" is active.
 * <p>
 * The entity must have a {@link LocationComponent} and at least one of the following shape components:
 * <ul>
 *     <li>{@link BoxShapeComponent}</li>
 *     <li>{@link CapsuleShapeComponent}</li>
 *     <li>{@link CylinderShapeComponent}</li>
 *     <li>{@link SphereShapeComponent}</li>
 * </ul>
 *
 * @see BoxShapeComponent
 * @see CapsuleShapeComponent
 * @see CylinderShapeComponent
 * @see SphereShapeComponent
 */
@RegisterSystem(RegisterMode.CLIENT)
public class BoundingBoxRenderer extends BaseComponentSystem implements RenderSystem {

    @In
    Config config;

    @In
    WorldRenderer worldRenderer;

    @In
    AssetManager assetManager;

    @In
    EntityManager entityManager;

    private StandardMeshData meshData;
    private Material material;
    private Mesh mesh;

    @Override
    public void initialise() {
        material = assetManager.getAsset("engine:white", Material.class).get();
        meshData = new StandardMeshData(DrawingMode.LINES, AllocationType.STREAM);
        mesh = Assets.generateAsset(meshData, Mesh.class);
    }

    @Override
    public void renderOverlay() {
        if (config.getRendering().getDebug().isRenderingEntityColliders()) {
            GL33.glDepthFunc(GL33.GL_ALWAYS);
            Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();

            Vector3f worldPos = new Vector3f();
            Vector3f worldPositionCameraSpace = new Vector3f();
            worldPos.sub(cameraPosition, worldPositionCameraSpace);
            Matrix4f matrixCameraSpace = new Matrix4f().translationRotateScale(worldPositionCameraSpace, new Quaternionf(), 1.0f);
            Matrix4f modelViewMatrix = new Matrix4f(worldRenderer.getActiveCamera().getViewMatrix()).mul(matrixCameraSpace);
            material.setMatrix4("projectionMatrix", worldRenderer.getActiveCamera().getProjectionMatrix());
            material.setMatrix4("modelViewMatrix", modelViewMatrix, true);

            int index = 0;
            meshData.reallocate(0, 0);
            meshData.indices.rewind();
            meshData.position.rewind();
            meshData.color0.rewind();

            Vector3f worldPosition = new Vector3f();
            Quaternionf worldRot = new Quaternionf();
            Matrix4f transform = new Matrix4f();
            AABBf bounds = new AABBf(0, 0, 0, 0, 0, 0);

            for (EntityRef entity : entityManager.getEntitiesWith(LocationComponent.class)) {
                LocationComponent location = entity.getComponent(LocationComponent.class);
                location.getWorldPosition(worldPosition);
                location.getWorldRotation(worldRot);

                BoxShapeComponent boxShapeComponent = entity.getComponent(BoxShapeComponent.class);
                if (boxShapeComponent != null) {
                    bounds.set(0, 0, 0, 0, 0, 0);
                    bounds.expand(new Vector3f(boxShapeComponent.extents).div(2.0f));
                    transform.translationRotateScale(worldPosition, worldRot, location.getWorldScale());
                    bounds.transform(transform);
                    index = addRenderBound(meshData, bounds, index);
                }
                CapsuleShapeComponent capsuleComponent = entity.getComponent(CapsuleShapeComponent.class);
                if (capsuleComponent != null) {
                    bounds.set(0, 0, 0, 0, 0, 0);
                    bounds.expand(new Vector3f(capsuleComponent.radius, capsuleComponent.height / 2.0f, capsuleComponent.radius).div(2.0f));
                    transform.translationRotateScale(worldPosition, worldRot, location.getWorldScale());
                    bounds.transform(transform);
                    index = addRenderBound(meshData, bounds, index);
                }
                CylinderShapeComponent cylinderShapeComponent = entity.getComponent(CylinderShapeComponent.class);
                if (cylinderShapeComponent != null) {
                    bounds.set(0, 0, 0, 0, 0, 0);
                    bounds.expand(new Vector3f(cylinderShapeComponent.radius, cylinderShapeComponent.height / 2.0f,
                            cylinderShapeComponent.radius).div(2.0f));
                    transform.translationRotateScale(worldPosition, worldRot, location.getWorldScale());
                    bounds.transform(transform);
                    index = addRenderBound(meshData, bounds, index);
                }
                SphereShapeComponent sphereShapeComponent = entity.getComponent(SphereShapeComponent.class);
                if (sphereShapeComponent != null) {
                    bounds.set(0, 0, 0, 0, 0, 0);
                    bounds.expand(new Vector3f(sphereShapeComponent.radius).div(2.0f));
                    transform.translationRotateScale(worldPosition, worldRot, location.getWorldScale());
                    bounds.transform(transform);
                    index = addRenderBound(meshData, bounds, index);
                }
            }

            material.enable();
            mesh.reload(meshData);
            mesh.render();
            GL33.glDepthFunc(GL33.GL_LEQUAL);
        }
    }

    private int addRenderBound(StandardMeshData meshData, AABBf bounds, int index) {
        Vector3f pos = new Vector3f();
        meshData.position.put(pos.set(bounds.minX, bounds.minY, bounds.minZ));
        meshData.position.put(pos.set(bounds.maxX, bounds.minY, bounds.minZ));
        meshData.position.put(pos.set(bounds.maxX, bounds.minY, bounds.maxZ));
        meshData.position.put(pos.set(bounds.minX, bounds.minY, bounds.maxZ));

        meshData.position.put(pos.set(bounds.minX, bounds.maxY, bounds.minZ));
        meshData.position.put(pos.set(bounds.maxX, bounds.maxY, bounds.minZ));
        meshData.position.put(pos.set(bounds.maxX, bounds.maxY, bounds.maxZ));
        meshData.position.put(pos.set(bounds.minX, bounds.maxY, bounds.maxZ));

        meshData.color0.put(Color.black);
        meshData.color0.put(Color.black);
        meshData.color0.put(Color.black);
        meshData.color0.put(Color.black);

        meshData.color0.put(Color.black);
        meshData.color0.put(Color.black);
        meshData.color0.put(Color.black);
        meshData.color0.put(Color.black);

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
        return index + 8;
    }
}
