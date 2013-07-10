package org.terasology.persistence.internal;

import org.terasology.protobuf.EntityData;

/**
 * @author Immortius
 */
public interface StoreId {

    void setUpIdentity(EntityData.EntityStoreMetadata.Builder metadata);
}
