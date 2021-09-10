// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.entity.internal;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

import javax.annotation.Nullable;

/**
 * Component for storing entity system information on an entity
 */
public class EntityInfoComponent implements Component<EntityInfoComponent> {
    // TODO: Switch this to use Optional<Prefab>
    public Prefab parentPrefab;
    /**
     * To simplify things for the {@link org.terasology.engine.persistence.StorageManager StorageManager} the persistent
     * property of entities must not be changed after creation.
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

    public EntityInfoComponent(@Nullable Prefab parentPrefab, boolean persisted, boolean alwaysRelevant,
                               EntityScope scope) {
        this(parentPrefab, persisted, alwaysRelevant);
        this.scope = scope;
    }

    @Override
    public void copyFrom(EntityInfoComponent other) {
        this.persisted = other.persisted;
        this.parentPrefab = other.parentPrefab;
        this.alwaysRelevant = other.alwaysRelevant;
        this.scope = other.scope;
        this.owner = other.owner;
    }
}
