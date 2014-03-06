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
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.BeforeDamagedEvent;
import org.terasology.logic.health.DoDestroyEvent;
import org.terasology.logic.health.FullHealthEvent;
import org.terasology.logic.health.OnDamagedEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.PickupBuilder;
import org.terasology.logic.inventory.action.GiveItemAction;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.particles.BlockParticleEffectComponent;
import org.terasology.math.Vector3i;
import org.terasology.physics.events.ImpulseEvent;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.entity.damage.BlockDamageModifierComponent;
import org.terasology.world.block.entity.damage.BlockDamagedComponent;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;
import org.terasology.world.block.items.OnBlockToItem;
import org.terasology.world.block.regions.ActAsBlockComponent;
import org.terasology.world.block.regions.BlockRegionComponent;

import javax.vecmath.Vector3f;

/**
 * Event handler for events affecting block entities
 *
 * @author Immortius <immortius@gmail.com>
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
    private InventoryManager inventoryManager;

    private BlockItemFactory blockItemFactory;
    private PickupBuilder pickupBuilder;
    private Random random;

    @Override
    public void initialise() {
        blockItemFactory = new BlockItemFactory(entityManager);
        pickupBuilder = new PickupBuilder(entityManager);
        random = new FastRandom();
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_LOW)
    public void doDestroy(DoDestroyEvent event, EntityRef entity, ActAsBlockComponent blockComponent) {
        commonDestroyed(event, entity);
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_LOW)
    public void doDestroy(DoDestroyEvent event, EntityRef entity, BlockComponent blockComponent) {
        commonDestroyed(event, entity);
        worldProvider.setBlock(blockComponent.getPosition(), BlockManager.getAir());
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_TRIVIAL)
    public void defaultDropsHandling(CreateBlockDropsEvent event, EntityRef entity, BlockComponent blockComponent) {
        Vector3i location = blockComponent.getPosition();
        commonDefaultDropsHandling(event, entity, location, blockComponent.getBlock());
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_TRIVIAL)
    public void defaultDropsHandling(CreateBlockDropsEvent event, EntityRef entity, ActAsBlockComponent blockComponent, LocationComponent locationComp) {
        Vector3i location = new Vector3i(locationComp.getWorldPosition(), 0.5f);
        commonDefaultDropsHandling(event, entity, location, blockComponent.block.getArchetypeBlock());
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_TRIVIAL)
    public void defaultDropsHandling(CreateBlockDropsEvent event, EntityRef entity, ActAsBlockComponent blockComponent, BlockRegionComponent blockRegion) {
        if (!entity.hasComponent(LocationComponent.class)) {
            Vector3i location = new Vector3i(blockRegion.region.center(), 0.5f);
            commonDefaultDropsHandling(event, entity, location, blockComponent.block.getArchetypeBlock());
        }
    }

    public void commonDefaultDropsHandling(CreateBlockDropsEvent event, EntityRef entity, Vector3i location, Block block) {
        BlockDamageModifierComponent blockDamageModifierComponent = event.getDamageType().getComponent(BlockDamageModifierComponent.class);
        float chanceOfBlockDrop = 1;

        if (blockDamageModifierComponent != null) {
            chanceOfBlockDrop = 1 - blockDamageModifierComponent.blockAnnihilationChance;
        }

        if (random.nextFloat() < chanceOfBlockDrop) {
            EntityRef item = blockItemFactory.newInstance(block.getBlockFamily(), 1);
            entity.send(new OnBlockToItem(item));

            if (shouldDropToWorld(event, block, blockDamageModifierComponent, item)) {
                processDropping(item, location);
            }
        }
    }

    private boolean shouldDropToWorld(CreateBlockDropsEvent event, Block block, BlockDamageModifierComponent blockDamageModifierComponent, EntityRef item) {
        return !isDirectPickup(block, blockDamageModifierComponent) || !giveItem(event, item);
    }

    private boolean giveItem(CreateBlockDropsEvent event, EntityRef item) {
        GiveItemAction action = new GiveItemAction(item, item);
        event.getInstigator().send(action);
        return action.isConsumed();
    }

    private boolean isDirectPickup(Block block, BlockDamageModifierComponent blockDamageModifierComponent) {
        return block.isDirectPickup() || (blockDamageModifierComponent != null && blockDamageModifierComponent.directPickup);
    }

    private void commonDestroyed(DoDestroyEvent event, EntityRef entity) {
        entity.send(new CreateBlockDropsEvent(event.getInstigator(), event.getDirectCause(), event.getDamageType()));

        BlockDamageModifierComponent blockDamageModifierComponent = event.getDamageType().getComponent(BlockDamageModifierComponent.class);
        // TODO: Configurable via block definition
        if (blockDamageModifierComponent == null || !blockDamageModifierComponent.skipPerBlockEffects) {
            entity.send(new PlaySoundEvent(Assets.getSound("engine:RemoveBlock"), 0.6f));
        }
    }

    private void processDropping(EntityRef item, Vector3i location) {
        EntityRef pickup = pickupBuilder.createPickupFor(item, location.toVector3f(), 60);
        pickup.send(new ImpulseEvent(random.nextVector3f(30.0f)));
    }

    @ReceiveEvent
    public void beforeDamaged(BeforeDamagedEvent event, EntityRef blockEntity, BlockComponent blockComp) {
        if (!blockComp.getBlock().isDestructible()) {
            event.consume();
        }
    }

    @ReceiveEvent
    public void beforeDamaged(BeforeDamagedEvent event, EntityRef blockEntity, ActAsBlockComponent blockComp) {
        if (!blockComp.block.getArchetypeBlock().isDestructible()) {
            event.consume();
        }
    }

    @ReceiveEvent(components = {BlockDamagedComponent.class})
    public void onRepaired(FullHealthEvent event, EntityRef entity) {
        entity.removeComponent(BlockDamagedComponent.class);
    }

    @ReceiveEvent
    public void onDamaged(OnDamagedEvent event, EntityRef entity, BlockComponent blockComponent, LocationComponent locComp) {
        onDamagedCommon(event, entity, blockComponent.getBlock().getBlockFamily(), locComp.getWorldPosition());
        if (!entity.hasComponent(BlockDamagedComponent.class)) {
            entity.addComponent(new BlockDamagedComponent());
        }
    }

    @ReceiveEvent
    public void onDamaged(OnDamagedEvent event, EntityRef entity, ActAsBlockComponent blockComponent, LocationComponent locComp) {
        onDamagedCommon(event, entity, blockComponent.block, locComp.getWorldPosition());

    }

    public void onDamagedCommon(OnDamagedEvent event, EntityRef entity, BlockFamily blockFamily, Vector3f location) {
        BlockDamageModifierComponent blockDamageSettings = event.getType().getComponent(BlockDamageModifierComponent.class);
        boolean skipDamageEffects = false;
        if (blockDamageSettings != null) {
            skipDamageEffects = blockDamageSettings.skipPerBlockEffects;
        }
        if (!skipDamageEffects) {
            onPlayBlockDamageCommon(blockFamily, location);
        }
    }

    private void onPlayBlockDamageCommon(BlockFamily family, Vector3f location) {
        EntityBuilder builder = entityManager.newBuilder("engine:defaultBlockParticles");
        builder.getComponent(LocationComponent.class).setWorldPosition(location);
        builder.getComponent(BlockParticleEffectComponent.class).blockType = family;
        builder.build();

        if (family.getArchetypeBlock().isDebrisOnDestroy()) {
            EntityBuilder dustBuilder = entityManager.newBuilder("engine:dustEffect");
            dustBuilder.getComponent(LocationComponent.class).setWorldPosition(location);
            dustBuilder.build();
        }

        // TODO: Configurable via block definition
        audioManager.playSound(Assets.getSound("engine:Dig"), location);
    }

    @ReceiveEvent(netFilter = RegisterMode.AUTHORITY)
    public void beforeDamage(BeforeDamagedEvent event, EntityRef entity, BlockComponent blockComp) {
        beforeDamageCommon(event, blockComp.getBlock());
    }

    @ReceiveEvent(netFilter = RegisterMode.AUTHORITY)
    public void beforeDamage(BeforeDamagedEvent event, EntityRef entity, ActAsBlockComponent blockComp) {
        beforeDamageCommon(event, blockComp.block.getArchetypeBlock());
    }

    private void beforeDamageCommon(BeforeDamagedEvent event, Block block) {
        if (event.getDamageType() != null) {
            BlockDamageModifierComponent blockDamage = event.getDamageType().getComponent(BlockDamageModifierComponent.class);
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
