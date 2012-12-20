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
package org.terasology.physics.locomotive;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.collision.dispatch.*;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.linearmath.Transform;
import org.terasology.components.actions.SpawnPrefabActionComponent;
import org.terasology.physics.HitResult;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.ItemComponent;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.events.*;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.AudioManager;
import org.terasology.math.Vector3fUtil;
import org.terasology.physics.BulletPhysics;
import org.terasology.physics.CollideEvent;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.ImpulseEvent;
import org.terasology.physics.character.CharacterMovementComponent;
import org.terasology.rendering.cameras.DefaultCamera;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockPickupComponent;

import javax.vecmath.*;

/**
 * @author Pencilcheck <pennsu@gmail.com>
 */
@RegisterComponentSystem
public final class LocomotiveSystem implements UpdateSubscriberSystem, EventHandlerSystem {


    @In
    private LocalPlayer localPlayer;

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    @In
    private BulletPhysics physics;

    private DefaultCamera playerCamera;

    private static final Logger logger = LoggerFactory.getLogger(LocomotiveSystem.class);

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {LocomotiveComponent.class, LocationComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        logger.info("Activating Locomotive System");

        LocomotiveComponent loco = entity.getComponent(LocomotiveComponent.class);
        loco.toggle();
        entity.saveComponent(loco);

        logger.info("Locomotive {}", loco.activated);
    }

    public void update(float delta) {
        for (EntityRef entity : entityManager.iteratorEntities(LocomotiveComponent.class, LocationComponent.class)) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            Vector3f worldPos = location.getWorldPosition();

            // Skip this System if not in a loaded chunk
            if (!worldProvider.isBlockActive(worldPos)) {
                continue;
            }

            LocomotiveComponent loco = entity.getComponent(LocomotiveComponent.class);

            if (!localPlayer.isValid())
                return;

            HitResult hit = physics.rayTrace(localPlayer.getPosition(), new Vector3f(0, -1, 0), 32);

            if (hit.isHit() && hit.getEntity() == entity) {
                loco.shouldMove = true;
            } else {
                loco.shouldMove = false;
            }

            entity.saveComponent(loco);

            if (loco.activated) {
                CharacterMovementComponent charMovement = localPlayer.getEntity().getComponent(CharacterMovementComponent.class);
                if (loco.shouldMove && charMovement.isGrounded) {

                    // Move forward
                    loco.movementDirection = localPlayer.getViewDirection();
                    loco.movementDirection = new Vector3f(loco.movementDirection.x, 0, loco.movementDirection.z);
                    loco.movementDirection.normalize();
                    loco.movementDirection.scale(1.5f * delta);
                    entity.saveComponent(loco);

                    worldPos.add(loco.movementDirection);
                    location.setWorldPosition(worldPos);
                    entity.saveComponent(location);

                    // Update player position
                    LocationComponent player_location = localPlayer.getEntity().getComponent(LocationComponent.class);
                    Vector3f playerPos = localPlayer.getPosition();
                    playerPos.add(loco.movementDirection);
                    player_location.setWorldPosition(playerPos);
                    localPlayer.getEntity().saveComponent(player_location);
                }
            }
        }
    }
}
