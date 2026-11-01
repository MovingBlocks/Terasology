// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.cameraTarget;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.physics.CollisionGroup;
import org.terasology.engine.physics.HitResult;
import org.terasology.engine.physics.Physics;
import org.terasology.engine.physics.StandardCollisionGroup;
import org.terasology.engine.world.BlockEntityRegistry;

import java.util.Arrays;

public class TargetSystem {

    private final BlockEntityRegistry blockRegistry;
    private final Physics physics;

    private EntityRef target = EntityRef.NULL;
    private EntityRef prevTarget = EntityRef.NULL;

    private Vector3i targetBlockPos;
    private CollisionGroup[] filter = {StandardCollisionGroup.DEFAULT, StandardCollisionGroup.WORLD, StandardCollisionGroup.CHARACTER};

    public TargetSystem(BlockEntityRegistry blockRegistry, Physics physics) {
        this.blockRegistry = blockRegistry;
        this.physics = physics;
    }

    /**
     * Gets the position of the block that is currently targeted. If there is currently no target, this will return
     * a null reference (isTargetAvailable() would have returned false in this case).
     * @return the target block position in world coordinates, a vector of 3 integers.
     */
    public Vector3ic getTargetBlockPosition() {
        return targetBlockPos;
    }

    public boolean isTargetAvailable() {
        return target.exists() || targetBlockPos != null;
    }

    public EntityRef getPreviousTarget() {
        return prevTarget;
    }

    public EntityRef getTarget() {
        return target;
    }

    public void setFilter(CollisionGroup... filter) {
        this.filter = Arrays.copyOf(filter, filter.length);
    }

    public boolean updateTarget(Vector3f pos, Vector3f dir, float maxDist) {

        if (targetBlockPos != null && !target.exists()) {
            target = blockRegistry.getEntityAt(targetBlockPos);
        }

        HitResult hitInfo = physics.rayTrace(pos, dir, maxDist, filter);
        EntityRef newTarget = hitInfo.getEntity();

        if (hitInfo.isWorldHit()) {
            if (targetBlockPos != null && targetBlockPos.equals(hitInfo.getBlockPosition())) {
                return false;
            }
            targetBlockPos = hitInfo.getBlockPosition();
        } else {
            if (target.equals(newTarget)) {
                return false;
            }
            targetBlockPos = null;
        }

        prevTarget = target;
        target = newTarget;

        LocationComponent location = target.getComponent(LocationComponent.class);
        if (location != null && targetBlockPos != null) {
            location.setLocalPosition(new Vector3f(targetBlockPos));
        }

        return true;
    }
}

