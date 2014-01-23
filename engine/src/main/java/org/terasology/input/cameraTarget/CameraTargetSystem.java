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

import com.google.common.base.Objects;
import org.terasology.registry.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.registry.In;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.BlockEntityRegistry;

import javax.vecmath.Vector3f;
import java.util.Arrays;

/**
 * @author Immortius
 */
public class CameraTargetSystem implements ComponentSystem {

    // TODO: This should come from somewhere, probably player entity?
    public static final float TARGET_DISTANCE = 5f;

    @In
    private LocalPlayer localPlayer;

    @In
    private BlockEntityRegistry blockRegistry;

    private EntityRef target = EntityRef.NULL;
    private Vector3i targetBlockPos;
    private Vector3f hitPosition = new Vector3f();
    private Vector3f hitNormal = new Vector3f();
    private CollisionGroup[] filter = {StandardCollisionGroup.DEFAULT, StandardCollisionGroup.WORLD};
    private float eyeFocusDistance;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    public boolean isTargetAvailable() {
        return target.exists() || targetBlockPos != null;
    }

    public EntityRef getTarget() {
        if (!target.exists() && targetBlockPos != null && blockRegistry != null) {
            target = blockRegistry.getEntityAt(targetBlockPos);
        }
        return target;
    }

    public Vector3f getHitPosition() {
        return new Vector3f(hitPosition);
    }

    public Vector3f getHitNormal() {
        return new Vector3f(hitNormal);

    }

    public void setFilter(CollisionGroup... filter) {
        this.filter = Arrays.copyOf(filter, filter.length);
    }

    public void update(float delta) {
        // Repair lost target
        // TODO: Improvements to temporary chunk handling will remove the need for this
        if (!target.exists() && targetBlockPos != null && blockRegistry != null) {
            target = blockRegistry.getEntityAt(targetBlockPos);
        }
        boolean lostTarget = false;
        if (!target.exists()) {
            targetBlockPos = null;
            lostTarget = true;
        }

        // TODO: This will change when camera are handled better (via a component)
        Camera camera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();

        Physics physicsRenderer = CoreRegistry.get(Physics.class);
        HitResult hitInfo = physicsRenderer.rayTrace(new Vector3f(camera.getPosition()), new Vector3f(camera.getViewingDirection()), TARGET_DISTANCE, filter);
        updateEyeDistance(hitInfo, delta);
        Vector3i newBlockPos = null;

        EntityRef newTarget = EntityRef.NULL;
        if (hitInfo.isHit()) {
            newTarget = hitInfo.getEntity();
            hitPosition = hitInfo.getHitPoint();
            hitNormal = hitInfo.getHitNormal();
            if (hitInfo.isWorldHit()) {
                newBlockPos = new Vector3i(hitInfo.getBlockPosition());
            }
        }
        if (!Objects.equal(target, newTarget) || lostTarget) {
            EntityRef oldTarget = target;
            oldTarget.send(new CameraOutEvent());
            newTarget.send(new CameraOverEvent());
            localPlayer.getCharacterEntity().send(new CameraTargetChangedEvent(oldTarget, newTarget));
        }
        target = newTarget;
        targetBlockPos = newBlockPos;
    }

    private void updateEyeDistance(HitResult hitInfo, float delta) {
        if (hitInfo.isHit()) {
            Vector3f playerToTargetRay = new Vector3f();
            playerToTargetRay.sub(hitInfo.getHitPoint(), localPlayer.getPosition());

            if (eyeFocusDistance == Float.MAX_VALUE) {
                eyeFocusDistance = playerToTargetRay.length();
            } else {
                eyeFocusDistance = TeraMath.lerpf(eyeFocusDistance, playerToTargetRay.length(), delta * 20.0f);
            }
        } else {
            eyeFocusDistance = Float.MAX_VALUE;
        }
    }

    public String toString() {
        Camera camera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
        if (targetBlockPos != null) {
            return String.format("From: %f %f %f, Dir: %f %f %f, Hit %d %d %d %f %f %f",
                    camera.getPosition().x,
                    camera.getPosition().y,
                    camera.getPosition().z,
                    camera.getViewingDirection().x,
                    camera.getViewingDirection().y,
                    camera.getViewingDirection().z,
                    targetBlockPos.x,
                    targetBlockPos.y,
                    targetBlockPos.z,
                    hitPosition.x,
                    hitPosition.y,
                    hitPosition.z);
        }
        return "";
    }

    public Vector3i getTargetBlockPosition() {
        if (targetBlockPos != null) {
            return new Vector3i(targetBlockPos);
        }
        return new Vector3i(hitPosition, 0.5f);
    }

    public float getEyeFocusDistance() {
        return eyeFocusDistance;
    }
}
