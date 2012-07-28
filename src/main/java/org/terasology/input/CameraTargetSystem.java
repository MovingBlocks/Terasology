/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.input;

import com.google.common.base.Objects;
import org.terasology.components.block.BlockComponent;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.world.BlockEntityRegistry;
import org.terasology.logic.world.WorldProvider;
import org.terasology.math.Vector3i;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.physics.BulletPhysicsRenderer;
import org.terasology.rendering.physics.HitResult;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
public class CameraTargetSystem implements ComponentSystem {

    // TODO: This should come from somewhere, probably player entity?
    public static final float TARGET_DISTANCE = 5f;

    private WorldProvider worldProvider;
    private LocalPlayer localPlayer;
    private BlockEntityRegistry blockRegistry;
    private EntityRef target = EntityRef.NULL;
    private Vector3i targetBlockPos = null;
    private Vector3f hitPosition = new Vector3f();
    private Vector3f hitNormal = new Vector3f();

    public void initialise() {
        localPlayer = CoreRegistry.get(LocalPlayer.class);
        worldProvider = CoreRegistry.get(WorldProvider.class);
        blockRegistry = CoreRegistry.get(BlockEntityRegistry.class);
    }

    @Override
    public void shutdown() {
    }

    public boolean isTargetAvailable() {
        return target.exists() || targetBlockPos != null;
    }

    public EntityRef getTarget() {
        if (targetBlockPos != null && !target.exists()) {
            return blockRegistry.getOrCreateEntityAt(targetBlockPos);
        }
        return target;
    }

    public Vector3f getHitPosition() {
        return new Vector3f(hitPosition);
    }

    public Vector3f getHitNormal() {
        return new Vector3f(hitNormal);

    }

    public void update() {
        // Repair lost target
        // TODO: Improvements to temporary chunk handling will remove the need for this
        if (!target.exists() && targetBlockPos != null) {
            target = blockRegistry.getOrCreateEntityAt(targetBlockPos);
        }
        boolean lostTarget = false;
        if (!target.exists()) {
            targetBlockPos = null;
            lostTarget = true;
        }

        // TODO: This will change when camera are handled better (via a component)
        Camera camera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
        // TODO: Check for non-block targets once we have support for that

        BulletPhysicsRenderer physicsRenderer = CoreRegistry.get(BulletPhysicsRenderer.class);
        HitResult hitInfo = physicsRenderer.rayTrace(new Vector3f(camera.getPosition()), new Vector3f(camera.getViewingDirection()), TARGET_DISTANCE);
        Vector3i newBlockPos = null;

        if (hitInfo.isHit()) {
            hitPosition = hitInfo.getHitPoint();
            hitNormal = hitInfo.getHitNormal();


            BlockComponent blockComp = hitInfo.getEntity().getComponent(BlockComponent.class);
            if (blockComp != null) {
                newBlockPos = new Vector3i(blockComp.getPosition());
            }
        }
        if (!Objects.equal(targetBlockPos, newBlockPos) || lostTarget) {
            EntityRef oldTarget = target;

            target = EntityRef.NULL;
            targetBlockPos = newBlockPos;
            if (newBlockPos != null) {
                target = hitInfo.getEntity();
            }

            oldTarget.send(new CameraOutEvent());
            target.send(new CameraOverEvent());
            localPlayer.getEntity().send(new CameraTargetChangedEvent(oldTarget, target));
        }
    }

    public String toString() {
        Camera camera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
        if (targetBlockPos != null) {
            return String.format("From: %f %f %f, Dir: %f %f %f, Hit %d %d %d %f %f %f", camera.getPosition().x, camera.getPosition().y, camera.getPosition().z, camera.getViewingDirection().x, camera.getViewingDirection().y, camera.getViewingDirection().z, targetBlockPos.x, targetBlockPos.y, targetBlockPos.z, hitPosition.x, hitPosition.y, hitPosition.z);
        }
        return "";
    }
}
