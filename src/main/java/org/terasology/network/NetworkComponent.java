package org.terasology.network;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;

/**
 * @author Immortius
 */
public class NetworkComponent implements Component {
    // Network identifier for the entity
    @Replicate
    public int networkId;

    @Replicate
    public EntityRef owner;

    public enum ReplicateMode {
        ALWAYS, // Always replicate this entity to all clients
        OWNER, // Always replicate this entity to its owner
        RELEVANT; // Replicate to client which this entity is relevant to (based on distance)
    }

    public ReplicateMode replicateMode = ReplicateMode.RELEVANT;

}
