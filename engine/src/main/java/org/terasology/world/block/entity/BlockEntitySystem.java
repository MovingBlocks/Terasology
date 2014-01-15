/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.world.block.entity;

import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.BeforeDamagedEvent;
import org.terasology.logic.health.FullHealthEvent;
import org.terasology.logic.health.NoHealthEvent;
import org.terasology.logic.health.OnDamagedEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.PickupBuilder;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.particles.BlockParticleEffectComponent;
import org.terasology.math.Vector3i;
import org.terasology.physics.events.ImpulseEvent;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BeforeBlockToItem;
import org.terasology.world.block.items.BlockItemFactory;
import org.terasology.world.block.items.OnBlockToItem;
import org.terasology.world.block.regions.BlockRegionComponent;

/**
 * Event handler for events affecting block entities
 *
 * @author Immortius <immortius@gmail.com>
 */
@RegisterSystem
public class BlockEntitySystem implements ComponentSystem {

    @In
    private WorldProvider worldProvider;

    @In
    private EntityManager entityManager;

    @In
    private AudioManager audioManager;

    @In
    private InventoryManager inventoryManager;

    private BlockItemFactory blockItemFactory;
    private PickupBuilder pickupBuilder;
    private Random random;

    @Override
    public void initialise() {
        blockItemFactory = new BlockItemFactory(entityManager);
        pickupBuilder = new PickupBuilder();
        random = new FastRandom();
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent
    public void onBlockDestroy(DestroyBlockEvent event, EntityRef entity) {
        BlockDamageComponent blockDamageComponent = event.getDamageType().getComponent(BlockDamageComponent.class);

        float chanceOfBlockDrop = 1;

        if (blockDamageComponent != null) {
            chanceOfBlockDrop = 1 - blockDamageComponent.blockAnnihilationChance;
        }

        Vector3i location = getBlockLocation(entity);

        if (random.nextFloat() < chanceOfBlockDrop) {
            Block block = worldProvider.getBlock(location);

            BeforeBlockToItem beforeBlockToItemEvent = new BeforeBlockToItem(event.getDamageType(), event.getInstigator(), event.getTool(), block.getBlockFamily(), 1);
            entity.send(beforeBlockToItemEvent);
            if (!beforeBlockToItemEvent.isConsumed()) {
                for (BlockFamily family : beforeBlockToItemEvent.getBlockItemsToGenerate()) {
                    EntityRef item = blockItemFactory.newInstance(family, beforeBlockToItemEvent.getQuanityForBlock(family));
                    entity.send(new OnBlockToItem(item));

                    if (family.getArchetypeBlock().isDirectPickup()) {
                        if (!inventoryManager.giveItem(event.getInstigator(), item)) {
                            processDropping(item, location);
                        }
                    } else {
                        processDropping(item, location);
                    }
                }
                for (EntityRef item : beforeBlockToItemEvent.getItemsToDrop()) {
                    processDropping(item, location);
                }
            }
        }

        BlockRegionComponent blockRegionComp = entity.getComponent(BlockRegionComponent.class);
        if (blockRegionComp != null) {
            for (Vector3i blockPosition : blockRegionComp.region) {
                worldProvider.setBlock(blockPosition, BlockManager.getAir());
            }
            entity.destroy();
        } else {
            worldProvider.setBlock(location, BlockManager.getAir());
        }
    }

    @ReceiveEvent
    public void onBlockHasNoHealth(NoHealthEvent event, EntityRef entity) {
        if (isBlockOrBlockRegion(entity)) {
            BlockDamageComponent blockDamageComponent = event.getDamageType().getComponent(BlockDamageComponent.class);

            // TODO: Configurable via block definition
            if (blockDamageComponent == null || !blockDamageComponent.skipPerBlockEffects) {
                entity.send(new PlaySoundEvent(Assets.getSound("engine:RemoveBlock"), 0.6f));
            }

            entity.send(new DestroyBlockEvent(event.getInstigator(), event.getTool(), event.getDamageType()));
        }
    }

    private boolean isBlockOrBlockRegion(EntityRef entity) {
        return entity.hasComponent(BlockComponent.class) || entity.hasComponent(BlockRegionComponent.class);
    }

    private void processDropping(EntityRef item, Vector3i location) {
        /* PHYSICS */
        EntityRef pickup = pickupBuilder.createPickupFor(item, location.toVector3f(), 60);
        pickup.send(new ImpulseEvent(random.nextVector3f(30.0f)));
    }

    @ReceiveEvent
    public void beforeDamaged(BeforeDamagedEvent event, EntityRef blockEntity) {
        BlockFamily family = worldProvider.getBlock(getBlockLocation(blockEntity)).getBlockFamily();
        if (!family.getArchetypeBlock().isDestructible()) {
            event.consume();
        }
    }

    @ReceiveEvent(components = {BlockDamagedComponent.class})
    public void onRepaired(FullHealthEvent event, EntityRef entity) {
        entity.removeComponent(BlockDamagedComponent.class);
    }

    @ReceiveEvent
    public void onDamaged(OnDamagedEvent event, EntityRef entity) {
        if (isBlockOrBlockRegion(entity)) {
            BlockDamageComponent blockDamageSettings = event.getType().getComponent(BlockDamageComponent.class);
            boolean skipDamageEffects = false;
            if (blockDamageSettings != null) {
                skipDamageEffects = blockDamageSettings.skipPerBlockEffects;
            }
            if (!skipDamageEffects) {
                entity.send(new PlayBlockDamagedEvent(event.getInstigator()));
            }
            if (!entity.hasComponent(BlockDamagedComponent.class)) {
                entity.addComponent(new BlockDamagedComponent());
            }
        }
    }

    @ReceiveEvent
    public void onPlayBlockDamage(PlayBlockDamagedEvent event, EntityRef entity) {
        final Vector3i location = getBlockLocation(entity);
        BlockFamily family = worldProvider.getBlock(location).getBlockFamily();

        EntityBuilder builder = entityManager.newBuilder("engine:defaultBlockParticles");
        builder.getComponent(LocationComponent.class).setWorldPosition(location.toVector3f());
        builder.getComponent(BlockParticleEffectComponent.class).blockType = family;
        builder.build();

        if (family.getArchetypeBlock().isDebrisOnDestroy()) {
            EntityBuilder dustBuilder = entityManager.newBuilder("engine:dustEffect");
            dustBuilder.getComponent(LocationComponent.class).setWorldPosition(location.toVector3f());
            dustBuilder.build();
        }

        // TODO: Configurable via block definition
        audioManager.playSound(Assets.getSound("engine:Dig"), location.toVector3f());
    }

    @ReceiveEvent(netFilter = RegisterMode.AUTHORITY)
    public void beforeDamage(BeforeDamagedEvent event, EntityRef entity) {
        if (isBlockOrBlockRegion(entity)) {
            Block block = worldProvider.getBlock(getBlockLocation(entity));
            if (event.getDamageType() != null) {
                BlockDamageComponent blockDamage = event.getDamageType().getComponent(BlockDamageComponent.class);
                if (blockDamage != null) {
                    BlockFamily blockFamily = block.getBlockFamily();
                    for (String category : blockFamily.getCategories()) {
                        if (blockDamage.materialDamageMultiplier.containsKey(category)) {
                            event.multiply(blockDamage.materialDamageMultiplier.get(category));
                        }
                    }
                }
            }
        }
    }

    private Vector3i getBlockLocation(EntityRef entity) {
        BlockComponent blockComp = entity.getComponent(BlockComponent.class);
        BlockRegionComponent blockRegionComp = entity.getComponent(BlockRegionComponent.class);

        if (blockComp != null) {
            return blockComp.getPosition();
        } else if (blockRegionComp != null) {
            // Use first block as the representation of the block region
            return blockRegionComp.region.iterator().next();
        } else {
            throw new IllegalArgumentException("Provided entity without BlockComponent or BlockRegionComponent");
        }
    }
}
