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

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.module.sandbox.API;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.Chunks;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A utility class for the {@link SectorSimulationSystem} and related components and events.
 */
@API
public final class SectorUtil {

    private SectorUtil() {
    }

    /**
     * Create a new {@link SectorRegionComponent} with the given chunks, from a collection of chunk positions.
     *
     * @param chunks the positions of the chunks to add
     * @return the newly created SectorRegionComponent
     */
    public static SectorRegionComponent createSectorRegionComponent(Collection<Vector3i> chunks) {
        SectorRegionComponent regionComponent = new SectorRegionComponent();
        regionComponent.chunks.addAll(chunks);
        return regionComponent;
    }

    /**
     * Add the given collection of chunks to the given component, converting the chunks to their positions.
     *
     * @param regionComponent the component to add the chunks to
     * @param chunks the chunks to add
     */
    public static void addChunksToRegionComponent(SectorRegionComponent regionComponent, Collection<Chunk> chunks) {
        regionComponent.chunks.addAll(chunks.stream()
                .map(k -> k.getPosition(new Vector3i()))
                .collect(Collectors.toSet()));
    }

    /**
     * Add the given collection of chunk positions to the {@link SectorRegionComponent} of the entity, creating it if
     * needed.
     *
     * @param entity the entity to which the chunks will be added
     * @param chunks the positions of the chunks to add
     */
    public static void addChunksToRegionComponent(EntityRef entity, Collection<Vector3i> chunks) {
        SectorRegionComponent regionComponent = entity.getComponent(SectorRegionComponent.class);
        if (regionComponent == null) {
            regionComponent = new SectorRegionComponent();
        }
        regionComponent.chunks.addAll(chunks);
        entity.addOrSaveComponent(regionComponent);
    }

    /**
     * Watched chunks are defined as the union of:
     * <ul>
     *     <li>The chunk in which the {@link LocationComponent#getWorldPosition()} resides, if any</li>
     *     <li>The set of chunks in {@link SectorRegionComponent#chunks}, if any</li>
     * </ul>
     *
     * @param entity the entity to query the watched chunks of
     * @return the set of positions of this entity's watched chunks
     */
    public static Set<Vector3i> getWatchedChunks(EntityRef entity) {
        Set<Vector3i> chunks = new HashSet<>();
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        Vector3f position = loc.getWorldPosition(new Vector3f());
        if (position.isFinite()) {
            chunks.add(Chunks.toChunkPos(position, new Vector3i()));
        }
        SectorRegionComponent regionComponent = entity.getComponent(SectorRegionComponent.class);
        if (regionComponent != null) {
            chunks.addAll(regionComponent.chunks); // potential leaky abstraction. component exposes its internal vectors
        }
        return chunks;
    }

    /**
     * Calculate whether the entity is watching no loaded chunks, or only the given chunk is loaded.
     *
     * @param entity the sector-scope entity to query
     * @param chunkPos the position of the chunk to check
     * @param chunkProvider the chunkProvider, so that it can check which chunks are loaded
     * @return whether the entity is watching no loaded chunks, or only the given chunk is loaded
     */
    public static boolean onlyWatchedChunk(EntityRef entity, Vector3ic chunkPos, ChunkProvider chunkProvider) {
        Set<Vector3i> readyChunks = getWatchedChunks(entity).stream()
                .filter(chunkProvider::isChunkReady)
                .collect(Collectors.toSet());
        return readyChunks.isEmpty() || (readyChunks.size() == 1 && readyChunks.contains(chunkPos));
    }

}
