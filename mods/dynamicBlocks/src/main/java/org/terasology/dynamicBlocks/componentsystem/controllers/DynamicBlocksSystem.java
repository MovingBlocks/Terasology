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
package org.terasology.dynamicBlocks.componentsystem.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.dynamicBlocks.components.DynamicBlockComponent;
import org.terasology.dynamicBlocks.componentsystem.entityfactory.DynamicFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventPriority;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.events.ActivateEvent;
import org.terasology.game.Timer;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.physics.BulletPhysics;
import org.terasology.physics.HitResult;
import org.terasology.physics.MovedEvent;
import org.terasology.physics.shapes.BoxShapeComponent;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector3f;

/**
 * @author Pencilcheck <pennsu@gmail.com>
 */
@RegisterSystem
public final class DynamicBlocksSystem implements UpdateSubscriberSystem {
    @In
    private LocalPlayer localPlayer;

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    @In
    private BulletPhysics physics;

    @In
    private Timer timer;

    DynamicFactory dynamicFactory;

    private static final Logger logger = LoggerFactory.getLogger(DynamicBlocksSystem.class);

    @Override
    public void initialise() {
        dynamicFactory = new DynamicFactory();
        dynamicFactory.setEntityManager(entityManager);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {DynamicBlockComponent.class, ItemComponent.class}, priority = EventPriority.PRIORITY_HIGH)
    public void onPlaceFunctional(ActivateEvent event, EntityRef item) {
        DynamicBlockComponent functionalItem = item.getComponent(DynamicBlockComponent.class);

        Side surfaceDir = Side.inDirection(event.getHitNormal());
        Side secondaryDirection = TeraMath.getSecondaryPlacementDirection(event.getDirection(), event.getHitNormal());

        // TODO: Check whether it is possible to place it (e.g. boat cannot be placed on land)

        Vector3f newPosition = new Vector3f(localPlayer.getPosition().x + localPlayer.getViewDirection().x * 1.5f,
                localPlayer.getPosition().y + localPlayer.getViewDirection().y * 1.5f,
                localPlayer.getPosition().z + localPlayer.getViewDirection().z * 1.5f
        );

        if (worldProvider.getBlock(newPosition).isPenetrable()) {
            /*event.getTarget().getComponent(LocationComponent.class).getWorldPosition()*/
            EntityRef entity = dynamicFactory.generateDynamicBlock(newPosition, functionalItem.getDynamicType());

            //functionalEntity.send(new ImpulseEvent(new Vector3f(localPlayer.getViewDirection().x, localPlayer.getViewDirection().y, localPlayer.getViewDirection().z)));

            /*
            if (!placeBlock(functionalItem.functionalFamily, event.getTarget().getComponent(BlockComponent.class).getPosition(), surfaceDir, secondaryDirection, functionalItem)) {
                event.cancel();
            }
            */
        }
    }

    /*
    private boolean placeBlock(BlockFamily type, Vector3i targetBlock, Side surfaceDirection, Side secondaryDirection, BlockItemComponent blockItem) {
        if (type == null)
            return true;

        Vector3i placementPos = new Vector3i(targetBlock);
        placementPos.add(surfaceDirection.getVector3i());

        Block block = type.getBlockFor(surfaceDirection, secondaryDirection);
        if (block == null)
            return false;

        if (canPlaceBlock(block, targetBlock, placementPos)) {
            if (blockEntityRegistry.setBlock(placementPos, block, worldProvider.getBlock(placementPos), blockItem.placedEntity)) {
                AudioManager.play(new AssetUri(AssetType.SOUND, "engine:PlaceBlock"), 0.5f);
                if (blockItem.placedEntity.exists()) {
                    blockItem.placedEntity = EntityRef.NULL;
                }
                return true;
            }
        }
        return false;
    }

    private boolean canPlaceBlock(Block block, Vector3i targetBlock, Vector3i blockPos) {
        Block centerBlock = worldProvider.getBlock(targetBlock.x, targetBlock.y, targetBlock.z);

        if (!centerBlock.isAttachmentAllowed()) {
            return false;
        }

        Block adjBlock = worldProvider.getBlock(blockPos.x, blockPos.y, blockPos.z);
        if (!adjBlock.isReplacementAllowed() || adjBlock.isTargetable()) {
            return false;
        }

        // Prevent players from placing blocks inside their bounding boxes
        if (!block.isPenetrable()) {
            return !CoreRegistry.get(BulletPhysics.class).scanArea(block.getBounds(blockPos), Lists.<CollisionGroup>newArrayList(StandardCollisionGroup.DEFAULT, StandardCollisionGroup.CHARACTER)).iterator().hasNext();
        }
        return true;
    }
    */
    @ReceiveEvent(components = {DynamicBlockComponent.class, LocationComponent.class})
    public void onDestroy(final RemovedComponentEvent event, final EntityRef entity) {
        DynamicBlockComponent comp = entity.getComponent(DynamicBlockComponent.class);
        if (comp.collider != null) {
            physics.removeCollider(comp.collider);
        }
    }

    public void update(float delta) {
        for (EntityRef entity : entityManager.iteratorEntities(DynamicBlockComponent.class, LocationComponent.class)) {
            DynamicBlockComponent loco = entity.getComponent(DynamicBlockComponent.class);
            LocationComponent location = entity.getComponent(LocationComponent.class);
            Vector3f worldPos = location.getWorldPosition();

            // Skip this System if not in a loaded chunk
            if (!worldProvider.isBlockActive(worldPos))
                continue;

            if (!localPlayer.isValid())
                return;

            if (standingOn(entity)) {
                Vector3f movementDirection = localPlayer.getViewDirection();
                float speed = movementDirection.length();
                movementDirection = new Vector3f(movementDirection.x, 0, movementDirection.z);
                movementDirection.normalize();
                movementDirection.scale(speed);

                Vector3f desiredVelocity = new Vector3f(movementDirection);
                desiredVelocity.scale(loco.getMaximumSpeed());

                entity.send(new CharacterMoveInputEvent(0, 0, 0, desiredVelocity, false, false));
            } else {
                entity.send(new CharacterMoveInputEvent(0, 0, 0, new Vector3f(), false, false));
            }
        }
    }

    @ReceiveEvent(components = {LocationComponent.class, DynamicBlockComponent.class})
    public void onMove(MovedEvent event, EntityRef entity) {
        if (standingOn(entity)) {
            // update player position
            LocationComponent player_location = localPlayer.getCharacterEntity().getComponent(LocationComponent.class);
            Vector3f location = player_location.getWorldPosition();
            location.add(event.getDelta());
            player_location.setWorldPosition(location);
            localPlayer.getCharacterEntity().saveComponent(player_location);
        }
    }

    public boolean standingOn(EntityRef entity) {
        BoxShapeComponent boxshape = localPlayer.getCharacterEntity().getComponent(BoxShapeComponent.class);
        // Only move when someone is standing on it
        HitResult hit = physics.rayTrace(localPlayer.getPosition(), new Vector3f(0, -1, 0), boxshape.extents.y);
        return hit.isHit() && hit.getEntity() == entity;
    }
}
