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

import com.bulletphysics.linearmath.Transform;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
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

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * TODO: This should be made generic (no explicit shader or mesh) and ported directly into WorldRenderer? Later note: some GelCube functionality moved to a module
 *
 */
@RegisterSystem(RegisterMode.CLIENT)
public class MeshRenderer extends BaseComponentSystem implements RenderSystem {
    private static final Logger logger = LoggerFactory.getLogger(MeshRenderer.class);

    @In
    private NetworkSystem network;

    @In
    private LocalPlayer localPlayer;

    @In
    private Config config;

    @In
    private WorldRenderer worldRenderer;

    @In
    private WorldProvider worldProvider;

    private SetMultimap<Material, EntityRef> opaqueMesh = HashMultimap.create();
    private SetMultimap<Material, EntityRef> translucentMesh = HashMultimap.create();
    private Map<EntityRef, Material> opaqueEntities = Maps.newHashMap();
    private Map<EntityRef, Material> translucentEntities = Maps.newHashMap();

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
        MeshComponent meshComp = entity.getComponent(MeshComponent.class);
        if (meshComp.material != null) {
            if (meshComp.translucent) {
                translucentMesh.put(meshComp.material, entity);
                translucentEntities.put(entity, meshComp.material);
                translucentMeshSorter.add(entity);
            } else {
                opaqueMesh.put(meshComp.material, entity);
                opaqueEntities.put(entity, meshComp.material);
                opaqueMeshSorter.add(entity);
            }
        }
    }

    @ReceiveEvent(components = {MeshComponent.class})
    public void onChangeMesh(OnChangedComponent event, EntityRef entity) {
        removeMesh(entity);
        if (entity.hasComponent(LocationComponent.class)) {
            addMesh(entity);
        }
    }

    private void removeMesh(EntityRef entity) {
        Material mat = opaqueEntities.remove(entity);
        if (mat != null) {
            opaqueMesh.remove(mat, entity);
            opaqueMeshSorter.remove(entity);
        } else {
            mat = translucentEntities.remove(entity);
            if (mat != null) {
                translucentMesh.remove(mat, entity);
                translucentMeshSorter.remove(entity);
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
            renderAlphaBlend(Arrays.asList(translucentMeshSorter.getNearest(config.getRendering().getMeshLimit())));
        } else {
            renderAlphaBlend(translucentEntities.keySet());
        }
    }

    private void renderAlphaBlend(Iterable<EntityRef> entityRefs) {
        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();

        FloatBuffer tempMatrixBuffer44 = BufferUtils.createFloatBuffer(16);
        FloatBuffer tempMatrixBuffer33 = BufferUtils.createFloatBuffer(12);

        for (EntityRef entity : entityRefs) {
            MeshComponent meshComp = entity.getComponent(MeshComponent.class);
            if (meshComp != null && meshComp.material != null && meshComp.material.isRenderable()) {
                meshComp.material.enable();
                LocationComponent location = entity.getComponent(LocationComponent.class);
                if (location == null) {
                    continue;
                }
                if (isHidden(entity, meshComp)) {
                    continue;
                }

                Quat4f worldRot = location.getWorldRotation();
                Vector3f worldPos = location.getWorldPosition();
                float worldScale = location.getWorldScale();
                AABB aabb = meshComp.mesh.getAABB().transform(worldRot, worldPos, worldScale);
                if (worldRenderer.getActiveCamera().hasInSight(aabb)) {
                    Vector3f worldPositionCameraSpace = new Vector3f();
                    worldPositionCameraSpace.sub(worldPos, cameraPosition);
                    Matrix4f matrixCameraSpace = new Matrix4f(worldRot, worldPositionCameraSpace, worldScale);
                    Matrix4f modelViewMatrix = MatrixUtils.calcModelViewMatrix(worldRenderer.getActiveCamera().getViewMatrix(), matrixCameraSpace);
                    MatrixUtils.matrixToFloatBuffer(modelViewMatrix, tempMatrixBuffer44);

                    meshComp.material.setMatrix4("projectionMatrix", worldRenderer.getActiveCamera().getProjectionMatrix());
                    meshComp.material.setMatrix4("worldViewMatrix", tempMatrixBuffer44, true);

                    MatrixUtils.matrixToFloatBuffer(MatrixUtils.calcNormalMatrix(modelViewMatrix), tempMatrixBuffer33);
                    meshComp.material.setMatrix3("normalMatrix", tempMatrixBuffer33, true);
                    meshComp.material.setFloat4("colorOffset", meshComp.color.rf(), meshComp.color.gf(), meshComp.color.bf(), meshComp.color.af(), true);
                    meshComp.material.setFloat("light", worldRenderer.getRenderingLightValueAt(worldPos), true);
                    meshComp.material.setFloat("sunlight", worldRenderer.getSunlightValueAt(worldPos), true);

                    OpenGLMesh mesh = (OpenGLMesh) meshComp.mesh;
                    meshComp.material.bindTextures();
                    mesh.render();
                }
            }
        }
    }

    @Override
    public void renderOpaque() {
        if (config.getRendering().isRenderNearest()) {
            SetMultimap<Material, EntityRef> entitiesToRender = HashMultimap.create();
            for (EntityRef entity : Arrays.asList(opaqueMeshSorter.getNearest(config.getRendering().getMeshLimit()))) {
                MeshComponent meshComp = entity.getComponent(MeshComponent.class);
                if (meshComp != null && meshComp.material != null) {
                    entitiesToRender.put(meshComp.material, entity);
                }
            }
            renderOpaque(entitiesToRender);
        } else {
            renderOpaque(opaqueMesh);
        }
    }

    private void renderOpaque(SetMultimap<Material, EntityRef> meshByMaterial) {
        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();

        Quat4f worldRot = new Quat4f();
        Vector3f worldPos = new Vector3f();
        Transform transWorldSpace = new Transform();

        FloatBuffer tempMatrixBuffer44 = BufferUtils.createFloatBuffer(16);
        FloatBuffer tempMatrixBuffer33 = BufferUtils.createFloatBuffer(12);

        for (Material material : meshByMaterial.keySet()) {
            if (material.isRenderable()) {
                OpenGLMesh lastMesh = null;
                material.enable();
                material.setFloat("sunlight", 1.0f);
                material.setFloat("blockLight", 1.0f);
                material.setMatrix4("projectionMatrix", worldRenderer.getActiveCamera().getProjectionMatrix());
                material.bindTextures();

                Set<EntityRef> entities = meshByMaterial.get(material);
                lastRendered = entities.size();
                for (EntityRef entity : entities) {
                    MeshComponent meshComp = entity.getComponent(MeshComponent.class);
                    LocationComponent location = entity.getComponent(LocationComponent.class);

                    if (isHidden(entity, meshComp) || location == null || meshComp.mesh == null
                            || !worldProvider.isBlockRelevant(location.getWorldPosition())) {
                        continue;
                    }
                    if (meshComp.mesh.isDisposed()) {
                        logger.error("Attempted to render disposed mesh");
                        continue;
                    }

                    location.getWorldRotation(worldRot);
                    location.getWorldPosition(worldPos);
                    float worldScale = location.getWorldScale();

                    javax.vecmath.Matrix4f matrixWorldSpace = new javax.vecmath.Matrix4f(VecMath.to(worldRot), VecMath.to(worldPos), worldScale);
                    transWorldSpace.set(matrixWorldSpace);

                    Vector3f worldPositionCameraSpace = new Vector3f();
                    worldPositionCameraSpace.sub(worldPos, cameraPosition);
                    Matrix4f matrixCameraSpace = new Matrix4f(worldRot, worldPositionCameraSpace, worldScale);

                    AABB aabb = meshComp.mesh.getAABB().transform(transWorldSpace);
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

                        material.setMatrix4("worldViewMatrix", tempMatrixBuffer44, true);

                        MatrixUtils.matrixToFloatBuffer(MatrixUtils.calcNormalMatrix(modelViewMatrix), tempMatrixBuffer33);
                        material.setMatrix3("normalMatrix", tempMatrixBuffer33, true);

                        material.setFloat3("colorOffset", meshComp.color.rf(), meshComp.color.gf(), meshComp.color.bf(), true);
                        material.setFloat("sunlight", worldRenderer.getSunlightValueAt(worldPos), true);
                        material.setFloat("blockLight", worldRenderer.getBlockLightValueAt(worldPos), true);

                        lastMesh.doRender();
                    }
                }
                if (lastMesh != null) {
                    lastMesh.postRender();
                }
            }
        }
    }

    @Override
    public void renderOverlay() {
    }

    @Override
    public void renderFirstPerson() {
    }

    @Override
    public void renderShadows() {
    }

    public int getLastRendered() {
        return lastRendered;
    }
}
