// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.prefab;

import org.terasology.engine.entitySystem.Component;

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
