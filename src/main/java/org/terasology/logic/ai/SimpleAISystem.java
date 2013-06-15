/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.logic.ai;

import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.events.HorizontalCollisionEvent;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.Timer;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.utilities.procedural.FastRandom;
import org.terasology.world.WorldProvider;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class SimpleAISystem implements UpdateSubscriberSystem {

    private WorldProvider worldProvider;
    private EntityManager entityManager;
    private FastRandom random = new FastRandom();
    private Timer timer;

    @Override
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        timer = CoreRegistry.get(Timer.class);
        worldProvider = CoreRegistry.get(WorldProvider.class);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(SimpleAIComponent.class, CharacterMovementComponent.class, LocationComponent.class)) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            Vector3f worldPos = location.getWorldPosition();

            // Skip this AI if not in a loaded chunk
            if (!worldProvider.isBlockActive(worldPos)) {
                continue;
            }
            SimpleAIComponent ai = entity.getComponent(SimpleAIComponent.class);

            Vector3f drive = new Vector3f();
            // TODO: shouldn't use local player, need some way to find nearest player
            LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
            if (localPlayer != null) {
                Vector3f dist = new Vector3f(worldPos);
                dist.sub(localPlayer.getPosition());
                double distanceToPlayer = dist.lengthSquared();

                if (distanceToPlayer > 6 && distanceToPlayer < 16) {
                    // Head to player
                    ai.movementTarget.set(localPlayer.getPosition());
                    ai.followingPlayer = true;
                    entity.saveComponent(ai);
                } else {
                    // Random walk
                    if (CoreRegistry.get(Timer.class).getTimeInMs() - ai.lastChangeOfDirectionAt > 12000 || ai.followingPlayer) {
                        ai.movementTarget.set(worldPos.x + random.randomFloat() * 500, worldPos.y, worldPos.z + random.randomFloat() * 500);
                        ai.lastChangeOfDirectionAt = timer.getTimeInMs();
                        ai.followingPlayer = false;
                        entity.saveComponent(ai);
                    }
                }

                Vector3f targetDirection = new Vector3f();
                targetDirection.sub(ai.movementTarget, worldPos);
                targetDirection.normalize();
                drive.set(targetDirection);

                float yaw = (float) Math.atan2(targetDirection.x, targetDirection.z);
                AxisAngle4f axisAngle = new AxisAngle4f(0, 1, 0, yaw);
                location.getLocalRotation().set(axisAngle);
                entity.saveComponent(location);
            }
            entity.send(new CharacterMoveInputEvent(0, 0, 0, drive, false, false));
        }
    }

    @ReceiveEvent(components = {SimpleAIComponent.class})
    public void onBump(HorizontalCollisionEvent event, EntityRef entity) {
        CharacterMovementComponent moveComp = entity.getComponent(CharacterMovementComponent.class);
        if (moveComp != null && moveComp.grounded) {
            moveComp.jump = true;
            entity.saveComponent(moveComp);
        }
    }
}
