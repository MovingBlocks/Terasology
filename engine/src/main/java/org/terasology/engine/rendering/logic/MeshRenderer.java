// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.logic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.RenderSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.world.WorldProvider;
import org.terasology.joml.geom.AABBf;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Set;

/**
 * TODO: This should be made generic (no explicit shader or mesh) and ported directly into WorldRenderer? Later note: some GelCube functionality moved to a module
 */
@RegisterSystem(RegisterMode.CLIENT)
public class MeshRenderer extends BaseComponentSystem implements RenderSystem {
    private static final Logger logger = LoggerFactory.getLogger(MeshRenderer.class);

    @In
    private NetworkSystem network;

    @In
    private Config config;

    @In
    private WorldRenderer worldRenderer;

    @In
    private WorldProvider worldProvider;

    private NearestSortingList opaqueMeshSorter = new NearestSortingList();
    private NearestSortingList translucentMeshSorter = new NearestSortingList();

    private int lastRendered;

    @Override
    public void initialise() {
        opaqueMeshSorter.initialise(worldRenderer.getActiveCamera());
        translucentMeshSorter.initialise(worldRenderer.getActiveCamera());
    }

    @Override
    public void shutdown() {
        opaqueMeshSorter.stop();
        translucentMeshSorter.stop();
    }

    @ReceiveEvent(components = {MeshComponent.class, LocationComponent.class})
    public void onNewMesh(OnActivatedComponent event, EntityRef entity) {
        addMesh(entity);
    }


    private boolean isHidden(EntityRef entity, MeshComponent mesh) {
        if (!mesh.hideFromOwner) {
            return false;
        }
        ClientComponent owner = network.getOwnerEntity(entity).getComponent(ClientComponent.class);
        return (owner != null && owner.local);
    }

    private void addMesh(EntityRef entity) {
        MeshComponent meshComponent = entity.getComponent(MeshComponent.class);
        if (meshComponent != null && meshComponent.material != null) {
            if (meshComponent.translucent) {
                translucentMeshSorter.add(entity);
            } else {
                opaqueMeshSorter.add(entity);
            }
        }
    }

    @ReceiveEvent(components = MeshComponent.class)
    public void onChangeMesh(OnChangedComponent event, EntityRef entity) {
        removeMesh(entity);
        if (entity.hasComponent(LocationComponent.class)) {
            addMesh(entity);
        }
    }

    private void removeMesh(EntityRef entity) {
        MeshComponent meshComponent = entity.getComponent(MeshComponent.class);
        if (meshComponent != null && meshComponent.material != null) {
            if (meshComponent.translucent) {
                translucentMeshSorter.remove(entity);
            } else {
                opaqueMeshSorter.remove(entity);
            }
        }
    }

    @ReceiveEvent(components = {MeshComponent.class, LocationComponent.class})
    public void onDestroyMesh(BeforeDeactivateComponent event, EntityRef entity) {
        removeMesh(entity);
    }

    @Override
    public void renderAlphaBlend() {
        if (config.getRendering().isRenderNearest()) {
            renderEntities(Arrays.asList(translucentMeshSorter.getNearest(config.getRendering().getMeshLimit())));
        } else {
            renderEntities(translucentMeshSorter.getEntities());
        }
    }

    public void renderOpaque() {
        if (config.getRendering().isRenderNearest()) {
            renderEntities(Arrays.asList(opaqueMeshSorter.getNearest(config.getRendering().getMeshLimit())));
        } else {
            renderEntities(opaqueMeshSorter.getEntities());
        }
    }

    private void renderEntities(Iterable<EntityRef> entityRefs) {
        SetMultimap<Material, EntityRef> entitiesToRender = HashMultimap.create();
        for (EntityRef entity : entityRefs) {
            MeshComponent meshComponent = entity.getComponent(MeshComponent.class);
            if (meshComponent != null && meshComponent.material != null) {
                entitiesToRender.put(meshComponent.material, entity);
            }
        }
        renderEntitiesByMaterial(entitiesToRender);
    }

    private void renderEntitiesByMaterial(SetMultimap<Material, EntityRef> meshByMaterial) {
        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();

        Quaternionf worldRot = new Quaternionf();
        Vector3f worldPos = new Vector3f();
        Matrix3f normalMatrix = new Matrix3f();
        Matrix4f matrixCameraSpace = new Matrix4f();
        Matrix4f modelViewMatrix = new Matrix4f();

        FloatBuffer tempMatrixBuffer44 = BufferUtils.createFloatBuffer(16);
        FloatBuffer tempMatrixBuffer33 = BufferUtils.createFloatBuffer(12);

        for (Material material : meshByMaterial.keySet()) {
            if (material.isRenderable()) {
                material.enable();
                material.setFloat("sunlight", 1.0f, true);
                material.setFloat("blockLight", 1.0f, true);
                material.setMatrix4("projectionMatrix", worldRenderer.getActiveCamera().getProjectionMatrix(), true);
                material.bindTextures();

                Set<EntityRef> entities = meshByMaterial.get(material);
                lastRendered = entities.size();
                for (EntityRef entity : entities) {
                    MeshComponent meshComp = entity.getComponent(MeshComponent.class);
                    LocationComponent location = entity.getComponent(LocationComponent.class);
                    if (isHidden(entity, meshComp) || location == null || meshComp.mesh == null) {
                        continue;
                    }
                    Vector3f worldPosition = location.getWorldPosition(new Vector3f());
                    if (!worldPosition.isFinite() && !isRelevant(entity, worldPosition)) {
                        continue;
                    }

                    if (meshComp.mesh.isDisposed()) {
                        logger.error("Attempted to render disposed mesh");
                        continue;
                    }

                    worldRot.set(location.getWorldRotation(new Quaternionf()));
                    worldPos.set(location.getWorldPosition(new Vector3f()));
                    float worldScale = location.getWorldScale();

                    Vector3f offsetFromCamera = worldPos.sub(cameraPosition, new Vector3f());
                    matrixCameraSpace.translationRotateScale(offsetFromCamera, worldRot, worldScale);


                    AABBf aabb = meshComp.mesh.getAABB().transform(new Matrix4f().translationRotateScale(worldPos, worldRot, worldScale), new AABBf());
                    if (worldRenderer.getActiveCamera().hasInSight(aabb)) {
                        modelViewMatrix.set(worldRenderer.getActiveCamera().getViewMatrix()).mul(matrixCameraSpace);
                        modelViewMatrix.get(tempMatrixBuffer44);
                        modelViewMatrix.normal(normalMatrix).get(tempMatrixBuffer33);

                        material.setMatrix4("projectionMatrix", worldRenderer.getActiveCamera().getProjectionMatrix(), true);
                        material.setMatrix4("modelViewMatrix", tempMatrixBuffer44, true);
                        material.setMatrix3("normalMatrix", tempMatrixBuffer33, true);

                        material.setFloat3("colorOffset", meshComp.color.rf(), meshComp.color.gf(), meshComp.color.bf(), true);
                        material.setFloat("sunlight", worldRenderer.getMainLightIntensityAt(worldPos), true);
                        material.setFloat("blockLight", Math.max(worldRenderer.getBlockLightIntensityAt(worldPos), meshComp.selfLuminance), true);

                        meshComp.mesh.render();
                    }
                }
            }
        }
    }

    /**
     * Checks whether the entity at the given position is relevant.
     * <p>
     * The entity at the given position is relevant if
     * a) the entity itself is always relevant, or
     * b) the block at the position is relevant.
     *
     * @param entity   the entity to check for relevance
     * @param position the world position the entity is located
     * @return true if the entity itself or the block at the given position are relevant, false otherwise.
     */
    private boolean isRelevant(EntityRef entity, Vector3fc position) {
        return worldProvider.isBlockRelevant(position) || entity.isAlwaysRelevant();
    }

    @Override
    public void renderOverlay() {
    }

    @Override
    public void renderShadows() {
    }

    public int getLastRendered() {
        return lastRendered;
    }
}
