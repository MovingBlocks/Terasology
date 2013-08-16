package org.terasology.logic.inventory.events;

import org.terasology.entitySystem.EntityBuilder;
import org.terasology.entitySystem.event.Event;

/**
 * @author Immortius
 */
public class ItemDroppedEvent implements Event {

    private EntityBuilder pickup;

    public ItemDroppedEvent(EntityBuilder pickupEntity) {
        this.pickup = pickupEntity;
    }

    public EntityBuilder getPickup() {
        return pickup;
    }
}
