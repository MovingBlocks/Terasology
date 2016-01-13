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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.PlayerTargetChangedEvent;
import org.terasology.math.geom.Vector3f;
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

    @Override
    public void update(float delta) {
        EntityRef charEntity = player.getCharacterEntity();
        if (charEntity.exists()) {
            Vector3f cameraPos = player.getViewPosition();
            CharacterComponent charComp = charEntity.getComponent(CharacterComponent.class);

            if (charComp != null) {
                Vector3f dir = player.getViewDirection();
                float maxDist = charComp.interactionRange;
                if (targetSystem.updateTarget(cameraPos, dir, maxDist)) {
                    EntityRef oldTarget = targetSystem.getPreviousTarget();
                    EntityRef newTarget = targetSystem.getTarget();
                    charEntity.send(new PlayerTargetChangedEvent(oldTarget, newTarget));
                }
            }
        }
    }
}
