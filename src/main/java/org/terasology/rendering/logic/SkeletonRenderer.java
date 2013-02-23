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

package org.terasology.rendering.logic;

import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.componentSystem.RenderSystem;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.rendering.assets.animation.MeshAnimationFrame;
import org.terasology.rendering.assets.skeletalmesh.Bone;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glMultMatrix;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glVertex3f;

/**
 * @author Immortius
 */
@RegisterComponentSystem
public class SkeletonRenderer implements RenderSystem, EventHandlerSystem, UpdateSubscriberSystem {

    private static final Logger logger = LoggerFactory.getLogger(SkeletonRenderer.class);

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

    @ReceiveEvent(components = {SkeletalMeshComponent.class, LocationComponent.class})
    public void newSkeleton(AddComponentEvent event, EntityRef entity) {
        SkeletalMeshComponent skeleton = entity.getComponent(SkeletalMeshComponent.class);
        if (skeleton.mesh == null) {
            return;
        }

        if (skeleton.boneEntities == null) {
            skeleton.boneEntities = Maps.newHashMap();
            for (Bone bone : skeleton.mesh.bones()) {
                LocationComponent loc = new LocationComponent();
                loc.setLocalPosition(bone.getLocalPosition());
                loc.setLocalRotation(bone.getLocalRotation());
                EntityRef boneEntity = entityManager.create(loc);
                EntityRef parent = (bone.getParent() != null) ? skeleton.boneEntities.get(bone.getParent().getName()) : entity;
                LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
                parentLoc.addChild(boneEntity, parent);
                parent.saveComponent(parentLoc);
                if (bone.getParent() == null) {
                    skeleton.rootBone = boneEntity;
                }
                skeleton.boneEntities.put(bone.getName(), boneEntity);
            }
            entity.saveComponent(skeleton);
        }

    }

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.iteratorEntities(SkeletalMeshComponent.class, LocationComponent.class)) {
            SkeletalMeshComponent skeletalMeshComp = entity.getComponent(SkeletalMeshComponent.class);
            if (skeletalMeshComp.animation != null && skeletalMeshComp.animation.getFrameCount() > 0) {
                skeletalMeshComp.animationTime += delta * skeletalMeshComp.animationRate;
                float framePos = skeletalMeshComp.animationTime / skeletalMeshComp.animation.getTimePerFrame();

                if (skeletalMeshComp.loop) {
                    while ((int) framePos >= skeletalMeshComp.animation.getFrameCount()) {
                        framePos -= skeletalMeshComp.animation.getFrameCount();
                        skeletalMeshComp.animationTime -= skeletalMeshComp.animation.getTimePerFrame() * skeletalMeshComp.animation.getFrameCount();
                    }
                    int frameId = (int) framePos;
                    MeshAnimationFrame frameA = skeletalMeshComp.animation.getFrame(frameId);
                    MeshAnimationFrame frameB = skeletalMeshComp.animation.getFrame((frameId + 1) % skeletalMeshComp.animation.getFrameCount());
                    updateSkeleton(skeletalMeshComp, frameA, frameB, framePos - frameId);
                } else {
                    if ((int) framePos >= skeletalMeshComp.animation.getFrameCount()) {
                        updateSkeleton(skeletalMeshComp, skeletalMeshComp.animation.getFrame(skeletalMeshComp.animation.getFrameCount() - 1), skeletalMeshComp.animation.getFrame(skeletalMeshComp.animation.getFrameCount() - 1), 1.0f);
                        MeshAnimation animation = skeletalMeshComp.animation;
                        skeletalMeshComp.animationTime = 0;
                        skeletalMeshComp.animation = null;
                        entity.send(new AnimEndEvent(animation));
                    } else {
                        int frameId = (int) framePos;
                        MeshAnimationFrame frameA = skeletalMeshComp.animation.getFrame(frameId);
                        MeshAnimationFrame frameB = (frameId + 1 >= skeletalMeshComp.animation.getFrameCount()) ? frameA : skeletalMeshComp.animation.getFrame(frameId + 1);
                        updateSkeleton(skeletalMeshComp, frameA, frameB, framePos - frameId);
                    }
                }
                entity.saveComponent(skeletalMeshComp);
            }
        }
    }

    private void updateSkeleton(SkeletalMeshComponent skeletalMeshComp, MeshAnimationFrame frameA, MeshAnimationFrame frameB, float interpolationVal) {
        Vector3f newPos = new Vector3f();
        Quat4f newRot = new Quat4f();

        for (int i = 0; i < skeletalMeshComp.animation.getBoneCount(); ++i) {
            EntityRef boneEntity = skeletalMeshComp.boneEntities.get(skeletalMeshComp.animation.getBoneName(i));
            if (boneEntity == null) {
                continue;
            }
            LocationComponent boneLoc = boneEntity.getComponent(LocationComponent.class);
            if (boneLoc != null) {

                newPos.interpolate(frameA.getPosition(i), frameB.getPosition(i), interpolationVal);
                boneLoc.setLocalPosition(newPos);
                newRot.interpolate(frameA.getRotation(i), frameB.getRotation(i), interpolationVal);
                newRot.normalize();
                boneLoc.setLocalRotation(newRot);
                boneEntity.saveComponent(boneLoc);
            }
        }
    }

    @Override
    public void renderOpaque() {
        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();

        Quat4f worldRot = new Quat4f();
        Vector3f worldPos = new Vector3f();
        Matrix4f matrix = new Matrix4f();
        Transform trans = new Transform();
        Quat4f inverseWorldRot = new Quat4f();

        glPushMatrix();
        glTranslated(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);

        for (EntityRef entity : entityManager.iteratorEntities(SkeletalMeshComponent.class, LocationComponent.class)) {
            SkeletalMeshComponent skeletalMesh = entity.getComponent(SkeletalMeshComponent.class);
            if (skeletalMesh.mesh == null || skeletalMesh.material == null) {
                continue;
            }
            skeletalMesh.material.enable();
            skeletalMesh.material.setFloat("light", 1);
            skeletalMesh.material.bindTextures();

            float[] openglMat = new float[16];
            FloatBuffer mBuffer = BufferUtils.createFloatBuffer(16);
            LocationComponent location = entity.getComponent(LocationComponent.class);

            location.getWorldRotation(worldRot);
            inverseWorldRot.inverse(worldRot);
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
            List<Vector3f> bonePositions = Lists.newArrayListWithCapacity(skeletalMesh.mesh.getVertexCount());
            List<Quat4f> boneRotations = Lists.newArrayListWithCapacity(skeletalMesh.mesh.getVertexCount());
            for (Bone bone : skeletalMesh.mesh.bones()) {
                EntityRef boneEntity = skeletalMesh.boneEntities.get(bone.getName());
                if (boneEntity == null) {
                    boneEntity = EntityRef.NULL;
                }
                LocationComponent boneLocation = boneEntity.getComponent(LocationComponent.class);
                if (boneLocation != null) {
                    Vector3f pos = boneLocation.getWorldPosition();
                    pos.sub(worldPos);
                    QuaternionUtil.quatRotate(inverseWorldRot, pos, pos);
                    bonePositions.add(pos);
                    Quat4f rot = new Quat4f();
                    rot.mul(inverseWorldRot, boneLocation.getWorldRotation());
                    boneRotations.add(rot);
                } else {
                    logger.warn("Unable to resolve bone \"{}\"", bone.getName());
                    bonePositions.add(new Vector3f());
                    boneRotations.add(new Quat4f());
                }
            }
            skeletalMesh.mesh.render(bonePositions, boneRotations);
            glPopMatrix();
        }

        glPopMatrix();
    }

    @Override
    public void renderTransparent() {
    }

    @Override
    public void renderOverlay() {
        /*ShaderManager.getInstance().enableDefault();
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
        glPopMatrix();*/
    }

    private void renderBoneOrientation(EntityRef boneEntity) {
        LocationComponent loc = boneEntity.getComponent(LocationComponent.class);
        if (loc == null) {
            return;
        }
        glPushMatrix();
        Vector3f worldPosA = loc.getWorldPosition();
        Quat4f worldRot = loc.getWorldRotation();
        Vector3f offset = new Vector3f(0, 0, 0.1f);
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
    }

    @Override
    public void renderShadows() {
    }

}
