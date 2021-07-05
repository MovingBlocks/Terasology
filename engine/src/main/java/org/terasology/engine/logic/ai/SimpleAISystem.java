// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.ai;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.characters.CharacterMoveInputEvent;
import org.terasology.engine.logic.characters.CharacterMovementComponent;
import org.terasology.engine.logic.characters.events.HorizontalCollisionEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.WorldProvider;

@RegisterSystem(RegisterMode.AUTHORITY)
public class SimpleAISystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private WorldProvider worldProvider;
    @In
    private EntityManager entityManager;
    private Random random = new FastRandom();
    @In
    private Time time;
    @In
    private LocalPlayer localPlayer;

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(SimpleAIComponent.class,
                CharacterMovementComponent.class, LocationComponent.class)) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            Vector3f worldPos = location.getWorldPosition(new Vector3f());

            // Skip this AI if not in a loaded chunk
            if (!worldProvider.isBlockRelevant(worldPos)) {
                continue;
            }
            SimpleAIComponent ai = entity.getComponent(SimpleAIComponent.class);

            Vector3f drive = new Vector3f();
            // TODO: shouldn't use local player, need some way to find nearest player
            if (localPlayer != null) {
                final Vector3f playerPosition = localPlayer.getPosition(new Vector3f());
                double distanceToPlayer = worldPos.distanceSquared(playerPosition);

                if (distanceToPlayer > 6 && distanceToPlayer < 16) {
                    // Head to player
                    ai.movementTarget.set(playerPosition);
                    ai.followingPlayer = true;
                    entity.saveComponent(ai);
                } else {
                    // Random walk
                    if (time.getGameTimeInMs() - ai.lastChangeOfDirectionAt > 12000 || ai.followingPlayer) {
                        ai.movementTarget.set(worldPos.x + random.nextFloat(-500.0f, 500.0f), worldPos.y,
                                worldPos.z + random.nextFloat(-500.0f, 500.0f));
                        ai.lastChangeOfDirectionAt = time.getGameTimeInMs();
                        ai.followingPlayer = false;
                        entity.saveComponent(ai);
                    }
                }

                Vector3f targetDirection = new Vector3f();
                targetDirection.sub(ai.movementTarget, worldPos);
                targetDirection.normalize();
                drive.set(targetDirection);

                float yaw = (float) Math.atan2(targetDirection.x, targetDirection.z);
                location.setLocalRotation(new Quaternionf().setAngleAxis(yaw, 0, 1, 0));
                entity.saveComponent(location);
            }
            entity.send(new CharacterMoveInputEvent(0, 0, 0, drive,
                    false, false, false, time.getGameDeltaInMs()));
        }
    }

    @ReceiveEvent(components = SimpleAIComponent.class)
    public void onBump(HorizontalCollisionEvent event, EntityRef entity) {
        CharacterMovementComponent moveComp = entity.getComponent(CharacterMovementComponent.class);
        if (moveComp != null && moveComp.grounded) {
            moveComp.jump = true;
            entity.saveComponent(moveComp);
        }
    }
}
