package org.terasology.rendering.gui.components;

import org.terasology.components.InventoryComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.CoreRegistry;

/**
 * This is the temporary storage place for items which are currently dragged by the user.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * 
 * TODO move this somewhere else
 * TODO somehow integrate this directly into the player entity? (can a player have more than one InventoryComponent?)
 */
public class InventoryMovementEntity {
    private static EntityRef entity;
    private static InventoryComponent inventory;
    
    EntityRef itemOwner = EntityRef.NULL;
    
    public InventoryMovementEntity() {
    	entity = CoreRegistry.get(EntityManager.class).create();
    	inventory = new InventoryComponent(1);
    	
    	entity.addComponent(inventory);
    }
    
    /**
     * Send to stored item to an entity.
     * @param entity The entitiy which the stored item will be send to.
     */
    public void send(EntityRef entity) {
    	if (inventory.itemSlots.get(0) != EntityRef.NULL) {
    		entity.send(new ReceiveItemEvent(inventory.itemSlots.get(0)));
    	}
    }
    
    /**
     * Store an item for movement.
     * @param owner The owner of this item.
     * @param item The item to move.
     */
    public void store(EntityRef owner, EntityRef item) {
    	itemOwner = owner;
    	entity.send(new ReceiveItemEvent(item));
    }
    
    /**
     * Sends the item which is currently stored back to its owner.
     */
    public void reset() {
    	if (hasitem())
    		itemOwner.send(new ReceiveItemEvent(inventory.itemSlots.get(0)));
    }
    
    /**
     * Check if an item is currently stored.
     * @return Returns true if an item is stored.
     */
    public boolean hasitem() {
    	if (inventory.itemSlots.get(0) != EntityRef.NULL)
    		return true;
    	
    	return false;
    }
    
    /**
     * Get the owner of this currently stored item.
     * @return Returns the owner.
     */
    public EntityRef getOwner() {
    	return itemOwner;
    }

    /**
     * Get the item which is currently stored.
     * @return Returns the item.
     */
	public EntityRef getItem() {
		return inventory.itemSlots.get(0);
	}
}
