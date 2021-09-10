// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.entity.internal;

import org.terasology.engine.entitySystem.entity.EntityPool;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.gestalt.entitysystem.component.Component;

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
