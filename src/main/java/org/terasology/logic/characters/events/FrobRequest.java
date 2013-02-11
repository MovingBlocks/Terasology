package org.terasology.logic.characters.events;

import org.terasology.entitySystem.EntityRef;
import org.terasology.network.NetworkEvent;
import org.terasology.network.ServerEvent;

/**
 * @author Immortius
 */
@ServerEvent(lagCompensate = true)
public class FrobRequest extends NetworkEvent {
    private EntityRef item = EntityRef.NULL;

    public FrobRequest() {
    }
}
