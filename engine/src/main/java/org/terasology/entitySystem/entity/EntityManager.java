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

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.internal.EngineEntityPool;
import org.terasology.entitySystem.entity.internal.EngineSectorManager;
import org.terasology.entitySystem.event.internal.EventSystem;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.sectors.SectorSimulationComponent;

import java.util.Map;

public interface EntityManager extends EntityPool {

    /**
     * Creates a new EntityRef in sector-scope
     *
     * @param maxDelta the maximum delta for the sector entity's simulations
     *                 @see SectorSimulationComponent#maxDelta
     * @return the newly created EntityRef
     */
    EntityRef createSectorEntity(long maxDelta);

    /**
     * @return A new entity with a copy of each of the other entity's components
     * @deprecated Use EntityRef.copy() instead.
     */
    @Deprecated
    EntityRef copy(EntityRef other);

    /**
     * Creates a copy of the components of an entity.
     *
     * @return A map of components types to components copied from the target entity.
     */
    // TODO: Remove? A little dangerous due to ownership
    Map<Class<? extends Component>, Component> copyComponents(EntityRef original);

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

}
