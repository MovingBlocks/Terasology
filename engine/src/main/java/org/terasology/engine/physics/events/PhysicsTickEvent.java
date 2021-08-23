// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.events;

import com.google.common.collect.Maps;
import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.BroadcastEvent;
import org.terasology.engine.physics.components.RigidBodyComponent;
import org.terasology.reflection.MappedContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@BroadcastEvent
public class PhysicsTickEvent implements Event {
    public long time = 0;
    public List<CreateBody> toCreate = new ArrayList<>();
    public List<EntityRef> toRemove = new ArrayList<>();

    public Map<EntityRef, Vector3f> toImpulse = Maps.newHashMap();
    public Map<EntityRef, Vector3f> toForce = Maps.newHashMap();
    public Map<EntityRef, ChangeVelocity> toChangeVelocity = Maps.newHashMap();
    public Map<EntityRef, BitFlags> toFlags = Maps.newHashMap();
    public Map<EntityRef, UpdateState> toUpdateState = Maps.newHashMap();

    @MappedContainer
    public static class UpdateState {
        public Vector3f angularFactor = new Vector3f(1f, 1f, 1f);
        public Vector3f linearFactor = new Vector3f(1f, 1f, 1f);
        public float restitution = 0.0f;
        public float friction = 0.0f;
    }


    @MappedContainer
    public static class BitFlags {
        public int collisionGroup = 0;
        public int collidesWith = 0;
        public boolean isKinematic = false;
    }

    @MappedContainer
    public static class ChangeVelocity {
        public Vector3f angularVelocity = null;
        public Vector3f linearVelocity = null;
    }


    @MappedContainer
    public static class CreateBody {
        public CreateBody(EntityRef target, RigidBodyComponent component) {
            this.target = target;
            this.mass = component.getMass();
            this.kinematic = component.getKinematic();
//            this.velocity.set(component.getVelocity());
            this.angularFactor.set(component.getAngularFactor());
            this.linearFactor.set(component.getLinearFactor());
            this.friction = component.getFriction();
            this.restitution = component.getRestitution();
//            this.angularVelocity.set(component.getAngularVelocity());
            this.collidesWith = component.getCollidesWith();
        }

        public EntityRef target;
        public float mass = 10.0f;
        public boolean kinematic;
        public Vector3f velocity = new Vector3f();
        public Vector3f angularFactor = new Vector3f(1f, 1f, 1f);
        public Vector3f linearFactor = new Vector3f(1f, 1f, 1f);
        public float friction = 0.5f;
        public float restitution = 0f;
        public Vector3f angularVelocity = new Vector3f();
        public int collisionGroup;
        public int collidesWith;

    }



}
