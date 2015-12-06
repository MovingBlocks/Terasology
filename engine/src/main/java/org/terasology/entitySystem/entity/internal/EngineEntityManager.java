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
package org.terasology.entitySystem.entity.internal;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.LowLevelEntityManager;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;

/**
 */
public interface EngineEntityManager extends LowLevelEntityManager {

    void setEntityRefStrategy(RefStrategy strategy);

    /**
     * Creates an entity but doesn't send any lifecycle events.
     * <br><br>
     * This is used by the block entity system to give an illusion of permanence to temporary block entities.
     *
     * @param components
     * @return The newly created entity ref.
     */
    EntityRef createEntityWithoutLifecycleEvents(Iterable<Component> components);

    /**
     * Creates an entity but doesn't send any lifecycle events.
     * <br><br>
     * This is used by the block entity system to give an illusion of permanence to temporary block entities.
     *
     * @param prefab
     * @return The newly created entity ref.
     */
    EntityRef createEntityWithoutLifecycleEvents(String prefab);

    EntityRef createEntityWithoutLifecycleEvents(Prefab prefab);

    /**
     * Destroys an entity without sending lifecycle events.
     * <br><br>
     * This is used by the block entity system to give an illusion of permanence to temporary block entities.
     *
     * @param entity
     */
    void destroyEntityWithoutEvents(EntityRef entity);

    /**
     * Allows the creation of an entity with a given id - this is used
     * when loading persisted entities
     *
     * @param id
     * @param components
     * @return The entityRef for the newly created entity
     */
    EntityRef createEntityWithId(long id, Iterable<Component> components);

    /**
     * Creates an entity ref with the given id. This is used when loading components with references.
     *
     * @param id
     * @return The entityRef for the given id
     */
    EntityRef createEntityRefWithId(long id);

    /**
     * This is used to persist the entity manager's state
     *
     * @return The id that will be used for the next entity (after freed ids are used)
     */
    long getNextId();

    /**
     * Sets the next id the entity manager will use. This is used when restoring the entity manager's state.
     *
     * @param id
     */
    void setNextId(long id);

    /**
     * Removes all entities from the entity manager and resets its state.
     */
    void clear();

    /**
     * Removes an entity while keeping its id in use - this allows it to be stored
     *
     * @param entity
     */
    void deactivateForStorage(EntityRef entity);

    /**
     * Subscribes to all changes related to entities. Used by engine systems.
     *
     * @param subscriber
     */
    void subscribeForChanges(EntityChangeSubscriber subscriber);

    /**
     * Subscribe for notification the destruction of entities.
     *
     * @param subscriber
     */
    void subscribeForDestruction(EntityDestroySubscriber subscriber);

    /**
     * Unsubscribes from changes relating to entities. Used by engine systems.
     *
     * @param subscriber
     */
    void unsubscribe(EntityChangeSubscriber subscriber);

    /**
     * Sets the event system the entity manager will use to propagate life cycle events.
     *
     * @param system
     */
    void setEventSystem(EventSystem system);

    /**
     * @return The default serialization library to use for serializing components
     */
    TypeSerializationLibrary getTypeSerializerLibrary();
}
