// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.cameraTarget;

import org.joml.Vector3f;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.players.FirstPersonHeldItemMountPointComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.logic.players.PlayerTargetChangedEvent;
import org.terasology.engine.physics.Physics;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.world.BlockEntityRegistry;

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
