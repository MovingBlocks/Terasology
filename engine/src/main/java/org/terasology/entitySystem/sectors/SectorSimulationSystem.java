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
package org.terasology.entitySystem.sectors;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.BaseEntityRef;
import org.terasology.entitySystem.entity.internal.EntityScope;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.ChunkMath;
import org.terasology.registry.In;
import org.terasology.world.WorldComponent;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.event.BeforeChunkUnload;
import org.terasology.world.chunks.event.OnChunkLoaded;

/**
 * This system handles the sending of sector-level simulation events, so module systems can subscribe to them for their
 * own sector-scope entities.
 *
 * For the purposes of this class, sector-scope means any {@link EntityRef} that's scope is SECTOR, and that has a
 * {@link SectorSimulationComponent}. The entity should automatically have the component, as long as it was created by
 * {@link EntityManager#createSectorEntity(long)} or its scope was set by
 * {@link BaseEntityRef#setScope(EntityScope)}.
 *
 * It periodically sends a {@link SectorSimulationEvent} to all sector-scope entities, which should trigger any
 * simulation that needs to happen regardless of whether or not the entity's chunk is loaded.
 *
 * It periodically sends a {@link LoadedSectorUpdateEvent} to all sector-scope entities which are in a loaded chunk.
 * This should trigger any block-based or chunk-based actions that need to happen.
 *
 * It also sends {@link OnChunkLoaded} and {@link BeforeChunkUnload} events to the entities, where appropriate. These
 * should be captured by filtering only to entities with a {@link SectorSimulationComponent}, to avoid capturing the
 * event sent to the world entity.
 */
@RegisterSystem
public class SectorSimulationSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    @In
    private DelayManager delayManager;

    @In
    private Time time;

    public static final String SECTOR_SIMULATION_ACTION = "sector:simulationAction";


    /* Set periodic events for each entity */


    @ReceiveEvent(components = SectorSimulationComponent.class)
    public void simulationComponentAdded(OnAddedComponent event, EntityRef entity) {
        registerSimulationComponent(entity);
    }

    @ReceiveEvent(components = SectorSimulationComponent.class)
    public void simulationComponentChanged(OnChangedComponent event, EntityRef entity) {
        registerSimulationComponent(entity);
    }

    @ReceiveEvent(components = SectorSimulationComponent.class)
    public void simulationComponentRemoved(BeforeRemoveComponent event, EntityRef entity) {
        unregisterSimulationComponent(entity);
    }

    /**
     * Add the sector simulation periodic action event for this sector-level entity.
     *
     * This event will be sent on a schedule based on {@link SectorSimulationComponent#maxDelta}, and will be used to
     * send {@link SectorSimulationEvent} and/or {@link LoadedSectorUpdateEvent}, as appropriate.
     *
     * This periodic event gets processed by
     * {@link SectorSimulationSystem#processPeriodicSectorEvent(PeriodicActionTriggeredEvent, EntityRef)};
     *
     * @param entity the entity to add the periodic event for
     */
    private void registerSimulationComponent(EntityRef entity) {
        SectorSimulationComponent simulationComponent = entity.getComponent(SectorSimulationComponent.class);

        unregisterSimulationComponent(entity);
        delayManager.addPeriodicAction(entity, SECTOR_SIMULATION_ACTION, 0, (long) (simulationComponent.maxDelta * 1000));
    }

    /**
     * Cancel any sector simulation periodic event for this sector-level entity
     *
     * @param entity the event to cancel the periodic event for
     */
    private void unregisterSimulationComponent(EntityRef entity) {
        if (delayManager.hasPeriodicAction(entity, SECTOR_SIMULATION_ACTION)) {
            delayManager.cancelPeriodicAction(entity, SECTOR_SIMULATION_ACTION);
        }
    }


    /* Send events for the module to capture */


    /**
     * Retrieve the periodic event sent to each sector-scope entity, and send the appropriate event(s) depending on the
     * status of the the chunk the entity is in.
     *
     * Also send the correct delta, and update the {@link SectorSimulationComponent#lastSimulationTime}.
     *
     * @param event the periodic action event sent at intervals of {@link SectorSimulationComponent#maxDelta}
     * @param entity the sector-scope entity the event was sent to
     */
    @ReceiveEvent(components = SectorSimulationComponent.class)
    public void processPeriodicSectorEvent(PeriodicActionTriggeredEvent event, EntityRef entity) {
        if (event.getActionId().equals(SECTOR_SIMULATION_ACTION)) {
            float delta = simulationDelta(entity);

            LocationComponent loc = entity.getComponent(LocationComponent.class);
            if (loc != null && worldProvider.isBlockRelevant(loc.getWorldPosition())) {
                sendLoadedSectorUpdateEvent(entity, delta);
            } else {
                entity.send(new SectorSimulationEvent(delta));
            }
        }
    }

    /**
     * Forward the OnChunkLoaded event to the appropriate sector-scope entities, if their chunk was loaded.
     *
     * @param event the event sent when any chunk is loaded
     * @param worldEntity ignored
     */
    @ReceiveEvent(components = WorldComponent.class)
    public void forwardOnChunkLoaded(OnChunkLoaded event, EntityRef worldEntity) {
        for (EntityRef entity : entityManager.getEntitiesWith(SectorSimulationComponent.class)) {
            LocationComponent loc = entity.getComponent(LocationComponent.class);
            if (loc != null && ChunkMath.calcChunkPos(loc.getWorldPosition()).equals(event.getChunkPos())) {
                entity.send(new OnChunkLoaded(event.getChunkPos()));
                sendLoadedSectorUpdateEvent(entity, simulationDelta(entity));
            }
        }
    }

    /**
     * Forward the BeforeChunkUnloaded event to the appropriate sector-scope entities, if their chunk was unloaded.
     *
     * @param event the event sent when any chunk is about to be unloaded
     * @param worldEntity ignored
     */
    @ReceiveEvent(components = WorldComponent.class)
    public void forwardChunkUnload(BeforeChunkUnload event, EntityRef worldEntity) {
        for (EntityRef entity : entityManager.getEntitiesWith(SectorSimulationComponent.class)) {
            LocationComponent loc = entity.getComponent(LocationComponent.class);
            if (loc != null && ChunkMath.calcChunkPos(loc.getWorldPosition()).equals(event.getChunkPos())) {
                entity.send(new BeforeChunkUnload(event.getChunkPos()));
            }
        }
    }

    /**
     * Send the appropriate events to a sector-scope entity in a loaded chunk.
     *
     * @param entity the entity to send the events to
     * @param delta the time since the last time {@link SectorSimulationEvent} was sent
     */
    private void sendLoadedSectorUpdateEvent(EntityRef entity, float delta) {
        entity.send(new SectorSimulationEvent(delta));
        entity.send(new LoadedSectorUpdateEvent());
    }

    /**
     * Calculate the time since the last {@link SectorSimulationEvent} was sent. This also updates the
     * {@link SectorSimulationComponent#lastSimulationTime}, so future deltas will be correct.
     * @param entity
     * @return
     */
    private float simulationDelta(EntityRef entity) {
        SectorSimulationComponent simulationComponent = entity.getComponent(SectorSimulationComponent.class);
        float currentTime = time.getGameTime();
        if (simulationComponent.lastSimulationTime == 0) {
            simulationComponent.lastSimulationTime = currentTime;
        }
        float delta = currentTime - simulationComponent.lastSimulationTime;
        simulationComponent.lastSimulationTime = currentTime;
        return delta;
    }

}
