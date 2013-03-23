package org.terasology.logic.characters.events;

import org.terasology.entitySystem.EntityRef;
import org.terasology.network.NetworkEvent;
import org.terasology.network.ServerEvent;

/**
 * @author Immortius
 */
@ServerEvent(lagCompensate = true)
public class AttackRequest extends NetworkEvent {

    private EntityRef item = EntityRef.NULL;

    protected AttackRequest() {
    }

    public AttackRequest(EntityRef withItem) {
        this.item = withItem;
    }

    public EntityRef getItem() {
        return item;
    }
}
