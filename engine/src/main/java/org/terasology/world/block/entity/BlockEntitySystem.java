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
import org.terasology.physics.events.ImpulseEvent;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BeforeBlockToItem;
import org.terasology.world.block.items.BlockItemFactory;
import org.terasology.world.block.items.OnBlockToItem;

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

    @ReceiveEvent(components = {BlockComponent.class})
    public void onBlockDestroy(DestroyBlockEvent event, EntityRef entity) {
        BlockComponent blockComp = entity.getComponent(BlockComponent.class);
        BlockDamageComponent blockDamageComponent = event.getDamageType().getComponent(BlockDamageComponent.class);

        float chanceOfBlockDrop = 1;

        if (blockDamageComponent != null) {
            chanceOfBlockDrop = 1 - blockDamageComponent.blockAnnihilationChance;
        }

        if (random.nextFloat() < chanceOfBlockDrop) {
            BeforeBlockToItem beforeBlockToItemEvent = new BeforeBlockToItem(event.getDamageType(), event.getInstigator(), event.getTool(), blockComp.getBlock().getBlockFamily(), 1);
            entity.send(beforeBlockToItemEvent);
            if (!beforeBlockToItemEvent.isConsumed()) {
                for (BlockFamily family : beforeBlockToItemEvent.getBlockItemsToGenerate()) {
                    EntityRef item = blockItemFactory.newInstance(family, beforeBlockToItemEvent.getQuanityForBlock(family));
                    entity.send(new OnBlockToItem(item));

                    if (family.getArchetypeBlock().isDirectPickup()) {
                        if (!inventoryManager.giveItem(event.getInstigator(), item))
                            processDropping(blockComp, item);
                    } else {
                        processDropping(blockComp, item);
                    }
                }
                for (EntityRef item : beforeBlockToItemEvent.getItemsToDrop()) {
                    processDropping(blockComp, item);
                }
            }
        }

        worldProvider.setBlock(blockComp.getPosition(), BlockManager.getAir());
    }

    @ReceiveEvent(components = {BlockComponent.class})
    public void onBlockHasNoHealth(NoHealthEvent event, EntityRef entity) {
        BlockDamageComponent blockDamageComponent = event.getDamageType().getComponent(BlockDamageComponent.class);

        // TODO: Configurable via block definition
        if (blockDamageComponent == null || !blockDamageComponent.skipPerBlockEffects) {
            entity.send(new PlaySoundEvent(Assets.getSound("engine:RemoveBlock"), 0.6f));
        }

        entity.send(new DestroyBlockEvent(event.getInstigator(), event.getTool(), event.getDamageType()));
    }

    private void processDropping(BlockComponent blockComp, EntityRef item) {
        /* PHYSICS */
        EntityRef pickup = pickupBuilder.createPickupFor(item, blockComp.getPosition().toVector3f(), 60);
        pickup.send(new ImpulseEvent(random.nextVector3f(30.0f)));
    }

    @ReceiveEvent
    public void beforeDamaged(BeforeDamagedEvent event, EntityRef blockEntity, BlockComponent blockComp) {
        BlockFamily family = worldProvider.getBlock(blockComp.getPosition()).getBlockFamily();
        if (!family.getArchetypeBlock().isDestructible()) {
            event.consume();
        }
    }

    @ReceiveEvent(components = {BlockComponent.class, BlockDamagedComponent.class})
    public void onRepaired(FullHealthEvent event, EntityRef entity) {
        entity.removeComponent(BlockDamagedComponent.class);
    }

    @ReceiveEvent(components = {BlockComponent.class})
    public void onDamaged(OnDamagedEvent event, EntityRef entity) {
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

    @ReceiveEvent
    public void onPlayBlockDamage(PlayBlockDamagedEvent event, EntityRef entity, BlockComponent blockComp) {
        BlockFamily family = worldProvider.getBlock(blockComp.getPosition()).getBlockFamily();

        EntityBuilder builder = entityManager.newBuilder("engine:defaultBlockParticles");
        builder.getComponent(LocationComponent.class).setWorldPosition(blockComp.getPosition().toVector3f());
        builder.getComponent(BlockParticleEffectComponent.class).blockType = family;
        builder.build();

        if (family.getArchetypeBlock().isDebrisOnDestroy()) {
            EntityBuilder dustBuilder = entityManager.newBuilder("engine:dustEffect");
            dustBuilder.getComponent(LocationComponent.class).setWorldPosition(blockComp.getPosition().toVector3f());
            dustBuilder.build();
        }

        // TODO: Configurable via block definition
        audioManager.playSound(Assets.getSound("engine:Dig"), blockComp.getPosition().toVector3f());
    }

    @ReceiveEvent(netFilter = RegisterMode.AUTHORITY)
    public void beforeDamage(BeforeDamagedEvent event, EntityRef entity, BlockComponent blockComp) {
        if (event.getDamageType() != null) {
            BlockDamageComponent blockDamage = event.getDamageType().getComponent(BlockDamageComponent.class);
            if (blockDamage != null) {
                BlockFamily block = worldProvider.getBlock(blockComp.getPosition()).getBlockFamily();
                for (String category : block.getCategories()) {
                    if (blockDamage.materialDamageMultiplier.containsKey(category)) {
                        event.multiply(blockDamage.materialDamageMultiplier.get(category));
                    }
                }
            }
        }
    }

}
