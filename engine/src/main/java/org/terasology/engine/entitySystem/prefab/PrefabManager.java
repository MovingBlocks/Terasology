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
package org.terasology.entitySystem.prefab;

import org.terasology.entitySystem.Component;

import java.util.Collection;

/**
 * A PrefabManager keep Prefabs organized and available to the game engine.
 *
 */
// TODO: This is basically unnecessary now, remove and just use Assets?
public interface PrefabManager {

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
     * Returns all loaded prefabs.
     *
     * @return Collection containing all prefabs
     */
    Iterable<Prefab> listPrefabs();

    /**
     * Returns all loaded prefabs that include the supplied Component (which may result in an empty set).
     *
     * @param withComponent a Component to filter by
     * @return Collection containing all prefabs that include the supplied Component
     */
    Collection<Prefab> listPrefabs(Class<? extends Component> withComponent);

}
