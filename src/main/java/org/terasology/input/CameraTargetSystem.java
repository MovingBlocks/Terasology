/*
 * Copyright 2012
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
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.BlockRaytracer;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.world.BlockEntityRegistry;
import org.terasology.logic.world.WorldProvider;
import org.terasology.math.Vector3i;
import org.terasology.model.structures.RayBlockIntersection;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;

/**
 * @author Immortius
 */
public class CameraTargetSystem implements ComponentSystem {

    private WorldProvider worldProvider;
    private LocalPlayer localPlayer;
    private BlockEntityRegistry blockRegistry;
    private EntityRef target = EntityRef.NULL;
    private Vector3i targetBlockPos = null;

    public void initialise() {
        localPlayer = CoreRegistry.get(LocalPlayer.class);
        worldProvider = CoreRegistry.get(WorldProvider.class);
        blockRegistry = CoreRegistry.get(BlockEntityRegistry.class);
    }

    @Override
    public void shutdown() {
    }

    public EntityRef getTarget() {
        return target;
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

        RayBlockIntersection.Intersection hitInfo = BlockRaytracer.trace(camera.getPosition(), camera.getViewingDirection(), worldProvider);
        Vector3i newBlockPos = (hitInfo != null) ? hitInfo.getBlockPosition() : null;

        if (!Objects.equal(targetBlockPos, newBlockPos) || lostTarget) {
            EntityRef oldTarget = target;

            target = EntityRef.NULL;
            targetBlockPos = newBlockPos;
            if (newBlockPos != null) {
                target = blockRegistry.getOrCreateEntityAt(targetBlockPos);
            }

            oldTarget.send(new CameraOutEvent());
            target.send(new CameraOverEvent());
            localPlayer.getEntity().send(new CameraTargetChangedEvent(oldTarget, target));
        }
    }
}
