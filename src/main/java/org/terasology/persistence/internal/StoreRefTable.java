package org.terasology.persistence.internal;

import gnu.trove.set.TIntSet;

/**
 * @author Immortius
 */
class StoreRefTable {
    private String id;
    private TIntSet externalReferences;

    public StoreRefTable(String id, TIntSet externalReferences) {
        this.id = id;
        this.externalReferences = externalReferences;
    }

    public String getId() {
        return id;
    }

    public TIntSet getExternalReferences() {
        return externalReferences;
    }
}
