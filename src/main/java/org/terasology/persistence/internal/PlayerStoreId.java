package org.terasology.persistence.internal;

import org.terasology.protobuf.EntityData;

import java.util.Objects;

/**
 * @author Immortius
 */
public class PlayerStoreId implements StoreId {
    private String id;

    public PlayerStoreId(String playerId) {
        this.id = playerId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof PlayerStoreId) {
            return Objects.equals(((PlayerStoreId) obj).id, id);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public void setUpIdentity(EntityData.EntityStoreMetadata.Builder metadata) {
        metadata.setType(EntityData.StoreType.PlayerStoreType);
        metadata.setStoreStringId(id);
    }
}
