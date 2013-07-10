package org.terasology.persistence.internal;

import com.google.common.base.Objects;
import org.terasology.math.Vector3i;
import org.terasology.protobuf.EntityData;

/**
 * @author Immortius
 */
public class ChunkStoreId implements StoreId {
    private Vector3i pos;

    public ChunkStoreId(Vector3i pos) {
        this.pos = new Vector3i(pos);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ChunkStoreId) {
            return Objects.equal(pos, ((ChunkStoreId) obj).pos);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pos);
    }

    @Override
    public void setUpIdentity(EntityData.EntityStoreMetadata.Builder metadata) {
        metadata.setType(EntityData.StoreType.ChunkStoreType);
        metadata.addStoreIntegerId(pos.x);
        metadata.addStoreIntegerId(pos.y);
        metadata.addStoreIntegerId(pos.z);
    }
}
