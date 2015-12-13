/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.entitySystem.entity.internal;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.network.Replicate;

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

    public EntityInfoComponent() {
    }

    public EntityInfoComponent(@Nullable Prefab parentPrefab, boolean persisted, boolean alwaysRelevant) {
        this.parentPrefab = parentPrefab;
        this.persisted = persisted;
        this.alwaysRelevant = alwaysRelevant;
    }
}
