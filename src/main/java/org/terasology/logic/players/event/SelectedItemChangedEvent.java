package org.terasology.logic.players.event;

import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.Event;

/**
 * @author Immortius
 */
public class SelectedItemChangedEvent implements Event {
    private EntityRef oldItem;
    private EntityRef newItem;

    public SelectedItemChangedEvent(EntityRef oldItem, EntityRef newItem) {
        this.oldItem = oldItem;
        this.newItem = newItem;
    }

    public EntityRef getOldItem() {
        return oldItem;
    }

    public EntityRef getNewItem() {
        return newItem;
    }
}
