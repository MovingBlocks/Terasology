package org.terasology.logic.inventory;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class PickedUpItem implements Event {
    private EntityRef item;

    public PickedUpItem(EntityRef item) {
        this.item = item;
    }

    public EntityRef getItem() {
        return item;
    }
}
