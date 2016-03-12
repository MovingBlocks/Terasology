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

package org.terasology.input.cameraTarget;

import java.util.Arrays;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.world.BlockEntityRegistry;

public class TargetSystem {

    private final BlockEntityRegistry blockRegistry;
    private final Physics physics;

    private EntityRef target = EntityRef.NULL;
    private EntityRef prevTarget = EntityRef.NULL;

    private Vector3i targetBlockPos;
    private CollisionGroup[] filter = {StandardCollisionGroup.DEFAULT, StandardCollisionGroup.WORLD};

    public TargetSystem(BlockEntityRegistry blockRegistry, Physics physics) {
        this.blockRegistry = blockRegistry;
        this.physics = physics;
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
            if (targetBlockPos != null) {
                if (targetBlockPos.equals(hitInfo.getBlockPosition())) {
                    return false;
                }
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
            location.setLocalPosition(targetBlockPos.toVector3f());
        }

        return true;
    }
}

