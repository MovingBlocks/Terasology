// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.logic;

import com.google.common.collect.Maps;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.RenderSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.location.Location;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.animation.MeshAnimation;
import org.terasology.engine.rendering.assets.animation.MeshAnimationFrame;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.mesh.StandardMeshData;
import org.terasology.engine.rendering.assets.mesh.resource.AllocationType;
import org.terasology.engine.rendering.assets.mesh.resource.DrawingMode;
import org.terasology.engine.rendering.assets.skeletalmesh.Bone;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.joml.geom.AABBf;
import org.terasology.joml.geom.AABBfc;
import org.terasology.nui.Color;

import java.nio.FloatBuffer;
import java.util.Random;

/**
 * Renders the skeletal mesh of entities when the debug setting "renderSkeletons" is active.
 * <p>
 * The entities must have a {@link SkinnedMeshComponent} and a {@link LocationComponent}.
 *
 * @see BoundingBoxRenderer another debug renderer for bounding boxes defined by shape components
 */
@RegisterSystem(RegisterMode.CLIENT)
public class SkinnedMeshRenderer extends BaseComponentSystem implements RenderSystem, UpdateSubscriberSystem {

    private static final Logger logger = LoggerFactory.getLogger(SkinnedMeshRenderer.class);

    @In
    private EntityManager entityManager;

    @In
    private WorldRenderer worldRenderer;

    @In
    private AssetManager assetManager;

    @In
    private Config config;

    private StandardMeshData meshData = new StandardMeshData(DrawingMode.LINES, AllocationType.STREAM);
    private Mesh mesh;
    private Material material;

    private Random random = new Random();

    @Override
    public void initialise() {
        super.initialise();
        mesh = Assets.generateAsset(meshData, Mesh.class);
        material = assetManager.getAsset("engine:white", Material.class).get();
    }

    @ReceiveEvent(components = {SkinnedMeshComponent.class, LocationComponent.class})
    public void newSkeleton(OnActivatedComponent event, EntityRef entity) {
        SkinnedMeshComponent skeleton = entity.getComponent(SkinnedMeshComponent.class);
        if (skeleton.mesh == null) {
            return;
        }

        if (skeleton.boneEntities == null) {
            skeleton.boneEntities = Maps.newHashMap();
            for (Bone bone : skeleton.mesh.bones()) {
                LocationComponent loc = new LocationComponent();
                EntityRef boneEntity = entityManager.create(loc);
                skeleton.boneEntities.put(bone.getName(), boneEntity);
            }
        }

        for (Bone bone : skeleton.mesh.bones()) {
            EntityRef boneEntity = skeleton.boneEntities.get(bone.getName());
            EntityRef parent = (bone.getParent() != null) ? skeleton.boneEntities.get(bone.getParent().getName()) : entity;
            Location.attachChild(parent, boneEntity);
        }
        for (Bone bone : skeleton.mesh.bones()) {
            EntityRef boneEntity = skeleton.boneEntities.get(bone.getName());
            LocationComponent loc = boneEntity.getComponent(LocationComponent.class);
            loc.setLocalPosition(bone.getLocalPosition());
            loc.setLocalRotation(bone.getLocalRotation());
            loc.setLocalScale(bone.getLocalScale().x);
            boneEntity.saveComponent(loc);
            if (bone.getParent() == null) {
                skeleton.rootBone = boneEntity;
            }
        }
        entity.saveComponent(skeleton);
    }

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(SkinnedMeshComponent.class, LocationComponent.class)) {
            updateSkeletalMeshOfEntity(entity, delta);
        }
    }

    private void updateSkeletalMeshOfEntity(EntityRef entity, float delta) {
        SkinnedMeshComponent skeletalMeshComp = entity.getComponent(SkinnedMeshComponent.class);
        if (skeletalMeshComp.mesh == null) {
            return;
        }

        if (skeletalMeshComp.animation == null) {
            return;
        }

        if (skeletalMeshComp.animation.getFrameCount() < 1) {
            return;
        }

        if (skeletalMeshComp.rootBone != null) {
            LocationComponent locationComponent = skeletalMeshComp.rootBone.getComponent(LocationComponent.class);
            if (locationComponent != null) {
                locationComponent.setLocalPosition(skeletalMeshComp.localOffset);
                locationComponent.setLocalScale(skeletalMeshComp.localScale);
                locationComponent.setLocalRotation(skeletalMeshComp.localRotation);
            }
        }

        float animationDuration = skeletalMeshComp.animation.getDuration();
        if (skeletalMeshComp.currentTime >= animationDuration) {
            skeletalMeshComp.currentTime -= animationDuration;
            if (skeletalMeshComp.currentTime < 0) {
                skeletalMeshComp.currentTime = 0;
            }
        }

        float framePos = skeletalMeshComp.currentTime / skeletalMeshComp.animation.getTimePerFrame();
        int currentFrame = (int) framePos;
        int nextFrame = currentFrame + 1;
        if (nextFrame >= skeletalMeshComp.animation.getFrameCount()) {
            nextFrame = 0;
        }
        float frameDelta = framePos - currentFrame;
        MeshAnimationFrame animatedFrame1 = skeletalMeshComp.animation.getFrame(currentFrame);
        MeshAnimationFrame animatedFrame2 = skeletalMeshComp.animation.getFrame(nextFrame);
        updateFrame(skeletalMeshComp, animatedFrame1, animatedFrame2, frameDelta);
        entity.saveComponent(skeletalMeshComp);
    }

    private void updateFrame(SkinnedMeshComponent skeletalMeshComp, MeshAnimationFrame frameA, MeshAnimationFrame frameB,
                             float interpolationVal) {


        for (int i = 0; i < skeletalMeshComp.animation.getBoneCount(); ++i) {
            String boneName = skeletalMeshComp.animation.getBoneName(i);
            EntityRef boneEntity = skeletalMeshComp.boneEntities.get(boneName);
            if (boneEntity == null) {
                continue;
            }

            LocationComponent boneLoc = boneEntity.getComponent(LocationComponent.class);
            if (boneLoc == null) {
                continue;
            }

            Vector3f newPos = frameA.getPosition(i).lerp(frameB.getPosition(i), interpolationVal, new Vector3f());
            boneLoc.setLocalPosition(newPos);
            Quaternionf newRot = frameA.getRotation(i).slerp(frameB.getRotation(i), interpolationVal, new Quaternionf());
            newRot.normalize();
            boneLoc.setLocalRotation(newRot);
            boneLoc.setLocalScale(frameA.getBoneScale(i).lerp(frameB.getBoneScale(i), interpolationVal, new Vector3f()).x);
            boneEntity.saveComponent(boneLoc);

        }
    }

    @Override
    public void renderOpaque() {
        Vector3fc cameraPosition = worldRenderer.getActiveCamera().getPosition();

        Quaternionf worldRot = new Quaternionf();
        Vector3f worldPos = new Vector3f();
        Quaternionf inverseWorldRot = new Quaternionf();

        FloatBuffer tempMatrixBuffer44 = BufferUtils.createFloatBuffer(16);
        FloatBuffer tempMatrixBuffer33 = BufferUtils.createFloatBuffer(12);

        for (EntityRef entity : entityManager.getEntitiesWith(SkinnedMeshComponent.class, LocationComponent.class)) {

            SkinnedMeshComponent skeletalMesh = entity.getComponent(SkinnedMeshComponent.class);
            if (skeletalMesh.mesh == null
                    || skeletalMesh.material == null
                    || skeletalMesh.boneEntities == null
                    || !skeletalMesh.material.isRenderable()) {
                continue;
            }
            AABBfc aabb;
            MeshAnimation animation = skeletalMesh.animation;
            if (animation != null) {
                aabb = animation.getAabb();
            } else {
                aabb = skeletalMesh.mesh.getAABB();
            }
            LocationComponent location = entity.getComponent(LocationComponent.class);
            location.getWorldRotation(worldRot);
            worldRot.invert(inverseWorldRot);
            location.getWorldPosition(worldPos);
            float worldScale = location.getWorldScale();

            aabb = aabb.transform(new Matrix4f().translationRotateScale(worldPos, worldRot, worldScale), new AABBf());

            //Scale bounding box for skeletalMesh.
            float scale = skeletalMesh.localScale;

            Vector3f aabbCenter = aabb.center(new Vector3f());
            Vector3f scaledExtents = aabb.extent(new Vector3f()).mul(scale, scale, scale);
            aabb = new AABBf(aabbCenter, aabbCenter).expand(scaledExtents);

            if (!worldRenderer.getActiveCamera().hasInSight(aabb)) {
                continue;
            }

            skeletalMesh.material.enable();
            skeletalMesh.material.setFloat("sunlight", 1.0f, true);
            skeletalMesh.material.setFloat("blockLight", 1.0f, true);
            skeletalMesh.material.setFloat3("colorOffset", skeletalMesh.color.rf(),
                    skeletalMesh.color.gf(), skeletalMesh.color.bf(), true);

            skeletalMesh.material.setMatrix4("projectionMatrix", worldRenderer.getActiveCamera().getProjectionMatrix());
            skeletalMesh.material.bindTextures();

            Vector3f worldPositionCameraSpace = new Vector3f();
            worldPos.sub(cameraPosition, worldPositionCameraSpace);

            Matrix4f matrixCameraSpace = new Matrix4f().translationRotateScale(worldPositionCameraSpace, worldRot, worldScale);

            Matrix4f modelViewMatrix = worldRenderer.getActiveCamera().getViewMatrix().mul(matrixCameraSpace, new Matrix4f());
            modelViewMatrix.get(tempMatrixBuffer44);
            skeletalMesh.material.setMatrix4("modelViewMatrix", tempMatrixBuffer44, true);

            modelViewMatrix.normal(new Matrix3f()).get(tempMatrixBuffer33);
            skeletalMesh.material.setMatrix3("normalMatrix", tempMatrixBuffer33, true);

            skeletalMesh.material.setFloat("sunlight", worldRenderer.getMainLightIntensityAt(worldPos), true);
            skeletalMesh.material.setFloat("blockLight", worldRenderer.getBlockLightIntensityAt(worldPos), true);

            Matrix4f boneTransform = new Matrix4f();
            for (Bone bone : skeletalMesh.mesh.bones()) {
                EntityRef boneEntity = skeletalMesh.boneEntities.get(bone.getName());
                if (boneEntity == null) {
                    boneEntity = EntityRef.NULL;
                }
                LocationComponent boneLocation = boneEntity.getComponent(LocationComponent.class);
                boneTransform.identity();
                if (boneLocation != null) {
                    boneLocation.getRelativeTransform(boneTransform, entity);
                    boneTransform.mul(bone.getInverseBindMatrix());
                } else {
                    logger.warn("Unable to resolve bone \"{}\"", bone.getName());
                }
                skeletalMesh.material.setMatrix4("boneTransforms[" + bone.getIndex() + "]", boneTransform, true);
            }
            skeletalMesh.mesh.render();
        }
    }

    @Override
    public void renderOverlay() {
        if (config.getRendering().getDebug().isRenderSkeletons()) {

            meshData.reallocate(0, 0);
            meshData.indices.rewind();
            meshData.position.rewind();
            meshData.color0.rewind();

            Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();

            Matrix4f entityTransform = new Matrix4f();
            Vector3f currentPos = new Vector3f();

            int index = 0;
            for (EntityRef entity : entityManager.getEntitiesWith(SkinnedMeshComponent.class, LocationComponent.class)) {
                SkinnedMeshComponent skeletalMesh = entity.getComponent(SkinnedMeshComponent.class);
                LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
                if (skeletalMesh.boneEntities == null) {
                    continue;
                }

                Vector3f location = locationComponent.getWorldPosition(new Vector3f());
                Quaternionf rotation = locationComponent.getWorldRotation(new Quaternionf());
                entityTransform.translationRotateScale(location, rotation, 1.0f); // transformation of the entity

                // position is referenced around (0,0,0) (worldposition - cameraposition)
                Vector3f worldPositionCameraSpace = cameraPosition.negate(new Vector3f());

                Matrix4f matrixCameraSpace = new Matrix4f().translationRotateScale(worldPositionCameraSpace, new Quaternionf(), 1.0f);
                Matrix4f modelViewMatrix = new Matrix4f(worldRenderer.getActiveCamera().getViewMatrix()).mul(matrixCameraSpace);
                material.setMatrix4("projectionMatrix", worldRenderer.getActiveCamera().getProjectionMatrix());
                material.setMatrix4("modelViewMatrix", modelViewMatrix, true);

                for (Bone bone : skeletalMesh.mesh.bones()) {
                    Bone parentBone = bone.getParent();
                    EntityRef boneEntity = skeletalMesh.boneEntities.get(bone.getName());
                    if (parentBone == null) {
                        continue;
                    }

                    // TODO: the position of the bone is de-coupled from the actual translation/scale
                    EntityRef boneParentEntity = skeletalMesh.boneEntities.get(parentBone.getName());
                    LocationComponent locCompA = boneEntity.getComponent(LocationComponent.class);
                    LocationComponent locCompB = boneParentEntity.getComponent(LocationComponent.class);

                    locCompA.getWorldPosition(currentPos);
                    meshData.position.put(currentPos); // the start of the bone

                    locCompB.getWorldPosition(currentPos);
                    meshData.position.put(currentPos); // the end of the bone

                    meshData.color0.put(Color.white);
                    meshData.color0.put(Color.white);

                    meshData.indices.putAll(new int[]{
                            index, index + 1
                    });

                    index += 2;
                }

            }

            GL33.glDepthFunc(GL33.GL_ALWAYS);
            material.enable();
            mesh.reload(meshData);
            mesh.render();
            GL33.glDepthFunc(GL33.GL_LEQUAL);
        }
    }
}
