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
package org.terasology.entitySystem.internal;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.network.Replicate;

/**
 * Component for storing entity system information on an entity
 *
 * @author Immortius <immortius@gmail.com>
 */
public class EntityInfoComponent implements Component {
    public String parentPrefab = "";
    public boolean persisted = true;

    @Replicate
    public EntityRef owner = EntityRef.NULL;
    public boolean alwaysRelevant = false;

    public EntityInfoComponent() {
    }

    public EntityInfoComponent(String parentPrefab, boolean persisted, boolean alwaysRelevant) {
        this.parentPrefab = parentPrefab;
        this.persisted = persisted;
        this.alwaysRelevant = alwaysRelevant;
    }
}
