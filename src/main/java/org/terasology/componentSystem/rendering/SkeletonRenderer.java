/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.componentSystem.rendering;

import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;
import com.google.common.collect.Lists;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.terasology.componentSystem.RenderSystem;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.rendering.MeshComponent;
import org.terasology.components.rendering.SkeletalMeshComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.math.AABB;
import org.terasology.math.TeraMath;
import org.terasology.rendering.assets.Material;
import org.terasology.rendering.assets.skeletalmesh.Bone;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.opengl.GL11.glAccum;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glMultMatrix;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glVertex3f;

/**
 * @author Immortius
 */
@RegisterComponentSystem
public class SkeletonRenderer implements RenderSystem, EventHandlerSystem {

    private Logger logger = Logger.getLogger(getClass().getName());
    private EntityManager entityManager;
    private WorldRenderer worldRenderer;

    @Override
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        worldRenderer = CoreRegistry.get(WorldRenderer.class);
    }

    @Override
    public void shutdown() {

    }

    @ReceiveEvent(components = { SkeletalMeshComponent.class, LocationComponent.class} )
    public void newSkeleton(AddComponentEvent event, EntityRef entity) {
        SkeletalMeshComponent skeleton = entity.getComponent(SkeletalMeshComponent.class);
        if (skeleton.mesh == null) {
            return;
        }

        if (skeleton.boneEntities == null) {
            skeleton.boneEntities = Lists.newArrayList();
            EntityRef rootBone = addBoneEntities(skeleton.mesh.getRootBone(), entity, skeleton);
            skeleton.rootBone = rootBone;
        }
        entity.saveComponent(skeleton);
    }

    private EntityRef addBoneEntities(Bone rootBone, EntityRef toEntity, SkeletalMeshComponent skeletalMesh) {
        LocationComponent loc = new LocationComponent();
        loc.setLocalPosition(rootBone.getLocalPosition());
        EntityRef boneEntity = entityManager.create(loc);

        LocationComponent parentLoc = toEntity.getComponent(LocationComponent.class);
        parentLoc.addChild(boneEntity, toEntity);
        toEntity.saveComponent(parentLoc);

        skeletalMesh.boneEntities.add(boneEntity);
        for (Bone child : rootBone.getChildren()) {
            addBoneEntities(child, boneEntity, skeletalMesh);
        }
        return boneEntity;
    }

    @Override
    public void renderOpaque() {
        boolean carryingTorch = CoreRegistry.get(LocalPlayer.class).isCarryingTorch();
        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();

        Quat4f worldRot = new Quat4f();
        Vector3f worldPos = new Vector3f();
        Matrix4f matrix = new Matrix4f();
        Transform trans = new Transform();
        Transform normTrans = new Transform();

        glPushMatrix();
        glTranslated(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);

        for (EntityRef entity : entityManager.iteratorEntities(SkeletalMeshComponent.class, LocationComponent.class)) {
            SkeletalMeshComponent skeletalMesh = entity.getComponent(SkeletalMeshComponent.class);
            if (skeletalMesh.mesh == null || skeletalMesh.material == null) {
                continue;
            }
            skeletalMesh.material.enable();
            skeletalMesh.material.setInt("carryingTorch", carryingTorch ? 1 : 0);
            skeletalMesh.material.setFloat("light", 1);
            skeletalMesh.material.bindTextures();

            float[] openglMat = new float[16];
            FloatBuffer mBuffer = BufferUtils.createFloatBuffer(16);
            LocationComponent location = entity.getComponent(LocationComponent.class);

            location.getWorldRotation(worldRot);
            location.getWorldPosition(worldPos);
            float worldScale = location.getWorldScale();
            matrix.set(worldRot, worldPos, worldScale);
            trans.set(matrix);

            glPushMatrix();
            trans.getOpenGLMatrix(openglMat);
            mBuffer.put(openglMat);
            mBuffer.flip();
            glMultMatrix(mBuffer);

            skeletalMesh.material.setFloat("light", worldRenderer.getRenderingLightValueAt(worldPos));
            skeletalMesh.mesh.render();
            glPopMatrix();
        }

        glPopMatrix();
    }

    @Override
    public void renderTransparent() {
    }

    @Override
    public void renderOverlay() {
        ShaderManager.getInstance().enableDefault();
        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();
        glPushMatrix();
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        glDisable(GL11.GL_TEXTURE_2D);
        glLineWidth(2);
        glTranslated(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
        for (EntityRef entity : entityManager.iteratorEntities(SkeletalMeshComponent.class, LocationComponent.class)) {
            SkeletalMeshComponent skeletalMesh = entity.getComponent(SkeletalMeshComponent.class);
            renderBone(skeletalMesh.rootBone);
        }
        glColor4f(1.0f, 0.0f, 1.0f, 1.0f);
        for (EntityRef entity : entityManager.iteratorEntities(SkeletalMeshComponent.class, LocationComponent.class)) {
            SkeletalMeshComponent skeletalMesh = entity.getComponent(SkeletalMeshComponent.class);
            renderBoneOrientation(skeletalMesh.rootBone);
        }
        glEnable(GL11.GL_TEXTURE_2D);
        glPopMatrix();
    }

    private void renderBoneOrientation(EntityRef boneEntity) {
        LocationComponent loc = boneEntity.getComponent(LocationComponent.class);
        if (loc == null) {
            return;
        }
        glPushMatrix();
        Vector3f worldPosA = loc.getWorldPosition();
        Quat4f worldRot = loc.getWorldRotation();
        Vector3f offset = new Vector3f(0,0,0.1f);
        QuaternionUtil.quatRotate(worldRot, offset, offset);
        offset.add(worldPosA);

        glBegin(GL11.GL_LINES);
        glVertex3f(worldPosA.x, worldPosA.y, worldPosA.z);
        glVertex3f(offset.x, offset.y, offset.z);
        glEnd();

        for (EntityRef child : loc.getChildren()) {
            renderBoneOrientation(child);
        }
        glPopMatrix();
    }

    private void renderBone(EntityRef boneEntity) {
        LocationComponent loc = boneEntity.getComponent(LocationComponent.class);
        if (loc == null) {
            return;
        }
        LocationComponent parentLoc = loc.getParent().getComponent(LocationComponent.class);
        if (parentLoc != null) {
            glPushMatrix();
            Vector3f worldPosA = loc.getWorldPosition();
            Vector3f worldPosB = parentLoc.getWorldPosition();

            glBegin(GL11.GL_LINES);
            glVertex3f(worldPosA.x, worldPosA.y, worldPosA.z);
            glVertex3f(worldPosB.x, worldPosB.y, worldPosB.z);
            glEnd();

            for (EntityRef child : loc.getChildren()) {
                renderBone(child);
            }
            glPopMatrix();
        }
    }

    @Override
    public void renderFirstPerson() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
