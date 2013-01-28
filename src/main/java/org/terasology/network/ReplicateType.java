package org.terasology.network;

/**
 * @author Immortius
 */
public enum ReplicateType {
    SERVER_TO_CLIENT(false),
    SERVER_TO_OWNER(false),
    OWNER_TO_SERVER(true),
    OWNER_TO_SERVER_TO_CLIENT(true),
    INITIAL(false);

    private boolean replicateFromOwner;

    private ReplicateType(boolean fromOwner) {
        this.replicateFromOwner = fromOwner;
    }

    public boolean isReplicateFromOwner() {
        return replicateFromOwner;
    }
}
