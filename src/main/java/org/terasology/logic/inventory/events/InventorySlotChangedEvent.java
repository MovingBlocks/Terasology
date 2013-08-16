package org.terasology.logic.inventory.events;

import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.Event;

/**
 * @author Immortius
 */
public class InventorySlotChangedEvent implements Event {
    private int slot;
    private EntityRef oldItem;
    private EntityRef newItem;

    public InventorySlotChangedEvent(int slot, EntityRef oldItem, EntityRef newItem) {
        this.slot = slot;
        this.oldItem = oldItem;
        this.newItem = newItem;
    }

    public int getSlot() {
        return slot;
    }

    public EntityRef getOldItem() {
        return oldItem;
    }

    public EntityRef getNewItem() {
        return newItem;
    }
}
