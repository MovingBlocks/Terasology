// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.entity;

import org.terasology.engine.entitySystem.entity.internal.EngineEntityPool;
import org.terasology.engine.entitySystem.entity.internal.EngineSectorManager;
import org.terasology.gestalt.entitysystem.component.Component;

public interface LowLevelEntityManager extends EntityManager {

    boolean isExistingEntity(long id);

    boolean isActiveEntity(long id);

    <T extends Component> T getComponent(long id, Class<T> componentClass);

    boolean hasComponent(long id, Class<? extends Component> componentClass);

    <T extends Component> T addComponent(long id, T component);

    <T extends Component> T removeComponent(long id, Class<T> componentClass);

    void saveComponent(long id, Component component);

    Iterable<Component> iterateComponents(long id);

    void destroy(long id);

    /**
     * @return the global entity pool
     */
    EngineEntityPool getGlobalPool();

    EngineEntityPool getCurrentWorldPool();

    /**
     * @return the sector manager
     */
    EngineSectorManager getSectorManager();

    /**
     * Moves the given entity into the given pool. This will move the entity and all of its components, as well as
     * re-assigning it in the entity manager.
     *
     * @param id the id of the entity to move
     * @param pool the pool to move the entity into
     * @return whether the move was successful
     */
    boolean moveToPool(long id, EngineEntityPool pool);

}
