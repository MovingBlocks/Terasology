/*
 * Copyright 2013 Moving Blocks
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
import com.google.common.collect.Sets;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.PickupComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.logic.manager.VertexBufferObjectManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.AABB;
import org.terasology.math.TeraMath;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.opengl.OpenGLMaterial;
import org.terasology.rendering.opengl.OpenGLMesh;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;
import org.terasology.rendering.shader.ShaderProgram;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.Set;

import static org.lwjgl.opengl.GL11.GL_COLOR_ARRAY;
import static org.lwjgl.opengl.GL11.GL_NORMAL_ARRAY;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_COORD_ARRAY;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glColorPointer;
import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glMultMatrix;
import static org.lwjgl.opengl.GL11.glNormalPointer;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTexCoordPointer;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glVertexPointer;

/**
 * TODO: This should be made generic (no explicit shader or mesh) and ported directly into WorldRenderer? Later note: some GelCube functionality moved to a mod
 *
 * @author Immortius <immortius@gmail.com>
 */
@RegisterSystem(RegisterMode.CLIENT)
public class MeshRenderer implements RenderSystem {
    private static final Logger logger = LoggerFactory.getLogger(MeshRenderer.class);

    @In
    private NetworkSystem network;

    @In
    private LocalPlayer localPlayer;

    private Mesh gelatinousCubeMesh;
    private WorldRenderer worldRenderer;

    private SetMultimap<Material, EntityRef> opaqueMesh = HashMultimap.create();
    private SetMultimap<Material, EntityRef> translucentMesh = HashMultimap.create();
    private Set<EntityRef> gelatinous = Sets.newHashSet();
    private Map<EntityRef, Material> opaqueEntities = Maps.newHashMap();
    private Map<EntityRef, Material> translucentEntities = Maps.newHashMap();


    private int batchVertexBuffer;
    private int batchIndexBuffer;

    boolean batch = false;

    public int lastRendered;

    @Override
    public void initialise() {
        worldRenderer = CoreRegistry.get(WorldRenderer.class);

        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), 0.8f, 0.8f, 0.6f, 0f, 0f, 0f);
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1.0f, 1.0f, 1.0f, 0.6f), 1.0f, 1.0f, 0.8f, 0f, 0f, 0f);
        gelatinousCubeMesh = tessellator.generateMesh();

        batchVertexBuffer = VertexBufferObjectManager.getInstance().getVboId();
        batchIndexBuffer = VertexBufferObjectManager.getInstance().getVboId();
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {MeshComponent.class, LocationComponent.class})
    public void onNewMesh(OnActivatedComponent event, EntityRef entity) {
        addMesh(entity);
    }

    private void addMesh(EntityRef entity) {
        MeshComponent meshComp = entity.getComponent(MeshComponent.class);
        // Don't render if hidden from owner (need to improve for third person)
        if (meshComp.hideFromOwner) {
            ClientComponent owner = network.getOwnerEntity(entity).getComponent(ClientComponent.class);
            if (owner != null && owner.local) {
                return;
            }
        }
        if (meshComp.renderType == MeshComponent.RenderType.GelatinousCube) {
            gelatinous.add(entity);
        } else {
            opaqueMesh.put(meshComp.material, entity);
            opaqueEntities.put(entity, meshComp.material);
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class, MeshComponent.class})
    public void onLocalMesh(OnChangedComponent event, EntityRef entity) {
        removeMesh(entity);
        addMesh(entity);
    }

    @ReceiveEvent(components = {MeshComponent.class})
    public void onChangeMesh(OnChangedComponent event, EntityRef entity) {
        removeMesh(entity);
        addMesh(entity);
    }

    private void removeMesh(EntityRef entity) {
        if (!gelatinous.remove(entity)) {
            Material mat = opaqueEntities.remove(entity);
            if (mat != null) {
                opaqueMesh.remove(mat, entity);
            } else {
                mat = translucentEntities.remove(entity);
                if (mat != null) {
                    translucentMesh.remove(mat, entity);
                }
            }
        }
    }

    @ReceiveEvent(components = {MeshComponent.class, LocationComponent.class})
    public void onDestroyMesh(BeforeDeactivateComponent event, EntityRef entity) {
        if (entity.getComponent(PickupComponent.class) != null) {
            return;
        }
        removeMesh(entity);
    }

    @Override
    public void renderTransparent() {

        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("gelatinousCube");
        shader.enable();

        for (EntityRef entity : gelatinous) {
            MeshComponent meshComp = entity.getComponent(MeshComponent.class);
            LocationComponent location = entity.getComponent(LocationComponent.class);
            if (location == null) continue;

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

    @Override
    public void renderOpaque() {
        boolean carryingTorch = CoreRegistry.get(LocalPlayer.class).isCarryingTorch();
        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();

        Quat4f worldRot = new Quat4f();
        Vector3f worldPos = new Vector3f();
        AxisAngle4f rot = new AxisAngle4f();
        Matrix4f matrix = new Matrix4f();
        Transform trans = new Transform();
        Transform normTrans = new Transform();

        glPushMatrix();
        glTranslated(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);

        for (Material material : opaqueMesh.keys()) {
            OpenGLMesh lastMesh = null;
            OpenGLMaterial openglMat = (OpenGLMaterial) material;
            openglMat.enable();
            openglMat.setInt("carryingTorch", carryingTorch ? 1 : 0);
            openglMat.setFloat("light", 1);
            openglMat.bindTextures();
            lastRendered = opaqueMesh.get(material).size();

            // Batching
            TFloatList vertexData = new TFloatArrayList();
            TIntList indexData = new TIntArrayList();
            int indexOffset = 0;
            float[] openglMatrix = new float[16];
            FloatBuffer mBuffer = BufferUtils.createFloatBuffer(16);

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
                    matrix.set(worldRot, worldPos, worldScale);
                    trans.set(matrix);
                    AABB aabb = meshComp.mesh.getAABB().transform(trans);
                    if (worldRenderer.isAABBVisible(aabb)) {
                        if (meshComp.mesh != lastMesh) {
                            if (lastMesh != null) {
                                lastMesh.postRender();
                            }
                            lastMesh = (OpenGLMesh) meshComp.mesh;
                            lastMesh.preRender();
                        }
                        glPushMatrix();
                        trans.getOpenGLMatrix(openglMatrix);
                        mBuffer.put(openglMatrix);
                        mBuffer.flip();
                        glMultMatrix(mBuffer);

                        material.setFloat("light", worldRenderer.getRenderingLightValueAt(worldPos));

                        lastMesh.doRender();

                        glPopMatrix();
                    }
                } else {
                    // Batching
                    MeshComponent meshComp = entity.getComponent(MeshComponent.class);
                    LocationComponent location = entity.getComponent(LocationComponent.class);
                    if (location == null) continue;

                    location.getWorldRotation(worldRot);
                    location.getWorldPosition(worldPos);
                    float worldScale = location.getWorldScale();
                    matrix.set(worldRot, worldPos, worldScale);
                    trans.set(matrix);
                    matrix.set(worldRot, new Vector3f(), 1);
                    normTrans.set(matrix);
                    AABB aabb = meshComp.mesh.getAABB().transform(trans);

                    if (worldRenderer.isAABBVisible(aabb)) {
                        indexOffset = ((OpenGLMesh) meshComp.mesh).addToBatch(trans, normTrans, vertexData, indexData, indexOffset);
                    }

                    if (indexOffset > 100) {
                        renderBatch(vertexData, indexData);
                        vertexData.clear();
                        indexData.clear();
                    }
                }
            }
            if (lastMesh != null) {
                lastMesh.postRender();
            }

            if (batch) {
                renderBatch(vertexData, indexData);
            }
        }

        glPopMatrix();

        /*for (EntityRef entity : manager.listEntities(MeshComponent.class)) {
            MeshComponent meshComp = entity.getComponent(MeshComponent.class);
            if (meshComp.renderType != MeshComponent.RenderType.Normal || meshComp.mesh == null) continue;

            LocationComponent location = entity.getComponent(LocationComponent.class);
            if (location == null) continue;



            Quat4f worldRot = location.getWorldRotation();
            Vector3f worldPos = location.getWorldPosition();
            float worldScale = location.getWorldScale();
            AABB aabb = meshComp.mesh.getAABB().transform(worldRot, worldPos, worldScale);

            if (worldRenderer.isAABBVisible(aabb)) {
                glPushMatrix();

                glTranslated(worldPos.x - cameraPosition.x, worldPos.y - cameraPosition.y, worldPos.z - cameraPosition.z);
                AxisAngle4f rot = new AxisAngle4f();
                rot.set(location.getWorldRotation());
                glRotatef(TeraMath.RAD_TO_DEG * rot.angle, rot.x, rot.y, rot.z);
                glScalef(worldScale, worldScale, worldScale);

                meshComp.material.enable();
                meshComp.material.setFloat("light", worldRenderer.getRenderingLightValueAt(worldPos));
                meshComp.material.setInt("carryingTorch", carryingTorch ? 1 : 0);
                meshComp.material.bindTextures();
                meshComp.mesh.render();

                glPopMatrix();
            }
        }*/
    }

    private void renderBatch(TFloatList vertexData, TIntList indexData) {
        if (vertexData.size() == 0 || indexData.size() == 0) return;

        PerformanceMonitor.startActivity("BatchRenderMesh");
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexData.size());
        vertexBuffer.put(vertexData.toArray());
        vertexBuffer.flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, batchVertexBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_DYNAMIC_DRAW);

        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indexData.size());
        indexBuffer.put(indexData.toArray());
        indexBuffer.flip();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, batchIndexBuffer);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_DYNAMIC_DRAW);

        glPushMatrix();

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnableClientState(GL_NORMAL_ARRAY);

        //GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, batchVertexBuffer);
        //GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, batchIndexBuffer);

        glVertexPointer(Mesh.VERTEX_SIZE, GL11.GL_FLOAT, 15 * 4, 0);
        GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
        glTexCoordPointer(Mesh.TEX_COORD_0_SIZE, GL11.GL_FLOAT, 15 * 4, 4 * 3);
        GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
        glTexCoordPointer(Mesh.TEX_COORD_1_SIZE, GL11.GL_FLOAT, 15 * 4, 4 * 5);
        glColorPointer(Mesh.COLOR_SIZE, GL11.GL_FLOAT, 15 * 4, 4 * 11);
        glNormalPointer(GL11.GL_FLOAT, 15 * 4, 4 * 8);

        GL12.glDrawRangeElements(GL11.GL_TRIANGLES, 0, indexData.size(), indexData.size(), GL_UNSIGNED_INT, 0);

        glDisableClientState(GL_NORMAL_ARRAY);
        glDisableClientState(GL_COLOR_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        glPopMatrix();
        PerformanceMonitor.endActivity();
    }

    @Override
    public void renderOverlay() {
    }

    @Override
    public void renderFirstPerson() {
    }
}
