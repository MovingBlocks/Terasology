package org.terasology.network;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;

/**
 * @author Immortius
 */
public class NetworkComponent implements Component {
    // Network identifier for the entity
    @Replicate
    private int networkId;

    @Replicate
    public EntityRef owner = EntityRef.NULL;

    public enum ReplicateMode {
        ALWAYS, // Always replicate this entity to all clients
        OWNER, // Always replicate this entity to its owner
        RELEVANT; // Replicate to client which this entity is relevant to (based on distance)
    }

    public ReplicateMode replicateMode = ReplicateMode.RELEVANT;

    public void setNetworkId(int networkId) {
        this.networkId = networkId;
    }

    public int getNetworkId() {
        return networkId;
    }
}
