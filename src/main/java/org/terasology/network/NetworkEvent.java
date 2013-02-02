package org.terasology.network;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

/**
 * @author Immortius
 */
public class NetworkEvent extends AbstractEvent {

    private EntityRef instigator = EntityRef.NULL;

    protected NetworkEvent() {
    }

    protected NetworkEvent(EntityRef instigator) {
        this.instigator = instigator;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
