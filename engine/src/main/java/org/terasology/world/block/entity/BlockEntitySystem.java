// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.entity;

import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.audio.events.PlaySoundEvent;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.health.DoDestroyEvent;
import org.terasology.engine.logic.inventory.events.DropItemEvent;
import org.terasology.engine.logic.inventory.events.GiveItemEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.world.block.entity.damage.BlockDamageModifierComponent;
import org.terasology.engine.world.block.regions.ActAsBlockComponent;
import org.terasology.engine.world.block.regions.BlockRegionComponent;
import org.terasology.engine.world.block.sounds.BlockSounds;
import org.terasology.engine.physics.events.ImpulseEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.items.BlockItemFactory;
import org.terasology.engine.world.block.items.OnBlockToItem;


/**
 * Event handler for events affecting block entities
 *
 */
@RegisterSystem
public class BlockEntitySystem extends BaseComponentSystem {

    @In
    private WorldProvider worldProvider;

    @In
    private EntityManager entityManager;

    @In
    private AudioManager audioManager;

    @In
    private BlockManager blockManager;

    private BlockItemFactory blockItemFactory;
    private Random random;

    @Override
    public void initialise() {
        blockItemFactory = new BlockItemFactory(entityManager);
        random = new FastRandom();
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_LOW)
    public void doDestroy(DoDestroyEvent event, EntityRef entity, ActAsBlockComponent blockComponent) {
        if (blockComponent.block != null) {
            commonDestroyed(event, entity, blockComponent.block.getArchetypeBlock());
        }
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_LOW)
    public void doDestroy(DoDestroyEvent event, EntityRef entity, BlockComponent blockComponent) {
        commonDestroyed(event, entity, blockComponent.block);
        worldProvider.setBlock(blockComponent.getPosition(), blockManager.getBlock(BlockManager.AIR_ID));
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_TRIVIAL)
    public void defaultDropsHandling(CreateBlockDropsEvent event, EntityRef entity, BlockComponent blockComponent) {
        Vector3ic location = blockComponent.getPosition();
        commonDefaultDropsHandling(event, entity, location, blockComponent.block);
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_TRIVIAL)
    public void defaultDropsHandling(CreateBlockDropsEvent event, EntityRef entity, ActAsBlockComponent blockComponent) {
        if (blockComponent.block != null) {
            if (entity.hasComponent(BlockRegionComponent.class)) {
                BlockRegionComponent blockRegion = entity.getComponent(BlockRegionComponent.class);
                if (blockComponent.dropBlocksInRegion) {
                    // loop through all the blocks in this region and drop them
                    for (Vector3ic location : blockRegion.region) {
                        Block blockInWorld = worldProvider.getBlock(location);
                        commonDefaultDropsHandling(event, entity, location, blockInWorld.getBlockFamily().getArchetypeBlock());
                    }
                } else {
                    // just drop the ActAsBlock block
                    Vector3i location = new Vector3i(blockRegion.region.center(new Vector3f()), RoundingMode.HALF_UP);
                    commonDefaultDropsHandling(event, entity, location, blockComponent.block.getArchetypeBlock());
                }
            } else if (entity.hasComponent(LocationComponent.class)) {
                LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
                Vector3i location = new Vector3i(locationComponent.getWorldPosition(new Vector3f()), RoundingMode.HALF_UP);
                commonDefaultDropsHandling(event, entity, location, blockComponent.block.getArchetypeBlock());
            }
        }
    }

    public void commonDefaultDropsHandling(CreateBlockDropsEvent event, EntityRef entity, Vector3ic location, Block block) {
        BlockDamageModifierComponent blockDamageModifierComponent = event.getDamageType().getComponent(BlockDamageModifierComponent.class);
        float chanceOfBlockDrop = 1;

        if (blockDamageModifierComponent != null) {
            chanceOfBlockDrop = 1 - blockDamageModifierComponent.blockAnnihilationChance;
        }

        if (random.nextFloat() < chanceOfBlockDrop) {
            EntityRef item = blockItemFactory.newInstance(block.getBlockFamily(), entity);
            entity.send(new OnBlockToItem(item));

            if (shouldDropToWorld(event, block, blockDamageModifierComponent, item)) {
                float impulsePower = 0;
                if (blockDamageModifierComponent != null) {
                    impulsePower = blockDamageModifierComponent.impulsePower;
                }

                processDropping(item, location, impulsePower);
            }
        }
    }

    private boolean shouldDropToWorld(CreateBlockDropsEvent event, Block block, BlockDamageModifierComponent blockDamageModifierComponent, EntityRef item) {
        return !isDirectPickup(block, blockDamageModifierComponent) || !giveItem(event, item);
    }

    private boolean giveItem(CreateBlockDropsEvent event, EntityRef item) {
        GiveItemEvent giveItemEvent = new GiveItemEvent(event.getInstigator());
        item.send(giveItemEvent);
        return giveItemEvent.isHandled();
    }

    private boolean isDirectPickup(Block block, BlockDamageModifierComponent blockDamageModifierComponent) {
        return block.isDirectPickup() || (blockDamageModifierComponent != null && blockDamageModifierComponent.directPickup);
    }

    private void commonDestroyed(DoDestroyEvent event, EntityRef entity, Block block) {
        entity.send(new CreateBlockDropsEvent(event.getInstigator(), event.getDirectCause(), event.getDamageType()));

        BlockDamageModifierComponent blockDamageModifierComponent = event.getDamageType().getComponent(BlockDamageModifierComponent.class);
        // TODO: Configurable via block definition
        if (blockDamageModifierComponent == null || !blockDamageModifierComponent.skipPerBlockEffects) {
            // dust particle effect
            if (entity.hasComponent(LocationComponent.class) && block.isDebrisOnDestroy()) {
                //TODO: particle system stuff should be split out better - this is effectively a stealth dependency on
                //      'CoreAssets' from the engine
                EntityBuilder dustBuilder = entityManager.newBuilder("CoreAssets:dustEffect");
                if (dustBuilder.hasComponent(LocationComponent.class)) {
                    dustBuilder.getComponent(LocationComponent.class).setWorldPosition(entity.getComponent(LocationComponent.class).getWorldPosition(new Vector3f()));
                    dustBuilder.build();
                }
            }

            // sound to play for destroyed block
            BlockSounds sounds = block.getSounds();
            if (!sounds.getDestroySounds().isEmpty()) {
                StaticSound sound = random.nextItem(sounds.getDestroySounds());
                entity.send(new PlaySoundEvent(sound, 0.6f));
            }
        }
    }

    private void processDropping(EntityRef item, Vector3ic location, float impulsePower) {
        item.send(new DropItemEvent(new Vector3f(location)));
        item.send(new ImpulseEvent(random.nextVector3f(impulsePower, new Vector3f())));
    }

}
