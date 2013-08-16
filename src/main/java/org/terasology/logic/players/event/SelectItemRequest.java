package org.terasology.logic.players.event;

import org.terasology.entitySystem.event.Event;
import org.terasology.network.ServerEvent;

/**
 * @author Immortius
 */
@ServerEvent
public class SelectItemRequest implements Event {

    private int slot = 0;

    protected SelectItemRequest() {
    }

    public SelectItemRequest(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }
}
