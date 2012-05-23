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
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.terasology.components.LocationComponent;
import org.terasology.entitySystem.EntityRef;
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
    private static final int NUM_GENERATOR_THREADS = 2;
    private static final int CACHE_SIZE = (int) Runtime.getRuntime().maxMemory() / 1048576;

    private Logger logger = Logger.getLogger(getClass().getName());
    private NewChunkCache farCache;

    private Set<Vector3i> generatingChunks = Sets.newHashSet();
    private BlockingQueue<Vector3i> generateQueue = new PriorityBlockingQueue<Vector3i>(128, new ChunkRelevanceComparator());
    private ConcurrentLinkedQueue<Vector3i> generatedChunks = Queues.newConcurrentLinkedQueue();
    private ExecutorService generatorThreadPool = Executors.newFixedThreadPool(NUM_GENERATOR_THREADS);

    private Set<CacheRegion> regions = Sets.newHashSet();

    private ConcurrentMap<Vector3i, NewChunk> nearCache = Maps.newConcurrentMap();
    private NewChunkGeneratorManager generator;

    public LocalChunkProvider(NewChunkCache farCache, NewChunkGeneratorManager generator) {
        this.farCache = farCache;
        this.generator = generator;
        for (int i = 0; i < NUM_GENERATOR_THREADS; ++i) {
            generatorThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    boolean running = true;
                    while (running) {
                        try {
                            Vector3i pos = generateQueue.take();
                            createChunk(pos);
                        }
                        catch (InterruptedException e) {
                            logger.log(Level.INFO, "Generator Thread ending");
                            running = false;
                        }
                    }
                }
            });
        }
    }

    @Override
    public void addRegionEntity(EntityRef entity, int distance) {
        CacheRegion region = new CacheRegion(entity, distance);
        regions.add(region);
        checkRegion(region);
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
                checkRegion(cacheRegion);
            }
        }
        //TODO: Switch to while loop once light propagation moved to background thread
        if (!generatedChunks.isEmpty()) {
            Vector3i chunkPos = generatedChunks.poll();
            logger.log(Level.FINE, "Received Chunk " + chunkPos);
            generatingChunks.remove(chunkPos);
            for (Vector3i pos : Region3i.createFromCenterExtents(chunkPos, new Vector3i(1,0,1))) {
                checkFullGenerate(pos);
            }
            for (Vector3i pos : Region3i.createFromCenterExtents(chunkPos, new Vector3i(2,0,2))) {
                checkComplete(pos);
            }
        }
        if (nearCache.size() > CACHE_SIZE) {
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
                    // TODO: need some way to not dispose chunks being edited
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
        generatorThreadPool.shutdown();
        try {
            generatorThreadPool.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Interrupted while attempting clean shutdown", e);
        }
        for (NewChunk chunk : nearCache.values()) {
            farCache.put(chunk);
            chunk.dispose();
        }
        nearCache.clear();
        generatedChunks.clear();
        generateQueue.clear();
        generatingChunks.clear();
    }

    @Override
    public float size() {
        return farCache.size();
    }

    /**
     * Checks for uncreated chunks in the region, fetching from far cache as necessary.
     * @param cacheRegion
     */
    private void checkRegion(CacheRegion cacheRegion) {
        for (final Vector3i chunkPos : cacheRegion.getRegion().expand(new Vector3i(2,0,2))) {
            NewChunk chunk = getChunk(chunkPos);
            if (chunk == null && !generatingChunks.contains(chunkPos)) {
                logger.log(Level.FINE, "Generating Chunk " + chunkPos);
                generatingChunks.add(chunkPos);
                if (!generateQueue.offer(chunkPos)) {
                    logger.severe("Failed to queue chunk generation: " + chunkPos);
                }
            }
        }
    }

    private NewChunk createChunk(Vector3i pos) {
        NewChunk newChunk = generator.generateChunk(pos);
        nearCache.putIfAbsent(pos, newChunk);
        generatedChunks.add(pos);
        return newChunk;
    }

    private void checkFullGenerate(Vector3i pos) {
        NewChunk chunk = getChunk(pos);
        if (chunk != null && chunk.getChunkState() == NewChunk.State.Awaiting2ndPass) {
            for (Vector3i adjPos : Region3i.createFromCenterExtents(pos, new Vector3i(1,0,1))) {
                if (!adjPos.equals(pos)) {
                    NewChunk adjChunk = getChunk(adjPos);
                    if (adjChunk == null) {
                        return;
                    }
                }
            }
            logger.log(Level.FINE, "Full generating " + pos);
            fullGenerateChunk(chunk);
        }
    }

    private void fullGenerateChunk(NewChunk chunk) {
        WorldView worldView = WorldView.createLocalView(chunk.getPos(), this);
        LightPropagator propagator = new LightPropagator(worldView);
        propagator.propagateOutOfTargetChunk();
        chunk.setChunkState(NewChunk.State.AwaitingFullLighting);
        generator.postProcess(chunk.getPos());
    }

    private void checkComplete(Vector3i pos) {
        NewChunk chunk = getChunk(pos);
        if (chunk != null && chunk.getChunkState() == NewChunk.State.AwaitingFullLighting) {
            for (Vector3i adjPos : Region3i.createFromCenterExtents(pos, new Vector3i(1,0,1))) {
                if (!adjPos.equals(pos)) {
                    NewChunk adjChunk = getChunk(adjPos);
                    if (adjChunk == null || (adjChunk.getChunkState() == NewChunk.State.Awaiting2ndPass)) {
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
            worldPos.x /= NewChunk.CHUNK_DIMENSION_X;
            worldPos.y = 0;
            worldPos.z /= NewChunk.CHUNK_DIMENSION_Z;
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
