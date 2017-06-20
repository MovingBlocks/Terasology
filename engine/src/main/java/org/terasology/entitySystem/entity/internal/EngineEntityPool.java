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

/**
 */
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
}
