/*
 * Copyright 2013 MovingBlocks
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

import org.terasology.math.Transform;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Set;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.AABB;
import org.terasology.math.MatrixUtils;
import org.terasology.math.VecMath;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.opengl.OpenGLMesh;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

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

        Quat4f worldRot = new Quat4f();
        Vector3f worldPos = new Vector3f();

        FloatBuffer tempMatrixBuffer44 = BufferUtils.createFloatBuffer(16);
        FloatBuffer tempMatrixBuffer33 = BufferUtils.createFloatBuffer(12);

        for (Material material : meshByMaterial.keySet()) {
            if (material.isRenderable()) {
                OpenGLMesh lastMesh = null;
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

                    if (isHidden(entity, meshComp) || location == null || meshComp.mesh == null || !isRelevant(entity, location.getWorldPosition())) {
                        continue;
                    }
                    if (meshComp.mesh.isDisposed()) {
                        logger.error("Attempted to render disposed mesh");
                        continue;
                    }

                    location.getWorldRotation(worldRot);
                    location.getWorldPosition(worldPos);
                    float worldScale = location.getWorldScale();

                    Transform toWorldSpace = new Transform(worldPos, worldRot, worldScale);

                    Vector3f offsetFromCamera = new Vector3f();
                    offsetFromCamera.sub(worldPos, cameraPosition);
                    Matrix4f matrixCameraSpace = new Matrix4f(worldRot, offsetFromCamera, worldScale);

                    AABB aabb = meshComp.mesh.getAABB().transform(toWorldSpace);
                    if (worldRenderer.getActiveCamera().hasInSight(aabb)) {
                        if (meshComp.mesh != lastMesh) {
                            if (lastMesh != null) {
                                lastMesh.postRender();
                            }
                            lastMesh = (OpenGLMesh) meshComp.mesh;
                            lastMesh.preRender();
                        }

                        Matrix4f modelViewMatrix = MatrixUtils.calcModelViewMatrix(worldRenderer.getActiveCamera().getViewMatrix(), matrixCameraSpace);
                        MatrixUtils.matrixToFloatBuffer(modelViewMatrix, tempMatrixBuffer44);
                        MatrixUtils.matrixToFloatBuffer(MatrixUtils.calcNormalMatrix(modelViewMatrix), tempMatrixBuffer33);

                        material.setMatrix4("projectionMatrix", worldRenderer.getActiveCamera().getProjectionMatrix(), true);
                        material.setMatrix4("worldViewMatrix", tempMatrixBuffer44, true);
                        material.setMatrix3("normalMatrix", tempMatrixBuffer33, true);

                        material.setFloat3("colorOffset", meshComp.color.rf(), meshComp.color.gf(), meshComp.color.bf(), true);
                        material.setFloat("sunlight", worldRenderer.getMainLightIntensityAt(worldPos), true);
                        material.setFloat("blockLight", Math.max(worldRenderer.getBlockLightIntensityAt(worldPos), meshComp.selfLuminance), true);

                        lastMesh.doRender();
                    }
                }
                if (lastMesh != null) {
                    lastMesh.postRender();
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
    private boolean isRelevant(EntityRef entity, Vector3f position) {
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
