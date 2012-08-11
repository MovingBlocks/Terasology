/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.entitySystem.persistence;

import java.util.Map;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.protobuf.EntityData;

/**
 * Interface for persist Entity System related classes to and from EntityData messages.
 *
 * @author Immortius <immortius@gmail.com>
 */
public interface EntityPersisterHelper {

    /**
     * @return The serialized form of the current EntityManager's and PrefabManager's data
     */
    EntityData.World serializeWorld();

    /**
     * @param entity
     * @return The message for a single Entity
     */
    EntityData.Entity serializeEntity(EntityRef entity);

    /**
     * @param prefab
     * @return The message for a single Prefab
     */
    EntityData.Prefab serializePrefab(Prefab prefab);

    /**
     * @param component
     * @return The message for a single Component
     */
    EntityData.Component serializeComponent(Component component);

    /**
     * Deserializes a world message, applying it to the current EntityManager
     *
     * @param world
     */
    void deserializeWorld(EntityData.World world);

    EntityRef deserializeEntity(EntityData.Entity entityData);

    Prefab deserializePrefab(EntityData.Prefab prefabData);

    /**
     * Deserializes a prefab, adjusting the prefab's name (and parent's names) to be within the supplied
     * package context if necessary.
     *
     * @param prefabData
     * @param packageContext
     * @return The deserialized prefab
     */
    Prefab deserializePrefab(EntityData.Prefab prefabData, String packageContext);

    Component deserializeComponent(EntityData.Component componentData);

    /**
     * @return Should serialization use a lookup table to map component types to indexes. This saves space in the
     *         final result, but reduces readability of text formats
     */
    boolean isUsingLookupTables();

    /**
     * Sets whether serialization should use a lookup table to map component types to indexes. This saves space in
     * the final result, but reduces readability of text formats
     *
     * @param enabled
     */
    void setUsingLookupTables(boolean enabled);

    /**
     * Sets the id table to use when encountering components with type indexes. This is useful when serializing
     * individual entities/prefabs/components.
     * <p/>
     * When using serialize/deserializeWorld, a table will automatically be generated.
     *
     * @param componentIdTable
     */
    public void setComponentTypeIdTable(Map<Integer, Class<? extends Component>> componentIdTable);

    /**
     * Clears the current component id table
     */
    public void clearComponentTypeIdTable();

    public EntityManager getEntityManager();

    public PrefabManager getPrefabManager();

    public ComponentLibrary getComponentLibrary();

    public void setEntityManager(PersistableEntityManager entityManager);

    public void setPrefabManager(PrefabManager prefabManager);

    public void setComponentLibrary(ComponentLibrary componentLibrary);


}