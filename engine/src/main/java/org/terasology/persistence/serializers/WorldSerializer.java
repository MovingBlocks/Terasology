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

import org.terasology.protobuf.EntityData;

/**
 * Serializes an entity system, with all prefabs and entities.
 *
 */
public interface WorldSerializer {

    /**
     * @return The serialized form of the current EntityManager's and PrefabManager's data
     */
    EntityData.GlobalStore serializeWorld(boolean verbose);

    /**
     * Deserializes a world message, applying it to the current EntityManager
     *
     * @param world
     */
    void deserializeWorld(EntityData.GlobalStore world);

}
