// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

/**
 */
public enum FieldReplicateType {
    SERVER_TO_CLIENT(false),
    SERVER_TO_OWNER(false),
    OWNER_TO_SERVER(true),
    OWNER_TO_SERVER_TO_CLIENT(true);

    private boolean replicateFromOwner;

    FieldReplicateType(boolean fromOwner) {
        this.replicateFromOwner = fromOwner;
    }

    public boolean isReplicateFromOwner() {
        return replicateFromOwner;
    }
}
