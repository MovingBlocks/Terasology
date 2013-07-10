package org.terasology.persistence.internal;

import gnu.trove.set.TIntSet;

/**
 * @author Immortius
 */
final class StoreMetadata {
    private StoreId id;
    private TIntSet externalReferences;

    public StoreMetadata(StoreId id, TIntSet externalReferences) {
        this.id = id;
        this.externalReferences = externalReferences;
    }

    public StoreId getId() {
        return id;
    }

    public TIntSet getExternalReferences() {
        return externalReferences;
    }
}
