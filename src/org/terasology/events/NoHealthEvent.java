package org.terasology.events;

import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class NoHealthEvent implements Event {
    private EntityRef instigator;
    
    public NoHealthEvent(EntityRef instigator) {
        this.instigator = instigator;
    }
    
    public EntityRef getInstigator() {
        return instigator;
    }
}
