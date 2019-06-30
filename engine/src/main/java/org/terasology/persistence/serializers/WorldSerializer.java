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
package org.terasology.persistence.serializers;

import org.terasology.entitySystem.Component;
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
