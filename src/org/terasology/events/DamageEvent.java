package org.terasology.events;

import org.terasology.entitySystem.Event;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class DamageEvent implements Event {
    private int amount;
    
    public DamageEvent(int amount) {
        this.amount = amount;
    }
    
    public int getAmount() {
        return amount;
    }
}
