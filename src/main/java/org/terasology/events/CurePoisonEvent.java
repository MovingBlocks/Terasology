package org.terasology.events;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

public class CurePoisonEvent extends AbstractEvent{
    private EntityRef instigator;

    public CurePoisonEvent() {
        this.instigator = EntityRef.NULL;
    }

    public CurePoisonEvent(EntityRef instigator) {
        this.instigator = instigator;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}

