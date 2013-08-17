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

import com.bulletphysics.linearmath.QuaternionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.TeraMath;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.assets.animation.MeshAnimationFrame;
import org.terasology.rendering.assets.skeletalmesh.Bone;
import org.terasology.rendering.opengl.OpenGLSkeletalMesh;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.util.List;

/**
 * @author Immortius
 */
@RegisterSystem(RegisterMode.CLIENT)
public class SkeletonRenderer implements RenderSystem, UpdateSubscriberSystem {

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
    public void newSkeleton(OnActivatedComponent event, EntityRef entity) {
        SkeletalMeshComponent skeleton = entity.getComponent(SkeletalMeshComponent.class);
        if (skeleton.mesh == null) {
            return;
        }

        if (skeleton.boneEntities == null) {
            skeleton.boneEntities = Maps.newHashMap();
            for (Bone bone : skeleton.mesh.getBones()) {
                LocationComponent loc = new LocationComponent();
                loc.setLocalPosition(bone.getLocalPosition());
                loc.setLocalRotation(bone.getLocalRotation());
                EntityRef boneEntity = entityManager.create(loc);
                EntityRef parent = (bone.getParent() != null) ? skeleton.boneEntities.get(bone.getParent().getName()) : entity;
                Location.attachChild(parent, boneEntity);
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
        for (EntityRef entity : entityManager.getEntitiesWith(SkeletalMeshComponent.class, LocationComponent.class)) {
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
                        MeshAnimationFrame frame = skeletalMeshComp.animation.getFrame(skeletalMeshComp.animation.getFrameCount() - 1);
                        updateSkeleton(skeletalMeshComp, frame, frame, 1.0f);
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
        Quat4f inverseWorldRot = new Quat4f();
        Matrix4f matrixCameraSpace = new Matrix4f();

        FloatBuffer tempMatrixBuffer44 = BufferUtils.createFloatBuffer(16);
        FloatBuffer tempMatrixBuffer33 = BufferUtils.createFloatBuffer(12);
        ;

        for (EntityRef entity : entityManager.getEntitiesWith(SkeletalMeshComponent.class, LocationComponent.class)) {
            SkeletalMeshComponent skeletalMesh = entity.getComponent(SkeletalMeshComponent.class);
            if (skeletalMesh.mesh == null || skeletalMesh.material == null) {
                continue;
            }
            skeletalMesh.material.enable();
            skeletalMesh.material.setFloat("sunlight", 1.0f, true);
            skeletalMesh.material.setFloat("blockLight", 1.0f, true);

            skeletalMesh.material.setMatrix4("projectionMatrix", worldRenderer.getActiveCamera().getProjectionMatrix());
            skeletalMesh.material.bindTextures();

            LocationComponent location = entity.getComponent(LocationComponent.class);

            location.getWorldRotation(worldRot);
            inverseWorldRot.inverse(worldRot);
            location.getWorldPosition(worldPos);

            Vector3f worldPositionCameraSpace = new Vector3f();
            worldPositionCameraSpace.sub(worldPos, cameraPosition);

            float worldScale = location.getWorldScale();
            matrixCameraSpace.set(worldRot, worldPositionCameraSpace, worldScale);

            Matrix4f modelViewMatrix = TeraMath.calcModelViewMatrix(worldRenderer.getActiveCamera().getViewMatrix(), matrixCameraSpace);
            TeraMath.matrixToFloatBuffer(modelViewMatrix, tempMatrixBuffer44);

            skeletalMesh.material.setMatrix4("worldViewMatrix", tempMatrixBuffer44, true);

            TeraMath.matrixToFloatBuffer(TeraMath.calcNormalMatrix(modelViewMatrix), tempMatrixBuffer33);
            skeletalMesh.material.setMatrix3("normalMatrix", tempMatrixBuffer33, true);

            skeletalMesh.material.setFloat("sunlight", worldRenderer.getSunlightValueAt(worldPos), true);
            skeletalMesh.material.setFloat("blockLight", worldRenderer.getBlockLightValueAt(worldPos), true);

            // TODO: Add frustum culling here
            List<Vector3f> bonePositions = Lists.newArrayListWithCapacity(skeletalMesh.mesh.getVertexCount());
            List<Quat4f> boneRotations = Lists.newArrayListWithCapacity(skeletalMesh.mesh.getVertexCount());
            for (Bone bone : skeletalMesh.mesh.getBones()) {
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
            ((OpenGLSkeletalMesh) skeletalMesh.mesh).render(bonePositions, boneRotations);
        }
    }

    @Override
    public void renderAlphaBlend() {
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

}
