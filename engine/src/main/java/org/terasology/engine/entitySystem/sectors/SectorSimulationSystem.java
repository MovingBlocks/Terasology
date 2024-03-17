// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.sectors;

import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.BaseEntityRef;
import org.terasology.engine.entitySystem.entity.internal.EntityScope;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldComponent;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.event.BeforeChunkUnload;
import org.terasology.engine.world.chunks.event.OnChunkLoaded;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

import java.util.stream.Collectors;

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
 * simulation that needs to happen regardless of the status of the entity's watched chunks.
 *
 * It periodically sends a {@link LoadedSectorUpdateEvent} to all sector-scope entities which have at least one watched
 * chunk that is ready (loaded). This should trigger any block-based or chunk-based actions that need to happen.
 *
 * It also sends {@link OnChunkLoaded} and {@link BeforeChunkUnload} events to the entities, whenever the status of a
 * watched chunk changes. These should be captured by filtering only to entities with a
 * {@link SectorSimulationComponent}, to avoid capturing the event sent to the world entity.
 *
 * @see SectorUtil#getWatchedChunks(EntityRef) for the definition of watched chunks.
 */
@RegisterSystem
public class SectorSimulationSystem extends BaseComponentSystem {
    public static final String SECTOR_SIMULATION_ACTION = "sector:simulationAction";

    @In
    private EntityManager entityManager;

    @In
    private DelayManager delayManager;

    @In
    private Time time;

    @In
    private ChunkProvider chunkProvider;

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
        unregisterSimulationAction(entity);
    }

    /**
     * Add the sector simulation periodic action event for this sector-level entity.
     *
     * This event will be sent on a schedule based on {@link SectorSimulationComponent#loadedMaxDelta} and
     * {@link SectorSimulationComponent#unloadedMaxDelta}, and will be used to send {@link SectorSimulationEvent}
     * and/or {@link LoadedSectorUpdateEvent}, as appropriate.
     *
     * This periodic event gets processed by
     * {@link #processPeriodicSectorEvent(PeriodicActionTriggeredEvent, EntityRef)};
     *
     * @param entity the entity to add the periodic event for
     */
    private void registerSimulationComponent(EntityRef entity) {
        if (SectorUtil.getWatchedChunks(entity).isEmpty()) {
            addUnloadedAction(entity);
        } else {
            addLoadedAction(entity);
        }
    }

    private void addUnloadedAction(EntityRef entity) {
        SectorSimulationComponent simulationComponent = entity.getComponent(SectorSimulationComponent.class);
        unregisterSimulationAction(entity);
        delayManager.addPeriodicAction(entity, SECTOR_SIMULATION_ACTION, 0, simulationComponent.unloadedMaxDelta);
    }

    private void addLoadedAction(EntityRef entity) {
        SectorSimulationComponent simulationComponent = entity.getComponent(SectorSimulationComponent.class);
        unregisterSimulationAction(entity);
        delayManager.addPeriodicAction(entity, SECTOR_SIMULATION_ACTION, 0, simulationComponent.loadedMaxDelta);
    }

    /**
     * Cancel any sector simulation periodic event for this sector-level entity
     *
     * @param entity the event to cancel the periodic event for
     */
    private void unregisterSimulationAction(EntityRef entity) {
        if (delayManager.hasPeriodicAction(entity, SECTOR_SIMULATION_ACTION)) {
            delayManager.cancelPeriodicAction(entity, SECTOR_SIMULATION_ACTION);
        }
    }


    /* Send events for the module to capture */


    /**
     * Retrieve the periodic event sent to each sector-scope entity, and send the appropriate event(s) depending on the
     * status of the entity's watched chunks.
     *
     * Also send the correct delta, and update the {@link SectorSimulationComponent#lastSimulationTime}.
     *
     * @param event the periodic action event sent at intervals based on the entity's max delta values
     * @param entity the sector-scope entity the event was sent to
     */
    @ReceiveEvent(components = SectorSimulationComponent.class)
    public void processPeriodicSectorEvent(PeriodicActionTriggeredEvent event, EntityRef entity) {
        if (event.getActionId().equals(SECTOR_SIMULATION_ACTION)) {
            long delta = simulationDelta(entity);

            boolean anyChunksReady = SectorUtil.getWatchedChunks(entity).stream()
                    .anyMatch(chunkProvider::isChunkReady);

            if (anyChunksReady) {
                sendLoadedSectorUpdateEvent(entity, delta);
            } else {
                entity.send(new SectorSimulationEvent(delta));
            }
        }
    }

    /**
     * Handles the OnChunkLoaded event for sector entities.
     *
     * Forwards the event to the appropriate sector-scope entities, if they are watching that chunk, and sends a
     * {@link SectorEntityLoad} event if this is the first watched chunk to be loaded for that entity.
     *
     * @param event the event sent when any chunk is loaded
     * @param worldEntity ignored
     */
    @ReceiveEvent(components = WorldComponent.class)
    public void chunkLoad(OnChunkLoaded event, EntityRef worldEntity) {
        for (EntityRef entity : entityManager.getEntitiesWith(SectorSimulationComponent.class)) {
            if (SectorUtil.getWatchedChunks(entity).contains(event.getChunkPos())) {
                entity.send(new OnChunkLoaded(event.getChunkPos()));
                if (SectorUtil.onlyWatchedChunk(entity, event.getChunkPos(), chunkProvider)) {
                    entity.send(new SectorEntityLoad());
                }
                sendLoadedSectorUpdateEvent(entity, simulationDelta(entity));
            }
        }
    }

    /**
     * Handles the BeforeChunkUnload event for sector entities.
     *
     * Forwards the event to the appropriate sector-scope entities, if they are watching that chunk, and sends a
     * {@link SectorEntityUnload} event if this chunk is the last chunk that the entity is watching.
     *
     * @param event the event sent when any chunk is about to be unloaded
     * @param worldEntity ignored
     */
    @ReceiveEvent(components = WorldComponent.class)
    public void chunkUnload(BeforeChunkUnload event, EntityRef worldEntity) {
        for (EntityRef entity : entityManager.getEntitiesWith(SectorSimulationComponent.class)) {
            if (SectorUtil.getWatchedChunks(entity).contains(event.getChunkPos())) {
                entity.send(new BeforeChunkUnload(event.getChunkPos()));
                if (SectorUtil.onlyWatchedChunk(entity, event.getChunkPos(), chunkProvider)) {
                    entity.send(new SectorEntityUnload());
                }
            }
        }
    }

    @ReceiveEvent(components = SectorSimulationComponent.class)
    public void sectorEntityLoaded(SectorEntityLoad event, EntityRef entity) {
        addLoadedAction(entity);
    }

    @ReceiveEvent(components = SectorSimulationComponent.class)
    public void sectorEntityUnloaded(SectorEntityUnload event, EntityRef entity) {
        addUnloadedAction(entity);
    }

    /**
     * Send the appropriate events to a sector-scope entity in a loaded chunk.
     *
     * @param entity the entity to send the events to
     * @param delta the time since the last time {@link SectorSimulationEvent} was sent, in ms
     */
    private void sendLoadedSectorUpdateEvent(EntityRef entity, long delta) {
        entity.send(new SectorSimulationEvent(delta));
        entity.send(new LoadedSectorUpdateEvent(SectorUtil.getWatchedChunks(entity)
                .stream()
                .filter(chunkProvider::isChunkReady)
                .collect(Collectors.toSet())));
    }

    /**
     * Calculate the time since the last {@link SectorSimulationEvent} was sent. This also updates the
     * {@link SectorSimulationComponent#lastSimulationTime}, so future deltas will be correct.
     * @param entity
     * @return
     */
    private long simulationDelta(EntityRef entity) {
        SectorSimulationComponent simulationComponent = entity.getComponent(SectorSimulationComponent.class);
        long currentTime = time.getGameTimeInMs();
        if (simulationComponent.lastSimulationTime == 0) {
            simulationComponent.lastSimulationTime = currentTime;
        }
        long delta = currentTime - simulationComponent.lastSimulationTime;
        simulationComponent.lastSimulationTime = currentTime;
        return delta;
    }

}
