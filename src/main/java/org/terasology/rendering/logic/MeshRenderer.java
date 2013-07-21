/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslated;

import java.nio.FloatBuffer;
import java.util.Set;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.componentSystem.RenderSystem;
import org.terasology.components.utility.DroppedItemTypeComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.math.AABB;
import org.terasology.math.TeraMath;
import org.terasology.rendering.assets.GLSLShaderProgramInstance;
import org.terasology.rendering.assets.Material;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;
import org.terasology.rendering.world.WorldRenderer;

import com.bulletphysics.linearmath.Transform;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.LinkedList;
import java.util.List;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.input.events.KeyDownEvent;
import org.terasology.logic.LocalPlayer;

/**
 * TODO: This should be made generic (no explicit shader or mesh) and ported
 * directly into WorldRenderer? Later note: some GelCube functionality moved to
 * a mod
 *
 * @author Immortius <immortius@gmail.com>
 */
@RegisterComponentSystem(headedOnly = true)
public class MeshRenderer implements RenderSystem, EventHandlerSystem {
    private static final Logger logger = LoggerFactory.getLogger(MeshRenderer.class);
    private Mesh gelatinousCubeMesh;
    private WorldRenderer worldRenderer;
    private NearestSortingList opaqueMeshSorter = new NearestSortingList();
    private NearestSortingList gelatinousMeshSorter = new NearestSortingList();
    private List<Material> materials = new LinkedList();

    private Multimap<Material, EntityRef> opaqueMesh = ArrayListMultimap.create();
    //private Multimap<Material, EntityRef> translucentMesh = HashMultimap.create();
    private Set<EntityRef> gelatinous = Sets.newHashSet();
    //private int batchVertexBuffer;
    //private int batchIndexBuffer;
    private boolean batch = false;
    private boolean renderNearest = true;
    private int maxOpaqueMeshesWhenSorting = 20;
    private int maxGenlatinousMeshesWhenSorting = 20;
    public int lastRendered;

    @Override
    public void initialise() {
        worldRenderer = CoreRegistry.get(WorldRenderer.class);

        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), 0.8f, 0.8f, 0.6f, 0f, 0f, 0f);
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1.0f, 1.0f, 1.0f, 0.6f), 1.0f, 1.0f, 0.8f, 0f, 0f, 0f);
        gelatinousCubeMesh = tessellator.generateMesh();

        //batchVertexBuffer = VertexBufferObjectManager.getInstance().getVboId();
        //batchIndexBuffer = VertexBufferObjectManager.getInstance().getVboId();
        EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();
        opaqueMeshSorter.initialize(playerEntity);
        gelatinousMeshSorter.initialize(playerEntity);
    }

    @Override
    public void shutdown() {
        opaqueMeshSorter.stop();
        gelatinousMeshSorter.stop();
    }

    /**
     * Used for toggling the rendering mode between nearest and all.
     * TODO: replace this keylistener with a text command or allow the user to
     * change the command
     * @param event
     * @param entity 
     */
    @ReceiveEvent(components = {LocalPlayerComponent.class})
    public void onKeyDown(KeyDownEvent event, EntityRef entity) {
        if (event.getKeyCharacter() == ';') {
            renderNearest = !renderNearest;
            if(! renderNearest) {
                gelatinousMeshSorter.stop();
                opaqueMeshSorter.stop();
            } else {
                EntityRef playerEntity = CoreRegistry.get(LocalPlayer.class).getEntity();
                opaqueMeshSorter.initialize(playerEntity);
                gelatinousMeshSorter.initialize(playerEntity);
            }
        }
    }

     
    @ReceiveEvent(components = {MeshComponent.class})
    public void onNewMesh(AddComponentEvent event, EntityRef entity) {
        if (entity.getComponent(DroppedItemTypeComponent.class) != null) {
            return;
        }
        MeshComponent meshComp = entity.getComponent(MeshComponent.class);
        if (meshComp.renderType == MeshComponent.RenderType.GelatinousCube) {
            gelatinous.add(entity);
            gelatinousMeshSorter.add(entity);
        } else {
            opaqueMesh.put(meshComp.material, entity);
            opaqueMeshSorter.add(entity);
        }
    }

    @ReceiveEvent(components = {MeshComponent.class})
    public void onDestroyMesh(RemovedComponentEvent event, EntityRef entity) {
        if (entity.getComponent(DroppedItemTypeComponent.class) != null) {
            return;
        }
        MeshComponent meshComponent = entity.getComponent(MeshComponent.class);
        if (meshComponent.renderType == MeshComponent.RenderType.GelatinousCube) {
            gelatinous.remove(entity);
            gelatinousMeshSorter.remove(entity);
        } else {
            opaqueMesh.remove(meshComponent.material, entity);
            opaqueMeshSorter.remove(entity);
        }
    }

    @Override
    public void renderAlphaBlend() {
        if(renderNearest) {
            renderAlphaBlendSorted();
        } else {
            renderAlphaBlendAll();
        }
    }

    public void render() {
        if (renderNearest) {
            renderNearest();
        } else {
            renderAll();
        }
    }

    @Override
    public void renderOpaque() {
        render();
    }

//    private void renderBatch(TFloatList vertexData, TIntList indexData) {
//        if (vertexData.size() == 0 || indexData.size() == 0) return;
//
//        PerformanceMonitor.startActivity("BatchRenderMesh");
//        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexData.size());
//        vertexBuffer.put(vertexData.toArray());
//        vertexBuffer.flip();
//        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, batchVertexBuffer);
//        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_DYNAMIC_DRAW);
//
//        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indexData.size());
//        indexBuffer.put(indexData.toArray());
//        indexBuffer.flip();
//        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, batchIndexBuffer);
//        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_DYNAMIC_DRAW);
//
//        glPushMatrix();
//
//        glEnableClientState(GL_VERTEX_ARRAY);
//        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
//        glEnableClientState(GL_COLOR_ARRAY);
//        glEnableClientState(GL_NORMAL_ARRAY);
//
//        //GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, batchVertexBuffer);
//        //GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, batchIndexBuffer);
//
//        glVertexPointer(Mesh.VERTEX_SIZE, GL11.GL_FLOAT, 15 * 4, 0);
//        GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
//        glTexCoordPointer(Mesh.TEX_COORD_0_SIZE, GL11.GL_FLOAT, 15 * 4, 4 * 3);
//        GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
//        glTexCoordPointer(Mesh.TEX_COORD_1_SIZE, GL11.GL_FLOAT, 15 * 4, 4 * 5);
//        glColorPointer(Mesh.COLOR_SIZE, GL11.GL_FLOAT, 15 * 4, 4 * 11);
//        glNormalPointer(GL11.GL_FLOAT, 15 * 4, 4 * 8);
//
//        GL12.glDrawRangeElements(GL11.GL_TRIANGLES, 0, indexData.size(), indexData.size(), GL_UNSIGNED_INT, 0);
//
//        glDisableClientState(GL_NORMAL_ARRAY);
//        glDisableClientState(GL_COLOR_ARRAY);
//        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
//        glDisableClientState(GL_VERTEX_ARRAY);
//
//        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
//        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
//        glPopMatrix();
//        PerformanceMonitor.endActivity();
//    }
    @Override
    public void renderOverlay() {
    }

    @Override
    public void renderFirstPerson() {
    }

    @Override
    public void renderShadows() {
    }

    /**
     * This is the old render method, as written by begla. Should be removed
     * when the material map is removed from this class.
     */
    private void renderAll() {
        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();

        Quat4f worldRot = new Quat4f();
        Vector3f worldPos = new Vector3f();
        //AxisAngle4f rot = new AxisAngle4f();
        Matrix4f matrixWorldSpace = new Matrix4f();
        Transform transWorldSpace = new Transform();
        Matrix4f matrixCameraSpace = new Matrix4f();
        //Transform normTrans = new Transform();

        FloatBuffer tempMatrixBuffer44 = BufferUtils.createFloatBuffer(16);
        FloatBuffer tempMatrixBuffer33 = BufferUtils.createFloatBuffer(12);

        for (Material material : opaqueMesh.keys()) {
            Mesh lastMesh = null;
            material.enable();

            material.getShaderProgramInstance().setBoolean("textured", true);

            material.getShaderProgramInstance().setFloat("sunlight", 1.0f);
            material.getShaderProgramInstance().setFloat("blockLight", 1.0f);

            material.getShaderProgramInstance().setMatrix4("projectionMatrix", worldRenderer.getActiveCamera().getProjectionMatrix());
            material.bindTextures();

            lastRendered = opaqueMesh.get(material).size();

            // Batching
            /*
             * TFloatList vertexData = new TFloatArrayList(); TIntList indexData
             * = new TIntArrayList(); int indexOffset = 0; float[] openglMat =
             * new float[16]; FloatBuffer mBuffer =
             * BufferUtils.createFloatBuffer(16);
             */

            for (EntityRef entity : opaqueMesh.get(material)) {
                //Basic rendering
                if (!batch) {
                    MeshComponent meshComp = entity.getComponent(MeshComponent.class);
                    LocationComponent location = entity.getComponent(LocationComponent.class);

                    if (location == null || meshComp.mesh == null) {
                        continue;
                    }
                    if (meshComp.mesh.isDisposed()) {
                        logger.error("Attempted to render disposed mesh");
                        continue;
                    }

                    location.getWorldRotation(worldRot);
                    location.getWorldPosition(worldPos);

                    float worldScale = location.getWorldScale();

                    matrixWorldSpace.set(worldRot, worldPos, worldScale);
                    transWorldSpace.set(matrixWorldSpace);

                    Vector3f worldPositionCameraSpace = new Vector3f();
                    worldPositionCameraSpace.sub(worldPos, cameraPosition);
                    matrixCameraSpace.set(worldRot, worldPositionCameraSpace, worldScale);

                    AABB aabb = meshComp.mesh.getAABB().transform(transWorldSpace);

                    boolean visible = worldRenderer.isAABBVisible(aabb);
                    if (visible) {
                        if (meshComp.mesh != lastMesh) {
                            if (lastMesh != null) {
                                lastMesh.postRender();
                            }
                            lastMesh = meshComp.mesh;
                            meshComp.mesh.preRender();
                        }

                        Matrix4f modelViewMatrix = TeraMath.calcModelViewMatrix(worldRenderer.getActiveCamera().getViewMatrix(), matrixCameraSpace);
                        TeraMath.matrixToFloatBuffer(modelViewMatrix, tempMatrixBuffer44);

                        material.getShaderProgramInstance().setMatrix4("worldViewMatrix", tempMatrixBuffer44);

                        TeraMath.matrixToFloatBuffer(TeraMath.calcNormalMatrix(modelViewMatrix), tempMatrixBuffer33);
                        material.getShaderProgramInstance().setMatrix3("normalMatrix", tempMatrixBuffer33);

                        material.getShaderProgramInstance().setFloat("sunlight", worldRenderer.getSunlightValueAt(worldPos));
                        material.getShaderProgramInstance().setFloat("blockLight", worldRenderer.getBlockLightValueAt(worldPos));

                        meshComp.mesh.doRender();
                    }
                } else {
                    // TODO: Do this
                    throw new RuntimeException("Batching has to be overhauled to use shader parameters for matrices instead of the OGL matrix stack");

                    /*
                     * MeshComponent meshComp =
                     * entity.getComponent(MeshComponent.class);
                     * LocationComponent location =
                     * entity.getComponent(LocationComponent.class); if
                     * (location == null) continue;
                     *
                     * location.getWorldRotation(worldRot);
                     * location.getWorldPosition(worldPos); float worldScale =
                     * location.getWorldScale(); matrixWorldSpace.set(worldRot,
                     * worldPos, worldScale);
                     * transWorldSpace.set(matrixWorldSpace);
                     * matrixWorldSpace.set(worldRot, new Vector3f(), 1);
                     * normTrans.set(matrixWorldSpace); AABB aabb =
                     * meshComp.mesh.getAABB().transform(transWorldSpace);
                     *
                     * final boolean visible =
                     * worldRenderer.isAABBVisible(aabb); if (visible) {
                     * indexOffset = meshComp.mesh.addToBatch(transWorldSpace,
                     * normTrans, vertexData, indexData, indexOffset); }
                     *
                     * if (indexOffset > 100) { renderBatch(vertexData,
                     * indexData); vertexData.clear(); indexData.clear(); }
                     */
                }
            }
            if (lastMesh != null) {
                lastMesh.postRender();
            }

            /*
             * if (batch) { renderBatch(vertexData, indexData); }
             */
        }
    }

    /**
     * The new render method, written by XanHou. Diff with old method: - Maximum
     * amount of meshes to render. - slightly worse performance on same amount
     * of meshes to render.
     */
    private void renderNearest() {
        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();

        Quat4f worldRot = new Quat4f();
        Vector3f worldPos = new Vector3f();
        Matrix4f matrixWorldSpace = new Matrix4f();
        Transform transWorldSpace = new Transform();
        Matrix4f matrixCameraSpace = new Matrix4f();

        FloatBuffer tempMatrixBuffer44 = BufferUtils.createFloatBuffer(16);
        FloatBuffer tempMatrixBuffer33 = BufferUtils.createFloatBuffer(12);

        EntityRef[] nearest = opaqueMeshSorter.getNearest(maxOpaqueMeshesWhenSorting);
        /**
         * Get the materials to iterate over.
         */
        materials.clear();
        for (EntityRef entity : nearest) {
            MeshComponent component = entity.getComponent(MeshComponent.class);
            if (component.mesh == null) {
                continue;
            }
            if (component.mesh.isDisposed()) {
                logger.error("Attempted to render disposed mesh");
                continue;
            }
            Material material = component.material;
            if (!materials.contains(material)) {
                materials.add(material);
            }
        }


        lastRendered = maxOpaqueMeshesWhenSorting;

        for (Material material : materials) {
            Mesh lastMesh = null;
            material.enable();

            material.getShaderProgramInstance().setBoolean("textured", true);

            material.getShaderProgramInstance().setFloat("sunlight", 1.0f);
            material.getShaderProgramInstance().setFloat("blockLight", 1.0f);

            material.getShaderProgramInstance().setMatrix4("projectionMatrix", worldRenderer.getActiveCamera().getProjectionMatrix());
            material.bindTextures();


            for (EntityRef entity : nearest) {
                MeshComponent meshComp = entity.getComponent(MeshComponent.class);
                if(meshComp.mesh == null) {
                    continue;
                }
                //Basic rendering
                if (meshComp.material == material) {
                    LocationComponent location = entity.getComponent(LocationComponent.class);

                    if (location == null) {
                        continue;
                    }
                    if (meshComp.mesh.isDisposed()) {
                        continue;
                    }

                    location.getWorldRotation(worldRot);
                    location.getWorldPosition(worldPos);

                    float worldScale = location.getWorldScale();

                    matrixWorldSpace.set(worldRot, worldPos, worldScale);
                    transWorldSpace.set(matrixWorldSpace);

                    Vector3f worldPositionCameraSpace = new Vector3f();
                    worldPositionCameraSpace.sub(worldPos, cameraPosition);
                    matrixCameraSpace.set(worldRot, worldPositionCameraSpace, worldScale);

                    AABB aabb = meshComp.mesh.getAABB().transform(transWorldSpace);

                    boolean visible = worldRenderer.isAABBVisible(aabb);
                    if (visible) {
                        if (meshComp.mesh != lastMesh) {
                            if (lastMesh != null) {
                                lastMesh.postRender();
                            }
                            lastMesh = meshComp.mesh;
                            meshComp.mesh.preRender();
                        }

                        Matrix4f modelViewMatrix = TeraMath.calcModelViewMatrix(worldRenderer.getActiveCamera().getViewMatrix(), matrixCameraSpace);
                        TeraMath.matrixToFloatBuffer(modelViewMatrix, tempMatrixBuffer44);

                        material.getShaderProgramInstance().setMatrix4("worldViewMatrix", tempMatrixBuffer44);

                        TeraMath.matrixToFloatBuffer(TeraMath.calcNormalMatrix(modelViewMatrix), tempMatrixBuffer33);
                        material.getShaderProgramInstance().setMatrix3("normalMatrix", tempMatrixBuffer33);

                        material.getShaderProgramInstance().setFloat("sunlight", worldRenderer.getSunlightValueAt(worldPos));
                        material.getShaderProgramInstance().setFloat("blockLight", worldRenderer.getBlockLightValueAt(worldPos));

                        meshComp.mesh.doRender();
                    }
                }
            }
            if (lastMesh != null) {
                lastMesh.postRender();
            }
        }
    }

    private void renderAlphaBlendAll() {
        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();
        GLSLShaderProgramInstance shader = ShaderManager.getInstance().getShaderProgramInstance("gelatinousCube");
        shader.enable();

        for (EntityRef entity : gelatinous) {
            MeshComponent meshComp = entity.getComponent(MeshComponent.class);
            LocationComponent location = entity.getComponent(LocationComponent.class);
            if (location == null) {
                continue;
            }

            Quat4f worldRot = location.getWorldRotation();
            Vector3f worldPos = location.getWorldPosition();
            float worldScale = location.getWorldScale();
            AABB aabb = gelatinousCubeMesh.getAABB().transform(worldRot, worldPos, worldScale);
            if (worldRenderer.isAABBVisible(aabb)) {
                glPushMatrix();

                glTranslated(worldPos.x - cameraPosition.x, worldPos.y - cameraPosition.y, worldPos.z - cameraPosition.z);
                AxisAngle4f rot = new AxisAngle4f();
                rot.set(worldRot);
                glRotatef(TeraMath.RAD_TO_DEG * rot.angle, rot.x, rot.y, rot.z);
                glScalef(worldScale, worldScale, worldScale);

                shader.setFloat4("colorOffset", meshComp.color.x, meshComp.color.y, meshComp.color.z, meshComp.color.w);
                shader.setFloat("light", worldRenderer.getRenderingLightValueAt(worldPos));

                gelatinousCubeMesh.render();

                glPopMatrix();
            }
        }
    }

    private void renderAlphaBlendSorted() {
        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();
        GLSLShaderProgramInstance shader = ShaderManager.getInstance().getShaderProgramInstance("gelatinousCube");
        shader.enable();
        EntityRef[] nearest = gelatinousMeshSorter.getNearest(maxGenlatinousMeshesWhenSorting);

        for (EntityRef entity : nearest) {
            MeshComponent meshComp = entity.getComponent(MeshComponent.class);
            LocationComponent location = entity.getComponent(LocationComponent.class);
            if (location == null) {
                continue;
            }

            Quat4f worldRot = location.getWorldRotation();
            Vector3f worldPos = location.getWorldPosition();
            float worldScale = location.getWorldScale();
            AABB aabb = gelatinousCubeMesh.getAABB().transform(worldRot, worldPos, worldScale);
            if (worldRenderer.isAABBVisible(aabb)) {
                glPushMatrix();

                glTranslated(worldPos.x - cameraPosition.x, worldPos.y - cameraPosition.y, worldPos.z - cameraPosition.z);
                AxisAngle4f rot = new AxisAngle4f();
                rot.set(worldRot);
                glRotatef(TeraMath.RAD_TO_DEG * rot.angle, rot.x, rot.y, rot.z);
                glScalef(worldScale, worldScale, worldScale);

                shader.setFloat4("colorOffset", meshComp.color.x, meshComp.color.y, meshComp.color.z, meshComp.color.w);
                shader.setFloat("light", worldRenderer.getRenderingLightValueAt(worldPos));

                gelatinousCubeMesh.render();

                glPopMatrix();
            }
        }
    }
}
