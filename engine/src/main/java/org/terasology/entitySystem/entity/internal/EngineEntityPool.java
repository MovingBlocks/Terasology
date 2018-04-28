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
package org.terasology.entitySystem.entity.internal;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityPool;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;

import java.util.Optional;

public interface EngineEntityPool extends EntityPool {

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

    void putEntity(long entityId, BaseEntityRef ref);

    ComponentTable getComponentStore();

    /**
     * Destroys an entity without sending lifecycle events.
     * <br><br>
     * This is used by the block entity system to give an illusion of permanence to temporary block entities.
     *
     * @param entity
     */
    void destroyEntityWithoutEvents(EntityRef entity);

    void destroy(long id);

    /**
     * Fund out if a particular entity has a component of the given class.
     *
     * @param entityId the entity to check
     * @param componentClass the class to check for
     * @return whether the entity has the component
     */
    boolean hasComponent(long entityId, Class<? extends Component> componentClass);

    /**
     * Remove the entity from the pool. This does not destroy the entity, it only removes the {@link BaseEntityRef}
     * and the {@link Component}s from this pool, so that the entity can be moved to a different pool. It does
     * not invalidate the {@link EntityRef}.
     *
     * Returns an {@link Optional} {@link BaseEntityRef} if it was removed, ready to be put into another pool. If
     * nothing was removed, return {@link Optional#empty()}.
     *
     * This method is intended for use by {@link EngineEntityManager#moveToPool(long, EngineEntityPool)}. Caution
     * should be taken if this method is used elsewhere, and the caller should ensure that the the entity is
     * immediately placed in a new pool. All of the components are removed, so should be manually copied before this
     * method is called if they are to be kept in use.
     *
     * @param id the id of the entity to remove
     * @return an optional {@link BaseEntityRef}, containing the removed entity
     */
    Optional<BaseEntityRef> remove(long id);

    /**
     * Insert the {@link BaseEntityRef} into this pool, with these {@link Component}s. This only inserts the ref, adds the
     * components, and assigns the entity to this pool in the EntityManager.
     *
     * No events are sent, so this should only be used when inserting a ref that has been created elsewhere. It is
     * intended for use by {@link EngineEntityManager#moveToPool(long, EngineEntityPool)}, so caution should be taken
     * if this method is used elsewhere.
     *
     * @param ref the EntityRef of the entity to be inserted
     * @param components the entity's components
     */
    void insertRef(BaseEntityRef ref, Iterable<Component> components);

}
