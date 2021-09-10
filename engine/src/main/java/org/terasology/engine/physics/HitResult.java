// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics;

import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.engine.entitySystem.entity.EntityRef;

/**
 * A HitResult holds the result of a ray-trace.
 *
 */
public class HitResult {
    private boolean hit;
    private EntityRef entity;
    private Vector3f hitPoint;
    private Vector3f hitNormal;
    private Vector3i blockPosition;
    private final boolean worldHit;

    public HitResult() {
        hit = false;
        entity = EntityRef.NULL;
        worldHit = false;
    }

    /**
     * Creates a HitResult for hitting an entity.
     *
     * @param entity
     * @param hitPoint
     * @param hitNormal
     */
    public HitResult(EntityRef entity, Vector3f hitPoint, Vector3f hitNormal) {
        this.hit = true;
        this.entity = entity;
        this.hitPoint = hitPoint;
        this.hitNormal = hitNormal;
        //This is the block were the hitPoint is inside:
        this.blockPosition = new Vector3i(hitPoint, RoundingMode.HALF_UP);
        this.worldHit = false;
    }

    /**
     * Creates a HitResult for hitting a block from the world.
     *
     * @param entity
     * @param hitPoint
     * @param hitNormal
     * @param blockPos
     */
    public HitResult(EntityRef entity, Vector3f hitPoint, Vector3f hitNormal, Vector3i blockPos) {
        this.hit = true;
        this.entity = entity;
        this.hitPoint = hitPoint;
        this.hitNormal = hitNormal;
        this.blockPosition = blockPos;
        this.worldHit = true;
    }

    /**
     * @return true if something was hit, false otherwise.
     */
    public boolean isHit() {
        return hit;
    }

    /**
     * @return The entity hit, or EntityRef.NULL if no entity was hit.
     */
    public EntityRef getEntity() {
        return entity;
    }

    /**
     * Returns the point where the hit took place.
     *
     * @return null if isHit() == false, otherwise the point where the hit took
     * place.
     */
    public Vector3f getHitPoint() {
        return hitPoint;
    }

    /**
     * Returns the normal of surface on which the hit took place.
     *
     * @return null if isHit() == false, otherwise the normal of surface on
     * which the hit took place.
     */
    public Vector3f getHitNormal() {
        return hitNormal;
    }

    /**
     * @return The block where the hit took place. If the world was hit, it will
     * return the location of the block that was hit. Otherwise it returns the
     * block location inside which the hit took place. This is different from
     * the block position of the entity that got hit!
     */
    public Vector3i getBlockPosition() {
        return blockPosition;
    }

    /**
     * Returns true if the hit has hit the world, rather than an entity.
     *
     * @return true if the world has been hit, false otherwise.
     */
    public boolean isWorldHit() {
        return worldHit;
    }
}
