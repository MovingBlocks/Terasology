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
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;

import java.util.Optional;

public interface EngineEntityManager extends LowLevelEntityManager, EngineEntityPool {

    void setEntityRefStrategy(RefStrategy strategy);

    RefStrategy getEntityRefStrategy();

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
    EntityRef getEntity(long id);

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
    TypeHandlerLibrary getTypeSerializerLibrary();

    /**
     * Gets the entity pool associated with a given entity.
     *
     * If the pool isn't assigned or the entity doesn't exist, an error is logged and the optional is returned empty
     *
     * @param id the id of the entity
     * @return an {@link Optional} containing the pool if it exists, or empty
     */
    Optional<EngineEntityPool> getPool(long id);

    /**
     * Creates a new entity.
     *
     * This method is designed for internal use by the EntityBuilder; the {@link #create} methods should be used in
     * most circumstances.
     *
     * @return the id of the newly created entity
     */
    long createEntity();

    /**
     * Attempts to register a new id with the entity manager.
     *
     * This method is designed for internal use by the EntityBuilder.
     *
     * @param id the id to register
     * @return whether the registration was successful
     */
    boolean registerId(long id);

    /**
     * Notifies the appropriate subscribers that an entity's component was changed.
     *
     * This method is designed for internal use by the EntityBuilder.
     *
     * @param changedEntity the entity which the changed component belongs to
     * @param component the class of the changed component
     */
    void notifyComponentAdded(EntityRef changedEntity, Class<? extends Component> component);

    /**
     *
     * Tell the EntityManager which pool the given entity is in, so that its components can be found.
     *
     * This is designed for internal use; {@link #moveToPool(long, EngineEntityPool)} should be used to move entities
     * between pools.
     *
     * @param entityId the id of the entity to assign
     * @param pool the pool the entity is in
     */
    void assignToPool(long entityId, EngineEntityPool pool);

}
