// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.components;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.physics.CollisionGroup;
import org.terasology.engine.physics.StandardCollisionGroup;
import org.terasology.engine.world.block.ForceBlockActive;

@ForceBlockActive
public class RigidBodyComponent implements Component {
    public static final int DIRTY_KINEMATIC = 0x1;
    public static final int DIRTY_COLLISION_FILTER = 0x2;
    public static final int DIRTY_FACTOR_FILTER = 0x4;

    protected int dirtyFlags = 0;

    //    @Replicate
    protected float mass = 10.0f;

    protected boolean kinematic;

    protected Vector3f angularFactor = new Vector3f(1f, 1f, 1f);
    protected Vector3f linearFactor = new Vector3f(1f, 1f, 1f);
    protected float friction = 0.5f;
    /**
     * The ratio of the relative velocity after impact to the relative velocity before the impact of two colliding bodies, equal to 1 for an
     * elastic collision and 0 for an inelastic collision.
     */
    protected float restitution = 0f;

    protected int collisionGroup = StandardCollisionGroup.DEFAULT.getFlag();
    protected int collidesWith = StandardCollisionGroup.DEFAULT.getFlag() | StandardCollisionGroup.WORLD.getFlag() | StandardCollisionGroup.KINEMATIC.getFlag();


    public Vector3fc getAngularFactor() {
        return this.angularFactor;
    }

    public Vector3fc getLinearFactor() {
        return this.linearFactor;
    }

    public boolean getKinematic() {
        return this.kinematic;
    }

    public float getMass() {
        return this.mass;
    }

    public int getCollisionGroup() {
        return this.collisionGroup;
    }

    public int getCollidesWith() {
        return this.collidesWith;
    }

    public float getFriction() {
        return this.friction;
    }

    public float getRestitution() {
        return this.restitution;
    }

    public void setAngularFactor(Vector3f angularFactor) {
        dirtyFlags |= DIRTY_FACTOR_FILTER;
        this.angularFactor.set(angularFactor);
    }

    public void setRestitution(float restitution) {
        dirtyFlags |= DIRTY_FACTOR_FILTER;
        this.restitution = restitution;
    }

    public void setLinearFactor(Vector3fc linearFactor) {
        dirtyFlags |= DIRTY_FACTOR_FILTER;
        this.linearFactor.set(linearFactor);
    }

    public void setFriction(float friction) {
        dirtyFlags |= DIRTY_FACTOR_FILTER;
        this.friction = friction;
    }

    public void setCollisionGroup(boolean enable, CollisionGroup... cl) {
        int oldFlags = collisionGroup;
        int flag = 0;
        for (CollisionGroup gr : cl) {
            flag |= gr.getFlag();
        }
        if (enable) {
            collisionGroup |= flag;
        } else {
            collisionGroup &= ~flag;
        }
        if ((oldFlags ^ flag) > 0) {
            dirtyFlags |= DIRTY_COLLISION_FILTER;
        }
    }

    public void setCollideWith(CollisionGroup cl) {
        if (cl.getFlag() != collisionGroup) {
            collisionGroup = cl.getFlag();
            dirtyFlags |= DIRTY_COLLISION_FILTER;
        }
    }

    public void setKinematic(boolean kinematic) {
        if (this.kinematic != kinematic) {
            this.kinematic = kinematic;
            dirtyFlags |= DIRTY_KINEMATIC;
        }
    }

    public int getDirtyFlags() {
        return dirtyFlags;
    }

    public void setDirtyFlags(boolean enable, int flag) {
        if (enable) {
            this.dirtyFlags |= flag;
        } else {
            this.dirtyFlags &= ~flag;
        }
    }
}
