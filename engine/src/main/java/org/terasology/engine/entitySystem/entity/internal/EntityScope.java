// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.entity.internal;

import org.terasology.context.annotation.API;

@API
public enum EntityScope {
    GLOBAL(true),
    SECTOR(true),
    CHUNK(false);

    private boolean alwaysRelevant;

    EntityScope(boolean alwaysRelevant) {
        this.alwaysRelevant = alwaysRelevant;
    }

    public boolean getAlwaysRelevant() {
        return alwaysRelevant;
    }

    public static EntityScope getDefaultScope() {
        return CHUNK;
    }
}
