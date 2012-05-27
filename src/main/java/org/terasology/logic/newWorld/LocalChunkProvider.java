/*
 * Copyright 2012
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

package org.terasology.logic.newWorld;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.components.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.newWorld.generationPhase.*;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;

import javax.vecmath.Vector3f;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Immortius
 */
public class LocalChunkProvider implements NewChunkProvider
{
    private static final int CACHE_SIZE = (int) (2 * Runtime.getRuntime().maxMemory() / 1048576);

    private Logger logger = Logger.getLogger(getClass().getName());
    private NewChunkCache farCache;

    private ChunkPhase generatePhase;
    private ChunkPhase secondPassPhase;
    private ChunkPhase internalLightingPhase;
    private ChunkPhase propagateLightPhase;
    private Set<CacheRegion> regions = Sets.newHashSet();

    private ConcurrentMap<Vector3i, NewChunk> nearCache = Maps.newConcurrentMap();
    private NewChunkGeneratorManager generator;

    public LocalChunkProvider(NewChunkCache farCache, NewChunkGeneratorManager generator) {
        this.farCache = farCache;
        this.generator = generator;
        Comparator<Vector3i> chunkRelevanceComparator = new ChunkRelevanceComparator();
        generatePhase = new CreateChunkPhase(4, chunkRelevanceComparator, generator, nearCache);
        secondPassPhase = new SecondPassPhase(1, chunkRelevanceComparator, generator, this);
        internalLightingPhase = new InternalLightingPhase(2, chunkRelevanceComparator, this);
        propagateLightPhase = new PropagateLightingPhase(1, chunkRelevanceComparator, generator, this);
    }

    @Override
    public void addRegionEntity(EntityRef entity, int distance) {
        CacheRegion region = new CacheRegion(entity, distance);
        regions.add(region);
        checkForMissingChunks(region);
    }

    @Override
    public void removeRegionEntity(EntityRef entity) {
        regions.remove(new CacheRegion(entity, 0));
    }

    @Override
    public void update() {
        for (CacheRegion cacheRegion : regions) {
            cacheRegion.update();
            if (cacheRegion.isDirty()) {
                cacheRegion.setUpToDate();
                checkForMissingChunks(cacheRegion);
            }
        }
        while (generatePhase.isResultAvailable()) {
            Vector3i chunkPos = generatePhase.poll();
            logger.log(Level.FINE, "Received generated chunk " + chunkPos);
            for (Vector3i pos : Region3i.createFromCenterExtents(chunkPos, new Vector3i(1,0,1))) {
                checkReadyForSecondPass(pos);
            }
        }
        while (secondPassPhase.isResultAvailable()) {
            Vector3i chunkPos = secondPassPhase.poll();
            logger.log(Level.FINE, "Received second passed chunk " + chunkPos);
            for (Vector3i pos : Region3i.createFromCenterExtents(chunkPos, new Vector3i(1,0,1))) {
                checkReadyToDoInternalLighting(pos);
            }
        }
        while (internalLightingPhase.isResultAvailable()) {
            Vector3i chunkPos = internalLightingPhase.poll();
            logger.log(Level.FINE, "Received internally lit chunk " + chunkPos);
            for (Vector3i pos : Region3i.createFromCenterExtents(chunkPos, new Vector3i(1,0,1))) {
                checkReadyToPropagateLighting(pos);
            }
        }
        while (propagateLightPhase.isResultAvailable()) {
            Vector3i chunkPos = propagateLightPhase.poll();
            logger.log(Level.FINE, "Received second passed chunk " + chunkPos);
            for (Vector3i pos : Region3i.createFromCenterExtents(chunkPos, new Vector3i(1,0,1))) {
                checkComplete(pos);
            }
        }
        if (nearCache.size() > CACHE_SIZE) {
            logger.log(Level.INFO, "Compacting cache");
            Iterator<Vector3i> iterator = nearCache.keySet().iterator();
            while (iterator.hasNext()) {
                Vector3i pos = iterator.next();
                boolean keep = false;
                for (CacheRegion region : regions) {
                    if (region.getRegion().encompasses(pos)) {
                        keep = true;
                        break;
                    }
                }
                if (!keep) {
                    // TODO: need some way to not dispose chunks being edited or generated
                    NewChunk chunk = nearCache.get(pos);
                    farCache.put(chunk);
                    iterator.remove();
                    chunk.dispose();
                }

            }
        }

    }

    @Override
    public boolean isChunkAvailable(Vector3i pos) {
        return nearCache.containsKey(pos);
    }

    @Override
    public NewChunk getChunk(int x, int y, int z) {
        return getChunk(new Vector3i(x, y, z));
    }

    @Override
    public NewChunk getChunk(Vector3i pos) {
        NewChunk c = nearCache.get(pos);
        if (c == null) {
            c = farFetch(pos);
        }
        return c;
    }

    private NewChunk farFetch(Vector3i pos) {
        NewChunk c = farCache.get(pos);
        if (c != null) {
            return nearCache.putIfAbsent(pos, c);
        }
        return null;
    }

    @Override
    public void dispose() {
        generatePhase.dispose();
        secondPassPhase.dispose();
        internalLightingPhase.dispose();
        propagateLightPhase.dispose();
        for (NewChunk chunk : nearCache.values()) {
            farCache.put(chunk);
            chunk.dispose();
        }
        nearCache.clear();
    }

    @Override
    public float size() {
        return farCache.size();
    }

    /**
     * Checks for uncreated chunks in the region, fetching from far cache as necessary.
     * @param cacheRegion
     */
    private void checkForMissingChunks(CacheRegion cacheRegion) {
        for (final Vector3i chunkPos : cacheRegion.getRegion().expand(new Vector3i(4,0,4))) {
            NewChunk chunk = getChunk(chunkPos);
            //TODO: Add checks for incomplete chunks too
            if (chunk == null && !generatePhase.processing(chunkPos)) {
                logger.log(Level.FINE, "Generating Chunk " + chunkPos);
                generatePhase.queue(chunkPos);
            }
        }
    }

    private void checkReadyForSecondPass(Vector3i pos) {
        NewChunk chunk = getChunk(pos);
        if (chunk != null && chunk.getChunkState() == NewChunk.State.AdjacencyGenerationPending && !secondPassPhase.processing(pos)) {
            for (Vector3i adjPos : Region3i.createFromCenterExtents(pos, new Vector3i(1,0,1))) {
                if (!adjPos.equals(pos)) {
                    NewChunk adjChunk = getChunk(adjPos);
                    if (adjChunk == null) {
                        return;
                    }
                }
            }
            logger.log(Level.FINE, "Queueing for adjacency generation " + pos);
            secondPassPhase.queue(pos);
        }
    }

    private void checkReadyToDoInternalLighting(Vector3i pos) {
        NewChunk chunk = getChunk(pos);
        if (chunk != null && chunk.getChunkState() == NewChunk.State.InternalLightGenerationPending && !internalLightingPhase.processing(pos)) {
            for (Vector3i adjPos : Region3i.createFromCenterExtents(pos, new Vector3i(1,0,1))) {
                if (!adjPos.equals(pos)) {
                    NewChunk adjChunk = getChunk(adjPos);
                    if (adjChunk == null || adjChunk.getChunkState().compareTo(NewChunk.State.InternalLightGenerationPending) < 0)  {
                        return;
                    }
                }
            }
            logger.log(Level.FINE, "Queueing for adjacency generation " + pos);
            internalLightingPhase.queue(pos);
        }
    }

    private void checkReadyToPropagateLighting(Vector3i pos) {
        NewChunk chunk = getChunk(pos);
        if (chunk != null && chunk.getChunkState() == NewChunk.State.LightPropagationPending && !propagateLightPhase.processing(pos)) {
            for (Vector3i adjPos : Region3i.createFromCenterExtents(pos, new Vector3i(1,0,1))) {
                if (!adjPos.equals(pos)) {
                    NewChunk adjChunk = getChunk(adjPos);
                    if (adjChunk == null || adjChunk.getChunkState().compareTo(NewChunk.State.LightPropagationPending) < 0) {
                        return;
                    }
                }
            }
            logger.log(Level.FINE, "Queueing for second pass " + pos);
            propagateLightPhase.queue(pos);
        }
    }

    private void checkComplete(Vector3i pos) {
        NewChunk chunk = getChunk(pos);
        if (chunk != null && chunk.getChunkState() == NewChunk.State.FullLightConnectivityPending) {
            for (Vector3i adjPos : Region3i.createFromCenterExtents(pos, new Vector3i(1,0,1))) {
                if (!adjPos.equals(pos)) {
                    NewChunk adjChunk = getChunk(adjPos);
                    if (adjChunk == null || adjChunk.getChunkState().compareTo(NewChunk.State.FullLightConnectivityPending) < 0) {
                        return;
                    }
                }
            }
            logger.log(Level.FINE, "Now complete " + pos);
            chunk.setChunkState(NewChunk.State.Complete);
            // TODO: Send event out

        }
    }

    private static class CacheRegion {
        private EntityRef entity;
        private int distance;
        private boolean dirty;
        private Vector3i center = new Vector3i();

        public CacheRegion(EntityRef entity, int distance) {
            this.entity = entity;
            this.distance = distance;

            LocationComponent loc = entity.getComponent(LocationComponent.class);
            if (loc == null) {
                dirty = false;
            } else {
                center.set(worldToChunkPos(loc.getWorldPosition()));
                dirty = true;
            }
        }

        public boolean isValid() {
            return entity.hasComponent(LocationComponent.class);
        }

        public boolean isDirty() {
            return dirty;
        }

        public void setUpToDate() {
            dirty = false;
        }

        public void update() {
            if (!isValid()) {
                dirty = false;
            } else {
                Vector3i newCenter = getCenter();
                if (!newCenter.equals(center)) {
                    dirty = true;
                    center.set(newCenter);
                }
            }
        }

        public Region3i getRegion() {
            LocationComponent loc = entity.getComponent(LocationComponent.class);
            if (loc != null) {
                return Region3i.createFromCenterExtents(worldToChunkPos(loc.getWorldPosition()), new Vector3i(distance/2,0,distance/2));
            }
            return Region3i.EMPTY;
        }

        private Vector3i getCenter() {
            LocationComponent loc = entity.getComponent(LocationComponent.class);
            if (loc != null) {
                return worldToChunkPos(loc.getWorldPosition());
            }
            return new Vector3i();
        }

        private Vector3i worldToChunkPos(Vector3f worldPos) {
            worldPos.x /= NewChunk.SIZE_X;
            worldPos.y = 0;
            worldPos.z /= NewChunk.SIZE_Z;
            return new Vector3i(worldPos);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o instanceof CacheRegion) {
                CacheRegion other = (CacheRegion) o;
                return Objects.equal(other.entity, entity);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(entity);
        }
    }

    private class ChunkRelevanceComparator implements Comparator<Vector3i> {

        @Override
        public int compare(Vector3i o1, Vector3i o2) {
            return score(o1) - score(o2);
        }

        private int score(Vector3i chunk) {
            int score = Integer.MAX_VALUE;
            for (CacheRegion region : regions) {
                int dist = distFromRegion(chunk, region.center);
                if (dist < score) {
                    score = dist;
                }
            }
            return score;
        }

        private int distFromRegion(Vector3i pos, Vector3i regionCenter) {
            return pos.gridDistance(regionCenter);
        }
    }
}
