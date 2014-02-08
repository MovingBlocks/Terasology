/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.core.logic.blockDropGrammar;

import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.DoDestroyEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.PickupBuilder;
import org.terasology.logic.inventory.action.GiveItemAction;
import org.terasology.logic.location.LocationComponent;
import org.terasology.physics.events.ImpulseEvent;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.entity.CreateBlockDropsEvent;
import org.terasology.world.block.entity.damage.BlockDamageModifierComponent;
import org.terasology.world.block.items.BlockItemFactory;

import javax.vecmath.Vector3f;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockDropGrammarSystem implements ComponentSystem {
    @In
    private EntityManager entityManager;
    @In
    private BlockManager blockManager;
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

    @ReceiveEvent(components = {BlockDropGrammarComponent.class})
    public void whenBlockDropped(CreateBlockDropsEvent event, EntityRef blockEntity) {
        event.consume();
    }

    @ReceiveEvent
    public void onDestroyed(DoDestroyEvent event, EntityRef entity, BlockDropGrammarComponent blockDrop, LocationComponent locationComp) {
        BlockDamageModifierComponent blockDamageModifierComponent = event.getDamageType().getComponent(BlockDamageModifierComponent.class);
        float chanceOfBlockDrop = 1;

        if (blockDamageModifierComponent != null) {
            chanceOfBlockDrop = 1 - blockDamageModifierComponent.blockAnnihilationChance;
        }

        if (random.nextFloat() < chanceOfBlockDrop) {
            if (blockDrop.blockDrops != null) {
                for (String drop : blockDrop.blockDrops) {
                    String dropResult = drop;
                    boolean dropping = true;
                    int pipeIndex = dropResult.indexOf('|');
                    if (pipeIndex > -1) {
                        float chance = Float.parseFloat(dropResult.substring(0, pipeIndex));
                        if (random.nextFloat() >= chance) {
                            dropping = false;
                        }
                        dropResult = dropResult.substring(pipeIndex + 1);
                    }
                    if (dropping) {
                        DropParser dropParser = new DropParser(random, dropResult).invoke();
                        EntityRef dropItem = blockItemFactory.newInstance(blockManager.getBlockFamily(dropParser.getDrop()), dropParser.getCount());
                        if (shouldDropToWorld(event, blockDamageModifierComponent, dropItem)) {
                            createDrop(dropItem, locationComp.getWorldPosition(), true);
                        }
                    }
                }
            }

            if (blockDrop.itemDrops != null) {
                for (String drop : blockDrop.itemDrops) {
                    String dropResult = drop;
                    boolean dropping = true;
                    int pipeIndex = dropResult.indexOf('|');
                    if (pipeIndex > -1) {
                        float chance = Float.parseFloat(dropResult.substring(0, pipeIndex));
                        if (random.nextFloat() >= chance) {
                            dropping = false;
                        }
                        dropResult = dropResult.substring(pipeIndex + 1);
                    }
                    if (dropping) {
                        DropParser dropParser = new DropParser(random, dropResult).invoke();
                        EntityBuilder dropEntity = entityManager.newBuilder(dropParser.getDrop());
                        if (dropParser.getCount() > 1) {
                            ItemComponent itemComponent = dropEntity.getComponent(ItemComponent.class);
                            itemComponent.stackCount = (byte) dropParser.getCount();
                        }
                        EntityRef dropItem = dropEntity.build();
                        if (shouldDropToWorld(event, blockDamageModifierComponent, dropItem)) {
                            createDrop(dropItem, locationComp.getWorldPosition(), false);
                        }
                    }
                }
            }
        }
    }

    private boolean shouldDropToWorld(DoDestroyEvent event, BlockDamageModifierComponent blockDamageModifierComponent, EntityRef dropItem) {
        return blockDamageModifierComponent == null || !blockDamageModifierComponent.directPickup
                || !giveItem(event.getInstigator(), dropItem);
    }

    private boolean giveItem(EntityRef instigator, EntityRef dropItem) {
        GiveItemAction giveEvent = new GiveItemAction(instigator, dropItem);
        instigator.send(giveEvent);
        return giveEvent.isConsumed();
    }

    private void createDrop(EntityRef item, Vector3f location, boolean applyMovement) {
        EntityRef pickup = pickupBuilder.createPickupFor(item, location, 60, true);
        if (applyMovement) {
            pickup.send(new ImpulseEvent(random.nextVector3f(30.0f)));
        }
    }

    private class DropParser {
        private Random rnd;
        private String drop;
        private int count;
        private String resultDrop;

        public DropParser(Random rnd, String drop) {
            this.rnd = rnd;
            this.drop = drop;
        }

        public String getDrop() {
            return resultDrop;
        }

        public int getCount() {
            return count;
        }

        public DropParser invoke() {
            resultDrop = drop;
            int timesIndex = resultDrop.indexOf('*');
            int countMin = 1;
            int countMax = 1;
            if (timesIndex > -1) {
                String timesStr = resultDrop.substring(0, timesIndex);
                int minusIndex = timesStr.indexOf('-');
                if (minusIndex > -1) {
                    countMin = Integer.parseInt(timesStr.substring(0, minusIndex));
                    countMax = Integer.parseInt(timesStr.substring(minusIndex + 1));
                } else {
                    countMin = Integer.parseInt(timesStr);
                    countMax = countMin;
                }
                resultDrop = resultDrop.substring(timesIndex + 1);
            }
            count = rnd.nextInt(countMin, countMax);
            return this;
        }
    }
}
