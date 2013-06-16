package org.terasology.world.block.items;

import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.Event;

/**
 * This event is sent when a block in the world is turned into an item, to allow modification of the item entity.
 * @author Immortius
 */
public class OnBlockToItem implements Event {

    private EntityRef item;

    public OnBlockToItem(EntityRef item) {
        this.item = item;
    }

    public EntityRef getItem() {
        return item;
    }
}
