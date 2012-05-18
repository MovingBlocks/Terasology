package org.terasology.events;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

public class PoisonedEvent extends AbstractEvent{
    private EntityRef instigator;

    public PoisonedEvent() {
        this.instigator = EntityRef.NULL;
    }

    public PoisonedEvent(EntityRef instigator) {
        this.instigator = instigator;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}

