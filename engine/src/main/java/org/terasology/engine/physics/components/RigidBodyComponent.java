// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.components;

import com.google.common.collect.Lists;
import org.joml.Vector3f;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.physics.CollisionGroup;
import org.terasology.engine.physics.StandardCollisionGroup;
import org.terasology.engine.world.block.ForceBlockActive;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;

@ForceBlockActive
public class RigidBodyComponent implements Component<RigidBodyComponent> {
    @Replicate
    public float mass = 10.0f;
    @Replicate
    public boolean kinematic;

    @Replicate(initialOnly = true)
    public Vector3f velocity = new Vector3f();

    @Replicate
    public Vector3f angularFactor = new Vector3f(1f, 1f, 1f);

    @Replicate
    public Vector3f linearFactor = new Vector3f(1f, 1f, 1f);

    @Replicate
    public float friction = 0.5f;

    /**
     * The ratio of the relative velocity after impact to the relative velocity before the impact of two colliding
     * bodies, equal to 1 for an elastic collision and 0 for an inelastic collision.
     */
    @Replicate
    public float restitution = 0f;

    @Replicate(initialOnly = true)
    public Vector3f angularVelocity = new Vector3f();

    @Replicate
    public CollisionGroup collisionGroup = StandardCollisionGroup.DEFAULT;
    @Replicate
    public List<CollisionGroup> collidesWith =
            Lists.<CollisionGroup>newArrayList(StandardCollisionGroup.DEFAULT, StandardCollisionGroup.WORLD,
                    StandardCollisionGroup.KINEMATIC);

    @Override
    public void copy(RigidBodyComponent other) {
        this.mass = other.mass;
        this.kinematic = other.kinematic;
        this.velocity = new Vector3f(other.velocity);
        this.angularFactor = new Vector3f(other.angularFactor);
        this.linearFactor = new Vector3f(other.linearFactor);
        this.friction = other.friction;
        this.restitution = other.restitution;
        this.angularVelocity = new Vector3f(other.angularVelocity);
        this.collisionGroup = other.collisionGroup;
        this.collidesWith = Lists.newArrayList(other.collidesWith);
    }
}
