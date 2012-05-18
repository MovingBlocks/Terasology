package org.terasology.events;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

/**
 * Intermediary Event for Activating the effect.
 * DrinkPotion ---> THIS -----> STATUS AFFECTOR
 */
public class BoostSpeedEvent extends AbstractEvent {
    private EntityRef instigator;

    public BoostSpeedEvent() {
        this.instigator = EntityRef.NULL;
    }

    public BoostSpeedEvent(EntityRef instigator) {
        this.instigator = instigator;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}

