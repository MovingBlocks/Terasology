/*
 * Copyright 2017 MovingBlocks
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

import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

public interface EntityPool {

    /**
     * Removes all entities from the pool.
     */
    void clear();

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
     * Creates a new entity from the given components.
     *
     * @param components the components to create this entity from
     * @param sendLifecycleEvents will only send lifecycle events if this is true
     * @return
     */
    EntityRef create(Iterable<Component> components, boolean sendLifecycleEvents);

    /**
     * @param prefabName The name of the prefab to create.
     * @return A new entity, based on the the prefab of the given name. If the prefab doesn't exist, just a new entity.
     */
    EntityRef create(String prefabName);

    /**
     * @return A new entity, based on the given prefab
     */
    EntityRef create(Prefab prefab);

    // TODO: Review. Probably better to move these into a static helper

    /**
     * @return A new entity, based on the given prefab, at the desired position
     * @deprecated This method is scheduled for removal in an upcoming version.
     *             Use the JOML implementation instead: {@link #create(String,Vector3fc)}.
     */
    @Deprecated
    EntityRef create(String prefab, Vector3f position);

    /**
     * @return A new entity, based on the given prefab, at the desired position
     */
    EntityRef create(String prefab, Vector3fc position);

    /**
     * @return A new entity, based on the given prefab, at the desired position
     * @deprecated This method is scheduled for removal in an upcoming version.
     *             Use the JOML implementation instead: {@link #create(Prefab,Vector3fc)}.
     */
    @Deprecated
    EntityRef create(Prefab prefab, Vector3f position);

    /**
     * @return A new entity, based on the given prefab, at the desired position
     */
    EntityRef create(Prefab prefab, Vector3fc position);

    /**
     * @return A new entity, based on the given prefab, at the desired position, and with the desired rotation
     * @deprecated This method is scheduled for removal in an upcoming version.
     *             Use the JOML implementation instead: {@link #create(Prefab,Vector3fc,Quaternionfc)}.
     */
    @Deprecated
    EntityRef create(Prefab prefab, Vector3f position, Quat4f rotation);


    /**
     * @return A new entity, based on the given prefab, at the desired position, and with the desired rotation
     */
    EntityRef create(Prefab prefab, Vector3fc position, Quaternionfc rotation);

    /**
     * Creates an entity but doesn't send any lifecycle events.
     * <br><br>
     * This is used by the block entity system to give an illusion of permanence to temporary block entities.
     *
     * @return The newly created entity ref.
     */
    EntityRef createEntityWithoutLifecycleEvents(Iterable<Component> components);

    /**
     * Creates an entity but doesn't send any lifecycle events.
     * <br><br>
     * This is used by the block entity system to give an illusion of permanence to temporary block entities.
     *
     * @return The newly created entity ref.
     */
    EntityRef createEntityWithoutLifecycleEvents(String prefab);

    EntityRef createEntityWithoutLifecycleEvents(Prefab prefab);

    /**
     * Allows the creation of an entity with a given id - this is used
     * when loading persisted entities
     *
     * @return The entityRef for the newly created entity
     */
    EntityRef createEntityWithId(long id, Iterable<Component> components);

    /**
     * Retrieve the entity ref with the given id.
     *
     * @return the {@link EntityRef}, if it exists; {@link EntityRef#NULL} otherwise
     */
    EntityRef getEntity(long id);

    /**
     * @return an iterable over all of the entities in this pool
     */
    Iterable<EntityRef> getAllEntities();

    /**
     * @return An iterable over all entities with the provided component types.
     */
    Iterable<EntityRef> getEntitiesWith(Class<? extends Component>... componentClasses);

    /**
     * @return A count of entities with the provided component types
     */
    int getCountOfEntitiesWith(Class<? extends Component>... componentClasses);

    /**
     * @return A count of currently active entities
     */
    int getActiveEntityCount();

    /**
     * Does this pool contain the given entity?
     *
     * @param id the id of the entity to search for
     * @return true if this pool contains the entity; false otherwise
     */
    boolean contains(long id);
}
