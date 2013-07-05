/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.persistence;

import org.terasology.entitySystem.EntityRef;

import java.io.IOException;
import java.util.Map;

/**
 * A store for entities. When added to the store, an entity is deactivated and removed from the entity system. Then
 * when restored, the entity is removed from the store, added too the entity system and reactivated.
 */
public interface EntityStore {

    void store(EntityRef entity);

    void store(EntityRef entity, String name);

    /**
     * Restores all the stored entities
     *
     * @return A mapping of stored entities to names they were stored with.
     * @throws java.io.IOException
     */
    public Map<String, EntityRef> restoreAll() throws IOException;

}
