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
package org.terasology.entitySystem.prefab;

import org.terasology.entitySystem.Component;

import java.util.Collection;

/**
 * A PrefabManager keep Prefabs organized and available to the game engine.
 *
 * @author Immortius <immortius@gmail.com>
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public interface PrefabManager {

    /**
     * Creates a Prefab if it doesn't already exist and registers it in a prefab table, then returns it.
     *
     * @param name The name the Prefab should be given or already exists under
     * @return The created or already existing Prefab
     */
    Prefab createPrefab(String name);

    /**
     * Returns the named Prefab or null if it doesn't exist.
     *
     * @param name The name of the desired Prefab
     * @return Prefab requested or null if it doesn't exist
     */
    Prefab getPrefab(String name);

    /**
     * Tests whether a named Prefab exists or not.
     *
     * @param name The name of the Prefab to look for
     * @return True if found, false if not
     */
    boolean exists(String name);

    /**
     * Registers a Prefab to a table by its contained name if it doesn't already exist, otherwise throws an exception.
     *
     * @param prefab The Prefab to register (which also holds its name)
     * @return The provided Prefab
     */
    Prefab registerPrefab(Prefab prefab);

    /**
     * Returns all loaded prefabs.
     *
     * @return Collection containing all prefabs
     */
    Collection<Prefab> listPrefabs();

    /**
     * Returns all loaded prefabs that include the supplied Component (which may result in an empty set).
     *
     * @param withComponent a Component to filter by
     * @return Collection containing all prefabs that include the supplied Component
     */
    Collection<Prefab> listPrefabs(Class<? extends Component> withComponent);

    /**
     * Removes a named Prefab from the storage table. No action if it doesn't exist.
     *
     * @param name Name of the Prefab to remove
     */
    void removePrefab(String name);
}
