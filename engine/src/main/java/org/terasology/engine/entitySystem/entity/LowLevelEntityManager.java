/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.engine.entitySystem.entity;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityPool;
import org.terasology.engine.entitySystem.entity.internal.EngineSectorManager;

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
