package org.terasology.events;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class NoHealthEvent extends AbstractEvent {
    private EntityRef instigator;

    public NoHealthEvent(EntityRef instigator) {
        this.instigator = instigator;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
