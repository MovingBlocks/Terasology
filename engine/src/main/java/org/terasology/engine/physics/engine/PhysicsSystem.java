// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.engine;

import com.google.common.collect.Lists;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.RenderSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.location.LocationResynchEvent;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.network.NetworkComponent;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.physics.CollisionGroup;
import org.terasology.engine.physics.HitResult;
import org.terasology.engine.physics.StandardCollisionGroup;
import org.terasology.engine.physics.components.RigidBodyComponent;
import org.terasology.engine.physics.components.TriggerComponent;
import org.terasology.engine.physics.components.shapes.BoxShapeComponent;
import org.terasology.engine.physics.components.shapes.CapsuleShapeComponent;
import org.terasology.engine.physics.components.shapes.CylinderShapeComponent;
import org.terasology.engine.physics.components.shapes.SphereShapeComponent;
import org.terasology.engine.physics.events.BlockImpactEvent;
import org.terasology.engine.physics.events.ChangeVelocityEvent;
import org.terasology.engine.physics.events.CollideEvent;
import org.terasology.engine.physics.events.EntityImpactEvent;
import org.terasology.engine.physics.events.ForceEvent;
import org.terasology.engine.physics.events.ImpactEvent;
import org.terasology.engine.physics.events.ImpulseEvent;
import org.terasology.engine.physics.events.PhysicsResynchEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.mesh.StandardMeshData;
import org.terasology.engine.rendering.assets.mesh.resource.AllocationType;
import org.terasology.engine.rendering.assets.mesh.resource.DrawingMode;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.OnChangedBlock;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.joml.geom.AABBf;
import org.terasology.nui.Color;

import java.util.Iterator;
import java.util.List;

/**
 * The PhysicsSystem is a bridging class between the event system and the
 * physics engine. It translates events into changes to the physics engine and
 * translates output of the physics engine into events. It also calls the update
 * method of the PhysicsEngine every frame.
 *
 */
@RegisterSystem
public class PhysicsSystem extends BaseComponentSystem implements UpdateSubscriberSystem, RenderSystem {

    private static final Logger logger = LoggerFactory.getLogger(PhysicsSystem.class);
    private static final long TIME_BETWEEN_NETSYNCS = 500;
    private static final CollisionGroup[] DEFAULT_COLLISION_GROUP =
            {StandardCollisionGroup.WORLD, StandardCollisionGroup.CHARACTER, StandardCollisionGroup.DEFAULT};
    private static final float COLLISION_DAMPENING_MULTIPLIER = 0.5f;
    @In
    private Time time;
    @In
    private NetworkSystem networkSystem;
    @In
    private PhysicsEngine physics;
    @In
    private WorldProvider worldProvider;
    @In
    protected AssetManager assetManager;
    @In
    protected WorldRenderer worldRenderer;
    @In
    protected Config config;
    @In
    protected EntityManager entityManager;

    private StandardMeshData meshData = new StandardMeshData(DrawingMode.LINES, AllocationType.STREAM);
    private Mesh mesh;
    private Material material;

    private long lastNetsync;

    @Override
    public void initialise() {
        material = assetManager.getAsset("engine:white", Material.class).get();
        mesh = Assets.generateAsset(meshData, Mesh.class);

        lastNetsync = 0;
    }

    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class}, priority = EventPriority.PRIORITY_NORMAL)
    public void newRigidBody(OnActivatedComponent event, EntityRef entity) {
        //getter also creates the rigid body
        physics.getRigidBody(entity);
    }

    @ReceiveEvent(components = {TriggerComponent.class, LocationComponent.class})
    //update also creates the trigger
    public void newTrigger(OnActivatedComponent event, EntityRef entity) {
        physics.updateTrigger(entity);
    }

    @ReceiveEvent(components = RigidBodyComponent.class)
    public void onImpulse(ImpulseEvent event, EntityRef entity) {
        physics.getRigidBody(entity).applyImpulse(event.getImpulse());
    }

    @ReceiveEvent(components = RigidBodyComponent.class)
    public void onForce(ForceEvent event, EntityRef entity) {
        physics.getRigidBody(entity).applyForce(event.getForce());
    }

    @ReceiveEvent(components = RigidBodyComponent.class)
    public void onChangeVelocity(ChangeVelocityEvent event, EntityRef entity) {
        if (event.getAngularVelocity() != null) {
            physics.getRigidBody(entity).setAngularVelocity(event.getAngularVelocity());
        }
        if (event.getLinearVelocity() != null) {
            physics.getRigidBody(entity).setLinearVelocity(event.getLinearVelocity());
        }
    }

    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class})
    public void removeRigidBody(BeforeDeactivateComponent event, EntityRef entity) {
        physics.removeRigidBody(entity);
    }

    @ReceiveEvent(components = {TriggerComponent.class, LocationComponent.class})
    public void removeTrigger(BeforeDeactivateComponent event, EntityRef entity) {
        physics.removeTrigger(entity);
    }

    @ReceiveEvent(components = {TriggerComponent.class, LocationComponent.class})
    public void updateTrigger(OnChangedComponent event, EntityRef entity) {
        physics.updateTrigger(entity);
    }

    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class})
    public void updateRigidBody(OnChangedComponent event, EntityRef entity) {
        physics.updateRigidBody(entity);
    }

    @ReceiveEvent(components = BlockComponent.class)
    public void onBlockAltered(OnChangedBlock event, EntityRef entity) {
        physics.awakenArea(new Vector3f(event.getBlockPosition()), 0.6f);
    }

    @ReceiveEvent
    public void onItemImpact(ImpactEvent event, EntityRef entity) {
        RigidBody rigidBody = physics.getRigidBody(entity);
        if (rigidBody != null) {
            Vector3f vImpactNormal = new Vector3f(event.getImpactNormal());
            Vector3f vImpactPoint = new Vector3f(event.getImpactPoint());
            Vector3f vImpactSpeed = new Vector3f(event.getImpactSpeed());

            float speedFactor = vImpactSpeed.length();
            vImpactNormal.normalize();
            vImpactSpeed.normalize();

            float dotImpactNormal = vImpactSpeed.dot(vImpactNormal);

            Vector3f impactResult = vImpactNormal.mul(dotImpactNormal);
            impactResult = vImpactSpeed.sub(impactResult.mul(2.0f));
            impactResult.normalize();

            Vector3f vNewLocationVector = (new Vector3f(impactResult)).mul(event.getTravelDistance());
            Vector3f vNewPosition = (new Vector3f(vImpactPoint)).add(vNewLocationVector);
            Vector3f vNewVelocity = (new Vector3f(impactResult)).mul(speedFactor * COLLISION_DAMPENING_MULTIPLIER);

            rigidBody.setLocation(vNewPosition);
            rigidBody.setLinearVelocity(vNewVelocity);
            rigidBody.setAngularVelocity(vNewVelocity);
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

            for (EntityRef entity : entityManager.getEntitiesWith(RigidBodyComponent.class)) {
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

    @Override
    public void update(float delta) {

        PerformanceMonitor.startActivity("Physics Renderer");
        physics.update(time.getGameDelta());
        PerformanceMonitor.endActivity();

        //Update the velocity from physics engine bodies to Components:
        Iterator<EntityRef> iter = physics.physicsEntitiesIterator();
        while (iter.hasNext()) {
            EntityRef entity = iter.next();
            RigidBodyComponent comp = entity.getComponent(RigidBodyComponent.class);
            RigidBody body = physics.getRigidBody(entity);

            // force location component to update and sync trigger state
            if (entity.hasComponent(TriggerComponent.class)) {
                physics.updateTrigger(entity);
            }

            if (body.isActive()) {
                body.getLinearVelocity(comp.velocity);
                body.getAngularVelocity(comp.angularVelocity);

                Vector3f vLocation = body.getLocation(new org.joml.Vector3f());

                Vector3f vDirection = new Vector3f(comp.velocity);
                float fDistanceThisFrame = vDirection.length();
                vDirection.normalize();

                fDistanceThisFrame = fDistanceThisFrame * delta;

                while (true) {
                    HitResult hitInfo = physics.rayTrace(vLocation, vDirection, fDistanceThisFrame + 0.5f, DEFAULT_COLLISION_GROUP);
                    if (hitInfo.isHit()) {
                        Block hitBlock = worldProvider.getBlock(hitInfo.getBlockPosition());
                        if (hitBlock != null) {
                            Vector3f vTravelledDistance = vLocation.sub(hitInfo.getHitPoint());
                            float fTravelledDistance  = vTravelledDistance.length();
                            if (fTravelledDistance > fDistanceThisFrame) {
                                break;
                            }
                            if (hitBlock.isPenetrable()) {
                                if (!hitInfo.getEntity().hasComponent(BlockComponent.class)) {
                                    entity.send(new EntityImpactEvent(hitInfo.getHitPoint(), hitInfo.getHitNormal(), comp.velocity,
                                            fDistanceThisFrame, hitInfo.getEntity()));
                                    break;
                                }
                                // decrease the remaining distance to check if we hit a block
                                fDistanceThisFrame = fDistanceThisFrame - fTravelledDistance;
                                vLocation = hitInfo.getHitPoint();
                            } else {
                                entity.send(new BlockImpactEvent(hitInfo.getHitPoint(), hitInfo.getHitNormal(), comp.velocity,
                                        fDistanceThisFrame, hitInfo.getEntity()));
                                break;
                            }
                        } else {
                            break;
                        }
                    } else  {
                        break;
                    }
                }
            }
        }

        if (networkSystem.getMode().isServer() && time.getGameTimeInMs() - TIME_BETWEEN_NETSYNCS > lastNetsync) {
            sendSyncMessages();
            lastNetsync = time.getGameTimeInMs();
        }

        List<CollisionPair> collisionPairs = physics.getCollisionPairs();

        for (CollisionPair pair : collisionPairs) {
            if (pair.b.exists()) {
                short bCollisionGroup = getCollisionGroupFlag(pair.b);
                short aCollidesWith = getCollidesWithGroupFlag(pair.a);
                if ((bCollisionGroup & aCollidesWith) != 0
                        || (pair.b.hasComponent(BlockComponent.class) && !pair.a.hasComponent(BlockComponent.class))) {
                    pair.a.send(new CollideEvent(pair.b, pair.pointA, pair.pointB, pair.distance, pair.normal));
                }
            }
            if (pair.a.exists()) {
                short aCollisionGroup = getCollisionGroupFlag(pair.a);
                short bCollidesWith = getCollidesWithGroupFlag(pair.b);
                if ((aCollisionGroup & bCollidesWith) != 0
                        || (pair.a.hasComponent(BlockComponent.class) && !pair.b.hasComponent(BlockComponent.class))) {
                    pair.b.send(new CollideEvent(pair.a, pair.pointB, pair.pointA, pair.distance, new Vector3f(pair.normal).mul(-1.0f)));
                }
            }
        }
    }

    private short getCollisionGroupFlag(EntityRef entity) {
        CollisionGroup collisionGroup = StandardCollisionGroup.NONE;
        if (entity.hasComponent(TriggerComponent.class)) {
            TriggerComponent entityTrigger = entity.getComponent(TriggerComponent.class);
            collisionGroup = entityTrigger.collisionGroup;
        } else if (entity.hasComponent(RigidBodyComponent.class)) {
            RigidBodyComponent entityRigidBody = entity.getComponent(RigidBodyComponent.class);
            collisionGroup = entityRigidBody.collisionGroup;
        }
        return collisionGroup.getFlag();
    }

    private short getCollidesWithGroupFlag(EntityRef entity) {
        List<CollisionGroup> collidesWithGroup = Lists.<CollisionGroup>newArrayList(StandardCollisionGroup.NONE);
        if (entity.hasComponent(TriggerComponent.class)) {
            TriggerComponent entityTrigger = entity.getComponent(TriggerComponent.class);
            collidesWithGroup = entityTrigger.detectGroups;
        } else if (entity.hasComponent(RigidBodyComponent.class)) {
            RigidBodyComponent entityRigidBody = entity.getComponent(RigidBodyComponent.class);
            collidesWithGroup = entityRigidBody.collidesWith;
        }
        short flag = 0;
        Iterator<CollisionGroup> iter = collidesWithGroup.iterator();
        while (iter.hasNext()) {
            CollisionGroup group = iter.next();
            flag |= group.getFlag();
        }
        return flag;
    }

    private void sendSyncMessages() {
        Iterator<EntityRef> iter = physics.physicsEntitiesIterator();
        while (iter.hasNext()) {
            EntityRef entity = iter.next();
            if (entity.hasComponent(NetworkComponent.class)) {
                //TODO after implementing rigidbody interface
                RigidBody body = physics.getRigidBody(entity);
                if (body.isActive()) {
                    entity.send(new LocationResynchEvent(body.getLocation(new Vector3f()), body.getOrientation(new Quaternionf())));
                    entity.send(new PhysicsResynchEvent(body.getLinearVelocity(new Vector3f()), body.getAngularVelocity(new Vector3f())));
                }
            }
        }
    }

    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class}, netFilter = RegisterMode.REMOTE_CLIENT)
    public void resynchPhysics(PhysicsResynchEvent event, EntityRef entity) {
        logger.debug("Received resynch event");
        RigidBody body = physics.getRigidBody(entity);
        body.setVelocity(event.getVelocity(), event.getAngularVelocity());
    }

    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class}, netFilter = RegisterMode.REMOTE_CLIENT)
    public void resynchLocation(LocationResynchEvent event, EntityRef entity) {
        logger.debug("Received location resynch event");
        RigidBody body = physics.getRigidBody(entity);
        body.setTransform(event.getPosition(), event.getRotation());
    }

    public static class CollisionPair {

        EntityRef a;
        EntityRef b;
        Vector3f pointA;
        Vector3f pointB;
        float distance;
        Vector3f normal;


        public CollisionPair(EntityRef a, EntityRef b, Vector3f pointA, Vector3f pointb, float distance, Vector3f normal) {
            this.a = a;
            this.b = b;
            this.pointA = pointA;
            this.pointB = pointb;
            this.distance = distance;
            this.normal = normal;
        }
    }
}
