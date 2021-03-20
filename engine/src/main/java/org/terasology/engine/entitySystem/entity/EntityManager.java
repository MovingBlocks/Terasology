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
package org.terasology.engine.entitySystem.entity;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityPool;
import org.terasology.engine.entitySystem.event.internal.EventSystem;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.sectors.SectorSimulationComponent;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.world.internal.WorldInfo;

import java.util.List;
import java.util.Map;

public interface EntityManager extends EntityPool {

    /**
     * Creates a new EntityRef in sector-scope
     *
     * @param maxDelta the maximum delta for the sector entity's simulations (both unloaded and loaded)
     *                 @see SectorSimulationComponent#unloadedMaxDelta
     *                 @see SectorSimulationComponent#loadedMaxDelta
     * @return the newly created EntityRef
     */
    EntityRef createSectorEntity(long maxDelta);

    /**
     * Takes the {@link GameManifest}, gets {@link WorldInfo} of different worlds from it and
     * creates pool for each world.
     *
     * @param gameManifest The game for which multiple pools will be needed.
     */
    void createWorldPools(GameManifest gameManifest);

    List<EngineEntityPool> getWorldPools();

    Map<WorldInfo, EngineEntityPool> getWorldPoolsMap();

    Map<Long, EngineEntityPool> getPoolMap();

    Map<EngineEntityPool, Long> getPoolCounts();

    /**
     * Creates a new EntityRef in sector-scope
     *
     * @param unloadedMaxDelta the maximum delta for the simulations when the entity's watched chunks aren't loaded
     *                         @see SectorSimulationComponent#unloadedMaxDelta
     * @param loadedMaxDelta the maximum delta when at least one of the entity's watched chunks is loaded
     *                       @see SectorSimulationComponent#loadedMaxDelta
     * @return the newly created EntityRef
     */
    EntityRef createSectorEntity(long unloadedMaxDelta, long loadedMaxDelta);

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

    boolean moveToPool(long id, EngineEntityPool pool);

}
