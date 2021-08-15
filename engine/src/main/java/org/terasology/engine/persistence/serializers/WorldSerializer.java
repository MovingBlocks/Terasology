// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.serializers;

import org.terasology.engine.entitySystem.Component;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * Serializes an entity system, with all prefabs and entities.
 *
 */
public interface WorldSerializer {

    /**
     * Serialize current EntityManager's and PrefabManager's data
     * @param verbose verbosity level of serialization
     * @return The serialized form of the current EntityManager's and PrefabManager's data
     */
    EntityData.GlobalStore serializeWorld(boolean verbose);

    /**
     * Serialize current EntityManager's and PrefabManager's data filtered by list of Components
     * @param verbose verbosity level of serialization
     * @param filterComponent list of component classes to filter world entities and prefabs
     * @return The serialized form of the current EntityManager's and PrefabManager's data filtered by list of Components
     */
    EntityData.GlobalStore serializeWorld(boolean verbose, List<Class<? extends Component>> filterComponent);


    /**
     * Deserializes a world message, applying it to the current EntityManager
     *
     * @param world
     */
    void deserializeWorld(EntityData.GlobalStore world);

}
