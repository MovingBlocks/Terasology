package org.terasology.network;

/**
 * @author Immortius
 */
public enum ReplicateDirection {
    SERVER_TO_CLIENT(false),
    OWNER_TO_SERVER(true),
    OWNER_TO_SERVER_TO_CLIENT(true);

    private boolean replicateFromOwner;

    private ReplicateDirection(boolean fromOwner) {
        this.replicateFromOwner = fromOwner;
    }

    public boolean isReplicateFromOwner() {
        return replicateFromOwner;
    }
}
