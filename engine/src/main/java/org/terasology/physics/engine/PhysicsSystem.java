/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.physics.engine;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.location.LocationResynchEvent;
import org.terasology.math.JomlUtil;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.network.NetworkComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.physics.components.RigidBodyComponent;
import org.terasology.physics.components.TriggerComponent;
import org.terasology.physics.events.ChangeVelocityEvent;
import org.terasology.physics.events.CollideEvent;
import org.terasology.physics.events.ForceEvent;
import org.terasology.physics.events.ImpulseEvent;
import org.terasology.physics.events.PhysicsResynchEvent;
import org.terasology.physics.events.ImpactEvent;
import org.terasology.physics.events.EntityImpactEvent;
import org.terasology.physics.events.BlockImpactEvent;
import org.terasology.registry.In;
import org.terasology.world.OnChangedBlock;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;

import com.google.common.collect.Lists;

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
public class PhysicsSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final Logger logger = LoggerFactory.getLogger(PhysicsSystem.class);
    private static final long TIME_BETWEEN_NETSYNCS = 500;
    private static final CollisionGroup[] DEFAULT_COLLISION_GROUP = {StandardCollisionGroup.WORLD, StandardCollisionGroup.CHARACTER, StandardCollisionGroup.DEFAULT};
    private static final float COLLISION_DAMPENING_MULTIPLIER = 0.5f;
    @In
    private Time time;
    @In
    private NetworkSystem networkSystem;
    @In
    private EntityManager entityManager;
    @In
    private PhysicsEngine physics;
    @In
    private WorldProvider worldProvider;

    private long lastNetsync;

    @Override
    public void initialise() {
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

    @ReceiveEvent(components = {RigidBodyComponent.class})
    public void onImpulse(ImpulseEvent event, EntityRef entity) {
        physics.getRigidBody(entity).applyImpulse(event.getImpulse());
    }

    @ReceiveEvent(components = {RigidBodyComponent.class})
    public void onForce(ForceEvent event, EntityRef entity) {
        physics.getRigidBody(entity).applyForce(event.getForce());
    }

    @ReceiveEvent(components = {RigidBodyComponent.class})
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

    @ReceiveEvent(components = {BlockComponent.class})
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
                                    entity.send(new EntityImpactEvent(hitInfo.getHitPoint(), hitInfo.getHitNormal(), comp.velocity, fDistanceThisFrame, hitInfo.getEntity()));
                                    break;
                                }
                                fDistanceThisFrame = fDistanceThisFrame - fTravelledDistance; // decrease the remaining distance to check if we hit a block
                                vLocation = hitInfo.getHitPoint();
                            } else {
                                entity.send(new BlockImpactEvent(hitInfo.getHitPoint(), hitInfo.getHitNormal(), comp.velocity, fDistanceThisFrame, hitInfo.getEntity()));
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
