// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.bullet;

import org.joml.Vector3f;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.physics.components.RigidBodyComponent;
import org.terasology.engine.physics.events.ChangeVelocityEvent;
import org.terasology.engine.physics.events.ForceEvent;
import org.terasology.engine.physics.events.ImpulseEvent;
import org.terasology.engine.physics.events.PhysicsTickEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;

import java.util.function.BiFunction;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class PhysicsSystemAuthority extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final long TIME_BETWEEN_NETSYNCS = 100;

    @In
    protected WorldProvider worldProvider;
    @In
    protected Time time;

    private long lastNetsync = 0;

    private PhysicsTickEvent current = new PhysicsTickEvent();

    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class})
    public void newRigidBody(LocationComponent loc, OnActivatedComponent event, EntityRef entity, RigidBodyComponent rigidBodyComponent, LocationComponent locationComponent) {
        locationComponent.replicateChanges = false; // physics state is simulate on both sides prevent changes from getting replicated
        current.toCreate.add(new PhysicsTickEvent.CreateBody(entity, rigidBodyComponent));
    }

    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class})
    public void removeRigidBody(BeforeDeactivateComponent event, EntityRef entity, RigidBodyComponent rigidBodyComponent, LocationComponent locationComponent) {
        locationComponent.replicateChanges = true;
        current.toRemove.add(entity);
    }

    @ReceiveEvent(components = {RigidBodyComponent.class, LocationComponent.class})
    public void updateRigidBody(OnChangedComponent event, EntityRef entity, RigidBodyComponent rigidBodyComponent) {

        if ((rigidBodyComponent.getDirtyFlags() & (RigidBodyComponent.DIRTY_COLLISION_FILTER | RigidBodyComponent.DIRTY_KINEMATIC)) > 0) {
            current.toFlags.compute(entity, new BiFunction<EntityRef, PhysicsTickEvent.BitFlags, PhysicsTickEvent.BitFlags>() {
                @Override
                public PhysicsTickEvent.BitFlags apply(EntityRef entityRef, PhysicsTickEvent.BitFlags bitFlags) {
                    PhysicsTickEvent.BitFlags state = (bitFlags == null) ? new PhysicsTickEvent.BitFlags() : bitFlags;
                    state.collidesWith = rigidBodyComponent.getCollidesWith();
                    state.collisionGroup = rigidBodyComponent.getCollisionGroup();
                    state.isKinematic = rigidBodyComponent.getKinematic();
                    return null;
                }
            });
        }
        if ((rigidBodyComponent.getDirtyFlags() & (RigidBodyComponent.DIRTY_FACTOR_FILTER)) > 0) {
            current.toUpdateState.compute(entity, new BiFunction<EntityRef, PhysicsTickEvent.UpdateState, PhysicsTickEvent.UpdateState>() {
                @Override
                public PhysicsTickEvent.UpdateState apply(EntityRef entityRef, PhysicsTickEvent.UpdateState updateState) {
                    PhysicsTickEvent.UpdateState state = (updateState == null) ? new PhysicsTickEvent.UpdateState() : updateState;
                    state.angularFactor = new Vector3f(rigidBodyComponent.getAngularFactor());
                    state.linearFactor = new Vector3f(rigidBodyComponent.getLinearFactor());
                    state.friction = rigidBodyComponent.getFriction();
                    state.restitution = rigidBodyComponent.getRestitution();
                    return state;
                }
            });
        }
    }



    @ReceiveEvent(components = RigidBodyComponent.class)
    public void onImpulse(ImpulseEvent event, EntityRef entity) {
        current.toImpulse.compute(entity, new BiFunction<EntityRef, Vector3f, Vector3f>() {
            @Override
            public Vector3f apply(EntityRef entityRef, Vector3f pos) {
                if (pos == null) {
                    return new Vector3f(event.getImpulse());
                }
                return pos.add(event.getImpulse());
            }
        });
//        btRigidBody rb = entityRigidBodies.get(entity);
//        if (rb != null) {
//            rb.applyCentralImpulse(event.getImpulse());
//        }
    }

    @ReceiveEvent(components = RigidBodyComponent.class)
    public void onForce(ForceEvent event, EntityRef entity) {
        current.toForce.compute(entity, new BiFunction<EntityRef, Vector3f, Vector3f>() {
            @Override
            public Vector3f apply(EntityRef entityRef, Vector3f pos) {
                if (pos == null) {
                    return new Vector3f(event.getForce());
                }
                return pos.add(event.getForce());
            }
        });
//        btRigidBody rb = entityRigidBodies.get(entity);
//        if (rb != null) {
//            rb.applyCentralForce(event.getForce());
//        }
    }

    @ReceiveEvent(components = RigidBodyComponent.class)
    public void onChangeVelocity(ChangeVelocityEvent event, EntityRef entity) {
        current.toChangeVelocity.compute(entity, new BiFunction<EntityRef, PhysicsTickEvent.ChangeVelocity,
                PhysicsTickEvent.ChangeVelocity>() {
            @Override
            public PhysicsTickEvent.ChangeVelocity apply(EntityRef entityRef, PhysicsTickEvent.ChangeVelocity old) {
                PhysicsTickEvent.ChangeVelocity velocity = old;
                if (velocity == null) {
                    velocity = new PhysicsTickEvent.ChangeVelocity();
                }
                velocity.angularVelocity = event.getAngularVelocity();
                velocity.linearVelocity = event.getLinearVelocity();
                return velocity;
            }
        });
    }


    @Override
    public void update(float delta) {
        EntityRef worldEntity = worldProvider.getWorldEntity();
        if (time.getGameTimeInMs() - TIME_BETWEEN_NETSYNCS > lastNetsync) {
            current.time = time.getGameTimeInMs();
            worldEntity.send(current);
            current = new PhysicsTickEvent();
            lastNetsync = time.getGameTimeInMs();
        }
    }
}
