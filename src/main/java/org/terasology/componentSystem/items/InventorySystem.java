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
package org.terasology.componentSystem.items;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;
import org.terasology.components.InventoryComponent;
import org.terasology.components.ItemComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.events.inventory.ReceiveItemEvent;

/**
 * System providing inventory related functionality
 *
 * @author Immortius <immortius@gmail.com>
 */
@RegisterComponentSystem
public class InventorySystem implements EventHandlerSystem {

    // TODO: differ per item?
    public static final byte MAX_STACK = (byte) 99;

    private static final Logger logger = LoggerFactory.getLogger(InventorySystem.class);

    @Override
    public void initialise() {

    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = InventoryComponent.class)
    public void onDestroyed(RemovedComponentEvent event, EntityRef entity) {
        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        for (EntityRef content : inventory.itemSlots) {
            content.destroy();
        }
    }

    /**
     * Adds an item to an inventory. If the item stacks it may be destroyed or partially moved (stack count diminished).
     *
     * @param event  - the event that triggered, should have a reference to the item
     * @param entity - the entity that owns the InventoryComponent that is receiving the item
     *               //TODO: Later the player should be allowed to explicitly place a partial stack in a target inventory without merging
     */
    @ReceiveEvent(components = InventoryComponent.class)
    public void onReceiveItem(ReceiveItemEvent event, EntityRef entity) {
        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        ItemComponent item = event.getItem().getComponent(ItemComponent.class);
        if (inventory == null || item == null)
            return;

        logger.info("Receiving item {} for toolbar", item.name);

        boolean itemChanged = false;
        
        //place slot in the inventory automatically
        if (event.getSlot() == -1) {
	        // First check for existing alike stacks in the target inventory
	        if (!item.stackId.isEmpty()) {
	            for (EntityRef itemStack : inventory.itemSlots) {
	                ItemComponent stackComp = itemStack.getComponent(ItemComponent.class);
	                if (stackComp != null) {
	                    // Found an existing stack, does it match what we're trying to add? If so then merge in what we can
	                    if (item.stackId.equals(stackComp.stackId)) {
	                        int stackSpace = MAX_STACK - stackComp.stackCount;
	                        int amountToTransfer = Math.min(stackSpace, item.stackCount);
	                        stackComp.stackCount += amountToTransfer;
	                        item.stackCount -= amountToTransfer;
	                        itemStack.saveComponent(stackComp); //TODO: Duping chance between here and entity.saveComponent(inventory); ?
	                        itemChanged = true;
	
	                        // If we consumed the whole remainder of the incoming item stack then destroy its entity entirely (we "merged" the entities)
	                        if (item.stackCount == 0) {
	                            event.getItem().destroy();
	                            return;
	                        }
	                    }
	                }
	            }
	        }
	
	        // Then check for free spaces to place the remainder of the incoming item stack
	        int freeSlot = inventory.itemSlots.indexOf(EntityRef.NULL);
	        if (freeSlot != -1) {
	            inventory.itemSlots.set(freeSlot, event.getItem());
	            // If the item came from another inventory we need to explicitly blank it out there (we "moved" the entity)
	            InventoryComponent sourceInventory = item.container.getComponent(InventoryComponent.class);
	            if (sourceInventory != null) {
	                int matchedSlot = sourceInventory.itemSlots.indexOf(event.getItem());
	                if (matchedSlot != -1) {
	                    sourceInventory.itemSlots.set(matchedSlot, EntityRef.NULL);
	                    item.container.saveComponent(sourceInventory);
	                }
	            }
	            // And then add it properly in the new inventory
	            item.container = entity;
	            event.getItem().saveComponent(item);
	            entity.saveComponent(inventory);
	            return;
	        } // If there are no free slots we do nothing, but may still save if part of the stack was merged in
        }
        else {
        	
        }
        
        if (itemChanged) {
            event.getItem().saveComponent(item);
        }
    }
}
