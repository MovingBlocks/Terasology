package org.terasology.world.block.entity;

import org.terasology.entitySystem.EntityRef;
import org.terasology.network.BroadcastEvent;
import org.terasology.network.NetworkEvent;

/**
 * @author Immortius
 */
@BroadcastEvent(skipInstigator = true)
public class PlayBlockDamagedEvent extends NetworkEvent {
    protected PlayBlockDamagedEvent() {
    }

    public PlayBlockDamagedEvent(EntityRef instigator) {
        super(instigator);
    }
}
