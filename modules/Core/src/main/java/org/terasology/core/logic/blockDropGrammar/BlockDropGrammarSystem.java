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
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.DoDestroyEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.PickupBuilder;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.events.ImpulseEvent;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.entity.CreateBlockDropsEvent;
import org.terasology.world.block.entity.damage.BlockDamageModifierComponent;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.List;

/**
 * This class is used to generate the drop of items using a grammar system
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockDropGrammarSystem extends BaseComponentSystem {
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
        pickupBuilder = new PickupBuilder(entityManager);
        random = new FastRandom();
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
            processDropBlock(event, blockDrop, locationComp, blockDamageModifierComponent);
        }
    }

    /**
     * Process the drop of a block when is destroyed
     * @param event Event which trigger the drop
     * @param blockDrop Block which was destroyed
     * @param locationComp Location and facing of the object
     * @param blockDamageModifierComponent Block modifier
     */
    private void processDropBlock(DoDestroyEvent event, BlockDropGrammarComponent blockDrop, LocationComponent locationComp, BlockDamageModifierComponent blockDamageModifierComponent) {
        List<String> blockDrops = blockDrop.blockDrops;
        List<String> itemDrops = blockDrop.itemDrops;

        BlockDropGrammarComponent.DropDefinition dropDefinition = getDropDefinition(itemDrops, blockDrop, blockDamageModifierComponent);
        if (dropDefinition != null) {
            blockDrops = dropDefinition.blockDrops;
            itemDrops = dropDefinition.itemDrops;
        }

        if (blockDrops != null) {
            processBlockDrops(blockDrops, event, locationComp, blockDamageModifierComponent);
        }

        if (itemDrops != null) {
            processItemDrops(itemDrops, event, locationComp, blockDamageModifierComponent);
        }
    }

    /**
     * Get the definition of the drop
     * @param itemDrops Base item drops
     * @param blockDrop Base block drops
     * @param blockDamageModifierComponent Block Modifier
     * @return The block definition if it is founded, null otherwise
     */
    private BlockDropGrammarComponent.DropDefinition getDropDefinition(List<String> itemDrops, BlockDropGrammarComponent blockDrop, BlockDamageModifierComponent blockDamageModifierComponent) {
        if (blockDamageModifierComponent != null && blockDrop.droppedWithTool != null) {
            for (String toolType : blockDamageModifierComponent.materialDamageMultiplier.keySet()) {
                if (blockDrop.droppedWithTool.containsKey(toolType)) {
                    return blockDrop.droppedWithTool.get(toolType);
                }
            }
        }
        return null;
    }

    /**
     * Process the item drop for the given list
     * @param itemDrops Items to be dropped
     * @param event Event which trigger the drop
     * @param locationComp Location and facing of the object
     * @param blockDamageModifierComponent Block Modifier
     */
    private void processItemDrops(List<String> itemDrops, DoDestroyEvent event, LocationComponent locationComp, BlockDamageModifierComponent blockDamageModifierComponent) {
        for (String drop : itemDrops) {
            if (isDropped(drop)) {
                dropFromItem(getDropResult(drop), event, locationComp, blockDamageModifierComponent);
            }
        }
    }

    /**
     * Generate the drop of the selected item from a item
     * @param dropResult Item to be dropped
     * @param event Event which trigger the drop
     * @param locationComp Location and facing of the object
     * @param blockDamageModifierComponent Block Modifier
     */
    private void dropFromItem(String dropResult, DoDestroyEvent event, LocationComponent locationComp, BlockDamageModifierComponent blockDamageModifierComponent) {
        DropParser dropParser = new DropParser(random, dropResult).invoke();
        EntityBuilder dropEntity = entityManager.newBuilder(dropParser.getDrop());
        if (dropParser.getCount() > 1) {
            ItemComponent itemComponent = dropEntity.getComponent(ItemComponent.class);
            itemComponent.stackCount = (byte) dropParser.getCount();
        }
        EntityRef dropItem = dropEntity.build();
        if (shouldDropToWorld(dropItem, event, blockDamageModifierComponent)) {
            createDrop(dropItem, locationComp.getWorldPosition(), false);
        }
    }

    /**
     * Process the block drop for the given list
     * @param blockDrops Items to be dropped
     * @param event Event which trigger the drop
     * @param locationComp Location and facing of the object
     * @param blockDamageModifierComponent Block Modifier
     */
    private void processBlockDrops(List<String> blockDrops, DoDestroyEvent event, LocationComponent locationComp, BlockDamageModifierComponent blockDamageModifierComponent) {
        for (String drop : blockDrops) {
            if (isDropped(drop)) {
                dropFromBlock(getDropResult(drop), event, locationComp, blockDamageModifierComponent);
            }
        }
    }
    
    /**
     * Check if the given drop is going to be dropped using the chance
     * @param drop String with the information of the drop
     * @return True if the item is dropped, False otherwise
     */
    private boolean isDropped(String drop) {
        int pipeIndex = drop.indexOf('|');
        if (pipeIndex > -1) {
            float chance = Float.parseFloat(drop.substring(0, pipeIndex));
            if (random.nextFloat() >= chance) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Get the item to be dropped from the String with the drop information
     * @param drop String with the information of the drop
     * @return Item to be dropped
     */
    private String getDropResult(String drop) {
        int pipeIndex = drop.indexOf('|');
        if (pipeIndex > -1) {
            return drop.substring(pipeIndex + 1);
        }
        return drop;
    }

    /**
     * Generate the drop for the selected item from a block
     * @param dropResult Item to be dropped
     * @param event Event which trigger the drop
     * @param locationComp Location and facing of the object
     * @param blockDamageModifierComponent Block Modifier
     */
    private void dropFromBlock(String dropResult, DoDestroyEvent event, LocationComponent locationComp, BlockDamageModifierComponent blockDamageModifierComponent) {
        DropParser dropParser = new DropParser(random, dropResult).invoke();
        EntityRef dropItem = blockItemFactory.newInstance(blockManager.getBlockFamily(dropParser.getDrop()), dropParser.getCount());
        if (shouldDropToWorld(dropItem, event, blockDamageModifierComponent)) {
            createDrop(dropItem, locationComp.getWorldPosition(), true);
        }
    }

    /**
     * Verify if the item should be dropped in the world
     * @param dropItem Item to be dropped
     * @param event Event which trigger the drop
     * @param blockDamageModifierComponent Block Modifier
     * @return True if the items should be dropped
     */
    private boolean shouldDropToWorld(EntityRef dropItem, DoDestroyEvent event, BlockDamageModifierComponent blockDamageModifierComponent) {
        return blockDamageModifierComponent == null || !blockDamageModifierComponent.directPickup
                || !giveItem(event.getInstigator(), dropItem);
    }

    /**
     * Give the item to the selected entity
     * @param instigator Entity to give the item
     * @param dropItem Entity of the item
     * @return True if the operation was successful
     */
    private boolean giveItem(EntityRef instigator, EntityRef dropItem) {
        return inventoryManager.giveItem(instigator, instigator, dropItem);
    }

    /**
     * Generate the drop of the entity
     * @param item Reference to the dropped item
     * @param location Location where the item must be created
     * @param applyMovement Apply an impulse on the creation
     */
    private void createDrop(EntityRef item, Vector3f location, boolean applyMovement) {
        EntityRef pickup = pickupBuilder.createPickupFor(item, location, 60, true);
        if (applyMovement) {
            pickup.send(new ImpulseEvent(random.nextVector3f(30.0f)));
        }
    }

    /**
     * This class is used to parse the drop information
     */
    private class DropParser {
        private Random rnd;
        private String drop;
        private int count;
        private String resultDrop;

        public DropParser(Random rnd, String drop) {
            this.rnd = rnd;
            this.drop = drop;
        }

        /**
         * @return The drop information
         */
        public String getDrop() {
            return resultDrop;
        }

        /**
         * @return Number of items to be dropped
         */
        public int getCount() {
            return count;
        }

        /**
         * @return The DropParser with the calculated result
         */
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
