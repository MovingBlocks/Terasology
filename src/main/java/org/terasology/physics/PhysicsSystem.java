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

package org.terasology.physics;

import org.terasology.physics.bullet.BulletPhysics;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gnu.trove.iterator.TFloatIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.Time;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.network.NetworkComponent;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.shapes.BoxShapeComponent;
import org.terasology.physics.shapes.CapsuleShapeComponent;
import org.terasology.physics.shapes.CylinderShapeComponent;
import org.terasology.physics.shapes.HullShapeComponent;
import org.terasology.physics.shapes.SphereShapeComponent;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Immortius
 */
@RegisterSystem
public class PhysicsSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(PhysicsSystem.class);

    private static final long TIME_BETWEEN_NETSYNCS = 200;
    private static final float RESYNC_TIME = 0.25f;

    @In
    private Time time;

    @In
    private NetworkSystem networkSystem;

    private BulletPhysics physics;
    private int skipProcessingFrames = 4;
    private long lastNetsync;
    private Map<EntityRef, ResynchData> pendingResynch = Maps.newLinkedHashMap();

    @Override
    public void initialise() {
        physics = CoreRegistry.get(BulletPhysics.class);
        skipProcessingFrames = 4;
        lastNetsync = 0;
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class}, priority = EventPriority.PRIORITY_NORMAL)
    public void newRigidBody(OnActivatedComponent event, EntityRef entity) {
        physics.newRigidBody(entity);
    }

    @ReceiveEvent(components = {RigidBodyComponent.class})
    public void onImpulse(ImpulseEvent event, EntityRef entity) {
        physics.getRigidBody(entity).applyImpulse(new Vector3f(event.getImpulse()));
    }

    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class})
    public void removeRigidBody(BeforeDeactivateComponent event, EntityRef entity) {
        physics.removeRigidBody(entity);
    }
    
    @ReceiveEvent(components = {TriggerComponent.class, LocationComponent.class})
    public void newTrigger(OnActivatedComponent event, EntityRef entity) {
        physics.newTrigger(entity);
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

    @Override
    public void update(float delta) {

        if (networkSystem.getMode() == NetworkMode.SERVER && time.getGameTimeInMs() - TIME_BETWEEN_NETSYNCS > lastNetsync) {
            sendSyncMessages();
            lastNetsync = time.getGameTimeInMs();
        }

        resynchronize(delta);

        // TODO: This shouldn't be necessary once this is correctly sequenced after the main physics update
        if (skipProcessingFrames > 0) {
            skipProcessingFrames--;
            return;
        }

        List<CollisionPair> collisionPairs = physics.getCollisionPairs();

        for (CollisionPair pair : collisionPairs) {
            if (pair.b.exists()) {
                pair.a.send(new CollideEvent(pair.b));
            }
            if (pair.a.exists()) {
                pair.b.send(new CollideEvent(pair.a));
            }
        }
    }

    /**
     * resynchronising happens smoothly to prevent stuttering of objects. This
     * means that it may take several frames for a resynchronisation message to
     * be processed.
     * @param delta The time since the last resynchronisation. Must be in the
     * same unit as RESYNC_TIME (seconds).
     */
    private void resynchronize(float delta) {
        Iterator<Map.Entry<EntityRef, ResynchData>> i = pendingResynch.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<EntityRef, ResynchData> entry = i.next();
            ResynchData data = entry.getValue();

            float applyDelta = delta;
            float timeDifference = delta + data.getT();
            if (timeDifference >= RESYNC_TIME) {
                applyDelta -= timeDifference - RESYNC_TIME;
                i.remove();
            }
            Vector3f change = new Vector3f();
            change.scale(applyDelta / RESYNC_TIME, data.positionDelta);
            physics.translate(entry.getKey(), change);
        }
    }

    private void sendSyncMessages() {
        /*for (Map.Entry<EntityRef, RigidBody> physicsObj : entityRigidBodies.entrySet()) {
            if (physicsObj.getKey().hasComponent(NetworkComponent.class)) {
                Transform transform = physicsObj.getValue().getWorldTransform(new Transform());
                if (physicsObj.getValue().getActivationState() == RigidBody.ACTIVE_TAG) {
                    PhysicsResynchEvent event = new PhysicsResynchEvent(transform.origin, transform.getRotation(new Quat4f()),
                            physicsObj.getValue().getLinearVelocity(new Vector3f()), physicsObj.getValue().getAngularVelocity(new Vector3f()));
                    physicsObj.getKey().send(event);
                }
            }
        }*/
        Iterator<EntityRef> iter = physics.physicsEntitiesIterator();
        while(iter.hasNext()) {
            EntityRef entity = iter.next();
            if(entity.hasComponent(NetworkComponent.class)) {
                //TODO after implementing rigidbody interface
                /*Transform transform = physicsObj.getValue().getWorldTransform(new Transform());
                if (physicsObj.getValue().getActivationState() == RigidBody.ACTIVE_TAG) {
                    PhysicsResynchEvent event = new PhysicsResynchEvent(transform.origin, transform.getRotation(new Quat4f()),
                            physicsObj.getValue().getLinearVelocity(new Vector3f()), physicsObj.getValue().getAngularVelocity(new Vector3f()));
                    physicsObj.getKey().send(event);
                }*/
            }
        }
    }

    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class}, netFilter = RegisterMode.REMOTE_CLIENT)
    public void resynch(PhysicsResynchEvent event, EntityRef entity) {
        logger.debug("Received resynch event");
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        Vector3f delta = new Vector3f(event.getPosition());
        delta.sub(loc.getWorldPosition());
        pendingResynch.put(entity, new ResynchData(delta, new Quat4f()));
        physics.getRigidBody(entity).setVelocity(event.getVelocity(), event.getAngularVelocity());
    }

    public static class CollisionPair {
        EntityRef a;
        EntityRef b;

        public CollisionPair(EntityRef a, EntityRef b) {
            this.a = a;
            this.b = b;
        }
    }

    private static class ResynchData {
        private Vector3f positionDelta = new Vector3f();
        private Quat4f rotationDelta = new Quat4f();
        private float t;

        public ResynchData(Vector3f position, Quat4f rotation) {
            this.positionDelta.set(position);
            this.rotationDelta.set(rotation);
        }

        public float getT() {
            return t;
        }

        public void setT(float t) {
            this.t = t;
        }

        public Vector3f getPositionDelta() {
            return positionDelta;
        }

        public Quat4f getRotationDelta() {
            return rotationDelta;
        }
    }
}
