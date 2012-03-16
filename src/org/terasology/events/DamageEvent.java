package org.terasology.events;

import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class DamageEvent implements Event {
    private int amount;
    private EntityRef instigator;

    public DamageEvent(int amount) {
        this.amount = amount;
        this.instigator = null;
    }

    public DamageEvent(int amount, EntityRef instigator) {
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
