/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.input.cameraTarget;

import org.joml.Vector3f;
import org.joml.Vector3ic;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.players.FirstPersonHeldItemMountPointComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.PlayerTargetChangedEvent;
import org.terasology.math.JomlUtil;
import org.terasology.physics.Physics;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.world.BlockEntityRegistry;

/**
 * Tracks the targeted entity (within interaction range) of the local player.
 */
@RegisterSystem
@Share(PlayerTargetSystem.class)
public class PlayerTargetSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private TargetSystem targetSystem;

    @In
    private Physics physics;

    @In
    private BlockEntityRegistry blockRegistry;

    @In
    private LocalPlayer player;

    @Override
    public void initialise() {
        targetSystem = new TargetSystem(blockRegistry, physics);
    }

    public EntityRef getTarget() {
        return targetSystem.getTarget();
    }

    /**
     * Get the position of the block that is currently targeted.
     * @return the position of the block, as a Vector3i
     */
    public Vector3ic getTargetBlockPosition() {
        return targetSystem.getTargetBlockPosition();
    }

    @Override
    public void update(float delta) {
        EntityRef charEntity = player.getCharacterEntity();
        if (charEntity.exists()) {
            Vector3f cameraPos = player.getViewPosition(new Vector3f());
            CharacterComponent charComp = charEntity.getComponent(CharacterComponent.class);

            if (charComp != null) {
                Vector3f dir = player.getViewDirection(new Vector3f());
                float maxDist = charComp.interactionRange;
                FirstPersonHeldItemMountPointComponent heldItemMountPoint = player.getCameraEntity().getComponent(FirstPersonHeldItemMountPointComponent.class);
                if (heldItemMountPoint != null && heldItemMountPoint.isTracked()) {
                    maxDist = heldItemMountPoint.translate.length() + 0.25f;
                    dir = new Vector3f(heldItemMountPoint.translate).normalize();
                }
                if (targetSystem.updateTarget(cameraPos, dir, maxDist)) {
                    EntityRef oldTarget = targetSystem.getPreviousTarget();
                    EntityRef newTarget = targetSystem.getTarget();
                    charEntity.send(new PlayerTargetChangedEvent(oldTarget, newTarget));
                }
            }
        }
    }
}
