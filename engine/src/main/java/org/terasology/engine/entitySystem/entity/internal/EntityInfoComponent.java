// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.entity.internal;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.network.Replicate;

import javax.annotation.Nullable;

/**
 * Component for storing entity system information on an entity
 *
 */
public class EntityInfoComponent implements Component {
    // TODO: Switch this to use Optional<Prefab>
    public Prefab parentPrefab;
    /**
     * To simplify things for the {@link StorageManager} the persistent property of entities must not be changed
     * after creation.
     */
    public boolean persisted = true;

    @Replicate
    public EntityRef owner = EntityRef.NULL;
    public boolean alwaysRelevant;
    public EntityScope scope = EntityScope.getDefaultScope();

    public EntityInfoComponent() {
    }

    public EntityInfoComponent(@Nullable Prefab parentPrefab, boolean persisted, boolean alwaysRelevant) {
        this.parentPrefab = parentPrefab;
        this.persisted = persisted;
        this.alwaysRelevant = alwaysRelevant;
    }

    public EntityInfoComponent(@Nullable Prefab parentPrefab, boolean persisted, boolean alwaysRelevant, EntityScope scope) {
        this(parentPrefab, persisted, alwaysRelevant);
        this.scope = scope;
    }
}
