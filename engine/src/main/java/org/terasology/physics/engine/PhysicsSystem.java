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
package org.terasology.physics.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.network.NetworkComponent;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.components.RigidBodyComponent;
import org.terasology.physics.components.TriggerComponent;
import org.terasology.physics.events.CollideEvent;
import org.terasology.physics.events.ImpulseEvent;
import org.terasology.physics.events.PhysicsResynchEvent;
import org.terasology.world.OnChangedBlock;
import org.terasology.world.block.BlockComponent;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.Iterator;
import java.util.List;

/**
 * The PhysicsSystem is a bridging class between the event system and the
 * physics engine. It translates events into changes to the physics engine and
 * translates output of the physics engine into events. It also calls the update
 * method of the PhysicsEngine every frame.
 *
 * @author Immortius
 */
@RegisterSystem
public class PhysicsSystem implements UpdateSubscriberSystem {

    private static final Logger logger = LoggerFactory.getLogger(PhysicsSystem.class);
    private static final long TIME_BETWEEN_NETSYNCS = 200;
    @In
    private Time time;
    @In
    private NetworkSystem networkSystem;
    @In
    private EntityManager entityManager;
    private PhysicsEngine physics;

    private long lastNetsync;

    @Override
    public void initialise() {
        physics = CoreRegistry.get(PhysicsEngine.class);
        lastNetsync = 0;
    }

    @Override
    public void shutdown() {
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
        physics.awakenArea(event.getBlockPosition().toVector3f(), 0.6f);
    }

    @Override
    public void update(float delta) {

        PerformanceMonitor.startActivity("Physics Renderer");
        physics.update(delta);
        PerformanceMonitor.endActivity();

        //Update the velocity from physics engine bodies to Components:
        Iterator<EntityRef> iter = physics.physicsEntitiesIterator();
        while (iter.hasNext()) {
            EntityRef entity = iter.next();
            RigidBodyComponent comp = entity.getComponent(RigidBodyComponent.class);
            RigidBody body = physics.getRigidBody(entity);
            body.getLinearVelocity(comp.velocity);
            body.getAngularVelocity(comp.angularVelocity);
            entity.saveComponent(comp);
        }

        if (networkSystem.getMode() == NetworkMode.SERVER && time.getGameTimeInMs() - TIME_BETWEEN_NETSYNCS > lastNetsync) {
            sendSyncMessages();
            lastNetsync = time.getGameTimeInMs();
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

    private void sendSyncMessages() {
        Iterator<EntityRef> iter = physics.physicsEntitiesIterator();
        while (iter.hasNext()) {
            EntityRef entity = iter.next();
            if (entity.hasComponent(NetworkComponent.class)) {
                //TODO after implementing rigidbody interface
                RigidBody body = physics.getRigidBody(entity);
                if (body.isActive()) {
                    PhysicsResynchEvent event = new PhysicsResynchEvent(body.getLocation(new Vector3f()), body.getOrientation(new Quat4f()),
                            body.getLinearVelocity(new Vector3f()), body.getAngularVelocity(new Vector3f()));
                    entity.send(event);
                }
            }
        }
    }

    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class}, netFilter = RegisterMode.REMOTE_CLIENT)
    public void resynch(PhysicsResynchEvent event, EntityRef entity) {
        logger.debug("Received resynch event");
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        Vector3f delta = new Vector3f(event.getPosition());
        delta.sub(loc.getWorldPosition());
        RigidBody body = physics.getRigidBody(entity);
        body.setVelocity(event.getVelocity(), event.getAngularVelocity());
        body.setTransform(event.getPosition(), event.getRotation());
    }

    public static class CollisionPair {

        EntityRef a;
        EntityRef b;

        public CollisionPair(EntityRef a, EntityRef b) {
            this.a = a;
            this.b = b;
        }
    }
}
