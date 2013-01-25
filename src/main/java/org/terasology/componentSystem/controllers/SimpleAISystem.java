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
package org.terasology.componentSystem.controllers;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.SimpleAIComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.HorizontalCollisionEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.logic.LocalPlayer;
import org.terasology.physics.character.CharacterMovementComponent;
import org.terasology.utilities.FastRandom;
import org.terasology.world.WorldProvider;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterComponentSystem(authorativeOnly = true)
public class SimpleAISystem implements EventHandlerSystem, UpdateSubscriberSystem {

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
        for (EntityRef entity : entityManager.iteratorEntities(SimpleAIComponent.class, CharacterMovementComponent.class, LocationComponent.class)) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            Vector3f worldPos = location.getWorldPosition();

            // Skip this AI if not in a loaded chunk
            if (!worldProvider.isBlockActive(worldPos)) {
                continue;
            }
            SimpleAIComponent ai = entity.getComponent(SimpleAIComponent.class);
            CharacterMovementComponent moveComp = entity.getComponent(CharacterMovementComponent.class);

            moveComp.getDrive().set(0, 0, 0);
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
                moveComp.setDrive(targetDirection);

                float yaw = (float) Math.atan2(targetDirection.x, targetDirection.z);
                AxisAngle4f axisAngle = new AxisAngle4f(0, 1, 0, yaw);
                location.getLocalRotation().set(axisAngle);
                entity.saveComponent(moveComp);
                entity.saveComponent(location);
            }
        }
    }

    @ReceiveEvent(components = {SimpleAIComponent.class})
    public void onBump(HorizontalCollisionEvent event, EntityRef entity) {
        CharacterMovementComponent moveComp = entity.getComponent(CharacterMovementComponent.class);
        if (moveComp != null && moveComp.isGrounded) {
            moveComp.jump = true;
            entity.saveComponent(moveComp);
        }
    }

    @ReceiveEvent(components = {SimpleAIComponent.class})
    public void onDeath(NoHealthEvent event, EntityRef entity) {
       entity.destroy();
    }

}
