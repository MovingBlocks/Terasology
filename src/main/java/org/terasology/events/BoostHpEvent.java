package org.terasology.events;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;


public class BoostHpEvent extends AbstractEvent {
    private EntityRef instigator;

    public BoostHpEvent() {
        this.instigator = EntityRef.NULL;
    }

    public BoostHpEvent(EntityRef instigator) {
        this.instigator = instigator;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
