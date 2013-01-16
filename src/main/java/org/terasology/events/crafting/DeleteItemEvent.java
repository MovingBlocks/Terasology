package org.terasology.events.crafting;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

/**
 *  @author Small-Jeeper
 */
public class DeleteItemEvent extends AbstractEvent {
    private float percent;

    public DeleteItemEvent() {
        this(0);
    }

    public DeleteItemEvent(float percent) {
        this.percent = percent;
    }

    public float getPercent(){
        return percent;
    }
}