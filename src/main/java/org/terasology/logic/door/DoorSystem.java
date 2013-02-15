/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.logic.door;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.audio.Sound;
import org.terasology.components.BlockParticleEffectComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityInfoComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.events.DamageEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockRegionComponent;
import org.terasology.world.block.management.BlockManager;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@RegisterComponentSystem
public class DoorSystem implements EventHandlerSystem {
    private static final Logger logger = LoggerFactory.getLogger(DoorSystem.class);

    @In
    private WorldProvider worldProvider;
    @In
    private EntityManager entityManager;
    @In
    private AudioManager audioManager;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {DoorComponent.class, ItemComponent.class})
    public void placeDoor(ActivateEvent event, EntityRef entity) {
        DoorComponent door = entity.getComponent(DoorComponent.class);
        BlockComponent targetBlockComp = event.getTarget().getComponent(BlockComponent.class);
        if (targetBlockComp == null) {
            event.cancel();
            return;
        }

        Vector3f horizDir = new Vector3f(event.getDirection());
        horizDir.y = 0;
        Side facingDir = Side.inDirection(horizDir);
        if (!facingDir.isHorizontal()) {
            event.cancel();
            return;
        }

        Vector3f offset = new Vector3f(event.getHitPosition());
        offset.sub(targetBlockComp.getPosition().toVector3f());
        Side offsetDir = Side.inDirection(offset);

        Vector3i primePos = new Vector3i(targetBlockComp.getPosition());
        primePos.add(offsetDir.getVector3i());
        Block primeBlock = worldProvider.getBlock(primePos);
        if (!primeBlock.isReplacementAllowed()) {
            event.cancel();
            return;
        }
        Block belowBlock = worldProvider.getBlock(primePos.x, primePos.y - 1, primePos.z);
        Block aboveBlock = worldProvider.getBlock(primePos.x, primePos.y + 1, primePos.z);

        // Determine top and bottom blocks
        Vector3i bottomBlockPos = null;
        Block bottomBlock = null;
        Vector3i topBlockPos = null;
        Block topBlock = null;
        if (belowBlock.isReplacementAllowed()) {
            bottomBlockPos = new Vector3i(primePos.x, primePos.y - 1, primePos.z);
            bottomBlock = belowBlock;
            topBlockPos = primePos;
            topBlock = primeBlock;
        } else if (aboveBlock.isReplacementAllowed()) {
            bottomBlockPos = primePos;
            bottomBlock = primeBlock;
            topBlockPos = new Vector3i(primePos.x, primePos.y + 1, primePos.z);
            topBlock = aboveBlock;
        } else {
            event.cancel();
            return;
        }

        Side attachSide = determineAttachSide(facingDir, offsetDir, bottomBlockPos, topBlockPos);
        if (attachSide == null) {
            event.cancel();
            return;
        }

        Side closedSide = facingDir.reverse();
        if (closedSide == attachSide || closedSide.reverse() == attachSide) {
            closedSide = attachSide.rotateClockwise(1);
        }

        worldProvider.setBlock(bottomBlockPos, door.bottomBlockFamily.getBlockFor(closedSide, Side.TOP), bottomBlock);
        worldProvider.setBlock(topBlockPos, door.topBlockFamily.getBlockFor(closedSide, Side.TOP), topBlock);

        EntityRef newDoor = entityManager.copy(entity);
        newDoor.addComponent(new BlockRegionComponent(Region3i.createBounded(bottomBlockPos, topBlockPos)));
        Vector3f doorCenter = bottomBlockPos.toVector3f();
        doorCenter.y += 0.5f;
        newDoor.addComponent(new LocationComponent(doorCenter));
        DoorComponent newDoorComp = newDoor.getComponent(DoorComponent.class);
        newDoorComp.closedDirection = closedSide;
        newDoorComp.openDirection = attachSide.reverse();
        newDoorComp.isOpen = false;
        newDoor.saveComponent(newDoorComp);
        newDoor.removeComponent(ItemComponent.class);
        audioManager.playSound(Assets.getSound("engine:PlaceBlock"), 0.5f);
        logger.info("Closed Direction: {}", newDoorComp.closedDirection);
        logger.info("Open Direction: {}", newDoorComp.openDirection);
    }

    private Side determineAttachSide(Side facingDir, Side offsetDir, Vector3i bottomBlockPos, Vector3i topBlockPos) {
        Side attachSide = null;
        if (offsetDir.isHorizontal()) {
            if (canAttachTo(topBlockPos, offsetDir.reverse()) && canAttachTo(bottomBlockPos, offsetDir.reverse())) {
                attachSide = offsetDir.reverse();
            }
        }
        if (attachSide == null) {
            Side clockwise = facingDir.rotateClockwise(1);
            if (canAttachTo(topBlockPos, clockwise) && canAttachTo(bottomBlockPos, clockwise)) {
                attachSide = clockwise;
            }
        }
        if (attachSide == null) {
            Side anticlockwise = facingDir.rotateClockwise(-1);
            if (canAttachTo(topBlockPos, anticlockwise) && canAttachTo(bottomBlockPos, anticlockwise)) {
                attachSide = anticlockwise;
            }
        }
        return attachSide;
    }

    private boolean canAttachTo(Vector3i doorPos, Side side) {
        Vector3i adjacentBlockPos = new Vector3i(doorPos);
        adjacentBlockPos.add(side.getVector3i());
        Block adjacentBlock = worldProvider.getBlock(adjacentBlockPos);
        return adjacentBlock.isAttachmentAllowed();
    }

    @ReceiveEvent(components = {DoorComponent.class, BlockRegionComponent.class, LocationComponent.class})
    public void onFrob(ActivateEvent event, EntityRef entity) {
        DoorComponent door = entity.getComponent(DoorComponent.class);
        Side oldDirection = (door.isOpen) ? door.openDirection : door.closedDirection;
        Side newDirection = (door.isOpen) ? door.closedDirection : door.openDirection;
        BlockRegionComponent regionComp = entity.getComponent(BlockRegionComponent.class);
        worldProvider.setBlock(regionComp.region.min(), door.bottomBlockFamily.getBlockFor(newDirection, Side.TOP), door.bottomBlockFamily.getBlockFor(oldDirection, Side.TOP));
        worldProvider.setBlock(regionComp.region.max(), door.topBlockFamily.getBlockFor(newDirection, Side.TOP), door.topBlockFamily.getBlockFor(oldDirection, Side.TOP));
        Sound sound = (door.isOpen) ? door.closeSound : door.openSound;
        if (sound != null) {
            LocationComponent loc = entity.getComponent(LocationComponent.class);
            audioManager.playSound(sound, loc.getWorldPosition(), 10, 1);
        }

        door.isOpen = !door.isOpen;
        entity.saveComponent(door);
    }

    @ReceiveEvent(components = {DoorComponent.class, LocationComponent.class})
    public void onDamaged(DamageEvent event, EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        DoorComponent doorComponent = entity.getComponent(DoorComponent.class);
        Vector3f center = location.getWorldPosition();
        EntityRef particlesEntity = entityManager.create();
        particlesEntity.addComponent(new LocationComponent(center));

        BlockParticleEffectComponent particleEffect = new BlockParticleEffectComponent();
        particleEffect.spawnCount = 64;
        particleEffect.blockType = doorComponent.bottomBlockFamily;
        particleEffect.initialVelocityRange.set(4, 4, 4);
        particleEffect.spawnRange.set(0.3f, 0.3f, 0.3f);
        particleEffect.destroyEntityOnCompletion = true;
        particleEffect.minSize = 0.05f;
        particleEffect.maxSize = 0.1f;
        particleEffect.minLifespan = 1f;
        particleEffect.maxLifespan = 1.5f;
        particleEffect.targetVelocity.set(0, -5, 0);
        particleEffect.acceleration.set(2f, 2f, 2f);
        particleEffect.collideWithBlocks = true;
        particlesEntity.addComponent(particleEffect);

        audioManager.playSound(Assets.getSound("engine:Dig"), 1.0f);
    }

    @ReceiveEvent(components = {DoorComponent.class, BlockRegionComponent.class})
    public void onOutOfHealth(NoHealthEvent event, EntityRef entity) {
        DoorComponent door = entity.getComponent(DoorComponent.class);
        BlockRegionComponent blockRegionComponent = entity.getComponent(BlockRegionComponent.class);
        for (Vector3i blockPos : blockRegionComponent.region) {
            worldProvider.setBlock(blockPos, BlockManager.getInstance().getAir(), worldProvider.getBlock(blockPos));
        }
        EntityInfoComponent entityInfo = entity.getComponent(EntityInfoComponent.class);
        if (entityInfo != null) {
            EntityRef doorItem = entityManager.create(entityInfo.parentPrefab);
            if (event.getInstigator().exists()) {
                event.getInstigator().send(new ReceiveItemEvent(doorItem));
            }
            ItemComponent itemComp = doorItem.getComponent(ItemComponent.class);
            if (itemComp != null && !itemComp.container.exists()) {
                doorItem.destroy();
            }
        }
        entity.destroy();
        audioManager.playSound(Assets.getSound("engine:RemoveBlock"), 0.6f);
    }
}
