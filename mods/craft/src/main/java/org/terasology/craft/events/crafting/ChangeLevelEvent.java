package org.terasology.craft.events.crafting;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

public class ChangeLevelEvent extends AbstractEvent {
    private float nextLevel;
    private EntityRef instigator;

    public ChangeLevelEvent(float nextLevel, EntityRef instigator) {
        this.nextLevel = nextLevel;
        this.instigator = instigator;
    }

    public boolean isDecreaseEvent() {
        return nextLevel < 0;
    }

}
