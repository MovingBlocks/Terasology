package org.terasology.logic.health;

import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.Event;

/**
 * @author Immortius
 */
public class HealEvent implements Event {
    private int amount;
    private EntityRef instigator;

    public HealEvent(int amount) {
        this.amount = amount;
        instigator = EntityRef.NULL;
    }

    public HealEvent(int amount, EntityRef instigator) {
        this.amount = amount;
        this.instigator = instigator;
    }

    public int getAmount() {
        return amount;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
