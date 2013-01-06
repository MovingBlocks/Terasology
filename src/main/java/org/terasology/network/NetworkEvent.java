package org.terasology.network;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

/**
 * @author Immortius
 */
public class NetworkEvent extends AbstractEvent {
    private EntityRef client;

    void setClient(EntityRef client) {
        this.client = client;
    }

    /**
     *
     * @return The entity for the client this message came from (server-only).
     */
    public EntityRef getClient() {
        return client;
    }

    public boolean isValidFor(EntityRef expectedClient) {
        return client == null || expectedClient.equals(client);
    }
}
