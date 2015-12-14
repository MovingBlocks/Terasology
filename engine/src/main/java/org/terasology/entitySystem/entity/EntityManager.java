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
package org.terasology.entitySystem.entity;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

import java.util.Map;

/**
 */
public interface EntityManager {

    /**
     * Creates an EntityBuilder.
     *
     * @return A new entity builder
     */
    EntityBuilder newBuilder();

    /**
     * Creates an EntityBuilder, from a prefab
     *
     * @return A new entity builder
     */
    EntityBuilder newBuilder(String prefabName);

    /**
     * Creates an EntityBuilder, from a prefab
     *
     * @return A new entity builder
     */
    EntityBuilder newBuilder(Prefab prefab);

    /**
     * @return A references to a new, unused entity
     */
    EntityRef create();

    /**
     * @return A references to a new, unused entity with the desired components
     */
    EntityRef create(Component... components);

    /**
     * @return A references to a new, unused entity with the desired components
     */
    EntityRef create(Iterable<Component> components);

    /**
     * @param prefabName The name of the prefab to create.
     * @return A new entity, based on the the prefab of the given name. If the prefab doesn't exist, just a new entity.
     */
    EntityRef create(String prefabName);

    /**
     * @param prefab
     * @return A new entity, based on the given prefab
     */
    EntityRef create(Prefab prefab);

    // TODO: Review. Probably better to move these into a static helper

    /**
     * @param prefab
     * @param position
     * @return A new entity, based on the given prefab, at the desired position
     */
    EntityRef create(String prefab, Vector3f position);

    /**
     * @param prefab
     * @param position
     * @return A new entity, based on the given prefab, at the desired position
     */
    EntityRef create(Prefab prefab, Vector3f position);

    /**
     * @param prefab
     * @param position
     * @param rotation
     * @return
     */
    EntityRef create(Prefab prefab, Vector3f position, Quat4f rotation);

    /**
     * @param id
     * @return The entity with the given id, or the null entity
     */
    EntityRef getEntity(long id);

    /**
     * @param other
     * @return A new entity with a copy of each of the other entity's components
     * @deprecated Use EntityRef.copy() instead.
     */
    @Deprecated
    EntityRef copy(EntityRef other);

    /**
     * Creates a copy of the components of an entity.
     *
     * @param original
     * @return A map of components types to components copied from the target entity.
     */
    // TODO: Remove? A little dangerous due to ownership
    Map<Class<? extends Component>, Component> copyComponents(EntityRef original);

    /**
     * @return An iterable over all entities
     */
    Iterable<EntityRef> getAllEntities();

    /**
     * @param componentClasses
     * @return An iterable over all entities with the provided component types.
     */
    Iterable<EntityRef> getEntitiesWith(Class<? extends Component>... componentClasses);

    /**
     * @param componentClasses
     * @return A count of entities with the provided component types
     */
    int getCountOfEntitiesWith(Class<? extends Component>... componentClasses);

    /**
     * @return The event system being used by the entity manager
     */
    EventSystem getEventSystem();

    /**
     * @return The prefab manager being used by the entity manager
     */
    PrefabManager getPrefabManager();

    /**
     * @return The component library being used by the entity manager
     */
    ComponentLibrary getComponentLibrary();

    /**
     * @return A count of currently active entities
     */
    int getActiveEntityCount();
}
