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

package org.terasology.logic.world;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.world.generator.core.ChunkGeneratorManager;
import org.terasology.logic.world.localChunkProvider.*;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.performanceMonitor.PerformanceMonitor;

import javax.vecmath.Vector3f;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Immortius
 */
public class LocalChunkProvider implements ChunkProvider {
    private static final int CACHE_SIZE = (int) (2 * Runtime.getRuntime().maxMemory() / 1048576);
    private static final int REQUEST_CHUNK_THREADS = 1;
    private static final int CHUNK_PROCESSING_THREADS = 8;
    private static final Vector3i LOCAL_REGION_EXTENTS = new Vector3i(1, 0, 1);

    private Logger logger = Logger.getLogger(getClass().getName());
    private ChunkStore farStore;

    private BlockingQueue<ChunkTask> chunkTasksQueue;
    private BlockingQueue<ChunkRequest> reviewChunkQueue;
    private ExecutorService reviewThreads;
    private ExecutorService chunkProcessingThreads;
    private ChunkGeneratorManager generator;

    private Set<CacheRegion> regions = Sets.newHashSet();

    private ConcurrentMap<Vector3i, Chunk> nearCache = Maps.newConcurrentMap();

    public LocalChunkProvider(ChunkStore farStore, ChunkGeneratorManager generator) {
        this.farStore = farStore;
        this.generator = generator;


        reviewChunkQueue = new PriorityBlockingQueue<ChunkRequest>(32);
        reviewThreads = Executors.newFixedThreadPool(REQUEST_CHUNK_THREADS);
        for (int i = 0; i < REQUEST_CHUNK_THREADS; ++i) {
            reviewThreads.execute(new Runnable() {
                @Override
                public void run() {
                    boolean running = true;
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    while (running) {

                        try {
                            ChunkRequest request = reviewChunkQueue.take();
                            switch (request.getType()) {
                                case REVIEW:
                                    for (Vector3i pos : request.getRegion()) {
                                        checkState(pos);
                                    }
                                    break;
                                case PRODUCE:
                                    for (Vector3i pos : request.getRegion()) {
                                        checkOrCreateChunk(pos);
                                    }
                                    break;
                                case EXIT:
                                    running = false;
                                    break;
                            }
                        } catch (InterruptedException e) {
                            logger.log(Level.SEVERE, "Thread interrupted", e);
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Error in thread", e);
                        }
                    }
                    logger.log(Level.INFO, "Thread shutdown safely");
                }
            });
        }

        chunkTasksQueue = new PriorityBlockingQueue<ChunkTask>(128, new ChunkTaskRelevanceComparator());
        chunkProcessingThreads = Executors.newFixedThreadPool(CHUNK_PROCESSING_THREADS);
        for (int i = 0; i < CHUNK_PROCESSING_THREADS; ++i) {
            chunkProcessingThreads.submit(new Runnable() {
                @Override
                public void run() {
                    boolean running = true;
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    while (running) {
                        try {
                            ChunkTask request = chunkTasksQueue.take();
                            if (request.isShutdownRequest()) {
                                running = false;
                                break;
                            }
                            request.enact();
                        } catch (InterruptedException e) {
                            logger.log(Level.SEVERE, "Thread interrupted", e);
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Error in thread", e);
                        }
                    }
                    logger.log(Level.INFO, "Thread shutdown safely");
                }
            });
        }
    }

    @Override
    public void addRegionEntity(EntityRef entity, int distance) {
        CacheRegion region = new CacheRegion(entity, distance);
        regions.remove(region);
        regions.add(region);
        reviewChunkQueue.offer(new ChunkRequest(ChunkRequest.RequestType.PRODUCE, region.getRegion().expand(new Vector3i(2, 0, 2))));
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
                reviewChunkQueue.offer(new ChunkRequest(ChunkRequest.RequestType.PRODUCE, cacheRegion.getRegion().expand(new Vector3i(2, 0, 2))));
            }
        }
        PerformanceMonitor.startActivity("Review cache size");
        if (nearCache.size() > CACHE_SIZE) {
            logger.log(Level.INFO, "Compacting cache");
            Iterator<Vector3i> iterator = nearCache.keySet().iterator();
            while (iterator.hasNext()) {
                Vector3i pos = iterator.next();
                boolean keep = false;
                for (CacheRegion region : regions) {
                    if (region.getRegion().expand(new Vector3i(4, 0, 4)).encompasses(pos)) {
                        keep = true;
                        break;
                    }
                }
                if (!keep) {
                    // TODO: need some way to not dispose chunks being edited or processed (or do so safely)
                    Chunk chunk = nearCache.get(pos);
                    if (chunk.isLocked()) {
                        continue;
                    }
                    chunk.lock();
                    try {
                        farStore.put(chunk);
                        iterator.remove();
                        chunk.dispose();
                    } finally {
                        chunk.unlock();
                    }
                }

            }
        }
        PerformanceMonitor.endActivity();
    }

    @Override
    public boolean isChunkAvailable(Vector3i pos) {
        return nearCache.containsKey(pos);
    }

    @Override
    public Chunk getChunk(int x, int y, int z) {
        return getChunk(new Vector3i(x, y, z));
    }

    @Override
    public Chunk getChunk(Vector3i pos) {
        return nearCache.get(pos);
    }

    @Override
    public void dispose() {
        for (int i = 0; i < REQUEST_CHUNK_THREADS; ++i) {
            reviewChunkQueue.offer(new ChunkRequest(ChunkRequest.RequestType.EXIT, Region3i.EMPTY));
        }
        for (int i = 0; i < CHUNK_PROCESSING_THREADS; ++i) {
            chunkTasksQueue.offer(new ShutdownTask());
        }
        reviewThreads.shutdown();
        chunkProcessingThreads.shutdown();
        try {
            if (!reviewThreads.awaitTermination(1, TimeUnit.SECONDS)) {
                logger.log(Level.WARNING, "Timed out awaiting chunk review thread termination");
            }
            if (!chunkProcessingThreads.awaitTermination(1, TimeUnit.SECONDS)) {
                logger.log(Level.WARNING, "Timed out awaiting chunk processing thread termination");
            }
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted awaiting chunk thread termination");
        }

        for (Chunk chunk : nearCache.values()) {
            farStore.put(chunk);
            chunk.dispose();
        }
        nearCache.clear();
    }

    @Override
    public float size() {
        return farStore.size();
    }

    private void checkOrCreateChunk(Vector3i chunkPos) {
        Chunk chunk = getChunk(chunkPos);
        if (chunk == null) {
            PerformanceMonitor.startActivity("Check chunk in cache");
            if (farStore.contains(chunkPos)) {
                chunkTasksQueue.offer(new AbstractChunkTask(chunkPos, this) {
                    @Override
                    public void enact() {
                        Chunk chunk = farStore.get(getPosition());
                        if (nearCache.putIfAbsent(getPosition(), chunk) == null) {
                            reviewChunkQueue.offer(new ChunkRequest(ChunkRequest.RequestType.REVIEW, Region3i.createFromCenterExtents(getPosition(), LOCAL_REGION_EXTENTS)));
                        }
                    }
                });
            } else {
                chunkTasksQueue.offer(new AbstractChunkTask(chunkPos, this) {
                    @Override
                    public void enact() {
                        Chunk chunk = generator.generateChunk(getPosition());
                        if (null == nearCache.putIfAbsent(getPosition(), chunk)) {
                            reviewChunkQueue.offer(new ChunkRequest(ChunkRequest.RequestType.REVIEW, Region3i.createFromCenterExtents(getPosition(), LOCAL_REGION_EXTENTS)));
                        }
                    }
                });
            }
            PerformanceMonitor.endActivity();
        } else {
            checkState(chunk);
        }
    }

    private void checkState(Vector3i pos) {
        Chunk chunk = getChunk(pos);
        if (chunk != null) {
            checkState(chunk);
        }
    }

    private void checkState(Chunk chunk) {
        switch (chunk.getChunkState()) {
            case ADJACENCY_GENERATION_PENDING:
                checkReadyForSecondPass(chunk.getPos());
                break;
            case INTERNAL_LIGHT_GENERATION_PENDING:
                checkReadyToDoInternalLighting(chunk.getPos());
                break;
            case LIGHT_PROPAGATION_PENDING:
                checkReadyToPropagateLighting(chunk.getPos());
                break;
            case FULL_LIGHT_CONNECTIVITY_PENDING:
                checkComplete(chunk.getPos());
                break;
            default:
                break;
        }
    }

    private void checkReadyForSecondPass(Vector3i pos) {
        Chunk chunk = getChunk(pos);
        if (chunk != null && chunk.getChunkState() == Chunk.State.ADJACENCY_GENERATION_PENDING) {
            for (Vector3i adjPos : Region3i.createFromCenterExtents(pos, LOCAL_REGION_EXTENTS)) {
                if (!adjPos.equals(pos)) {
                    Chunk adjChunk = getChunk(adjPos);
                    if (adjChunk == null) {
                        return;
                    }
                }
            }
            logger.log(Level.FINE, "Queueing for adjacency generation " + pos);
            chunkTasksQueue.offer(new AbstractChunkTask(pos, this) {
                @Override
                public void enact() {
                    WorldView view = WorldView.createLocalView(getPosition(), getProvider());
                    if (view == null) {
                        return;
                    }
                    view.lock();
                    try {
                        if (!view.isValidView()) {
                            return;
                        }
                        Chunk chunk = getProvider().getChunk(getPosition());
                        if (chunk.getChunkState() != Chunk.State.ADJACENCY_GENERATION_PENDING) {
                            return;
                        }

                        generator.secondPassChunk(getPosition(), view);
                        chunk.setChunkState(Chunk.State.INTERNAL_LIGHT_GENERATION_PENDING);
                        reviewChunkQueue.offer(new ChunkRequest(ChunkRequest.RequestType.REVIEW, Region3i.createFromCenterExtents(getPosition(), LOCAL_REGION_EXTENTS)));
                    } finally {
                        view.unlock();
                    }
                }
            });
        }
    }

    private void checkReadyToDoInternalLighting(Vector3i pos) {
        Chunk chunk = getChunk(pos);
        if (chunk != null && chunk.getChunkState() == Chunk.State.INTERNAL_LIGHT_GENERATION_PENDING) {
            for (Vector3i adjPos : Region3i.createFromCenterExtents(pos, LOCAL_REGION_EXTENTS)) {
                if (!adjPos.equals(pos)) {
                    Chunk adjChunk = getChunk(adjPos);
                    if (adjChunk == null || adjChunk.getChunkState().compareTo(Chunk.State.INTERNAL_LIGHT_GENERATION_PENDING) < 0) {
                        return;
                    }
                }
            }
            logger.log(Level.FINE, "Queueing for internal light generation " + pos);
            chunkTasksQueue.offer(new AbstractChunkTask(pos, this) {
                @Override
                public void enact() {
                    Chunk chunk = getProvider().getChunk(getPosition());
                    if (chunk == null) {
                        return;
                    }

                    chunk.lock();
                    try {
                        if (chunk.isDisposed() || chunk.getChunkState() != Chunk.State.INTERNAL_LIGHT_GENERATION_PENDING) {
                            return;
                        }
                        InternalLightProcessor.generateInternalLighting(chunk);
                        chunk.setChunkState(Chunk.State.LIGHT_PROPAGATION_PENDING);
                        reviewChunkQueue.offer(new ChunkRequest(ChunkRequest.RequestType.REVIEW, Region3i.createFromCenterExtents(getPosition(), LOCAL_REGION_EXTENTS)));
                    } finally {
                        chunk.unlock();
                    }
                }
            });
        }
    }

    private void checkReadyToPropagateLighting(Vector3i pos) {
        Chunk chunk = getChunk(pos);
        if (chunk != null && chunk.getChunkState() == Chunk.State.LIGHT_PROPAGATION_PENDING) {
            for (Vector3i adjPos : Region3i.createFromCenterExtents(pos, LOCAL_REGION_EXTENTS)) {
                if (!adjPos.equals(pos)) {
                    Chunk adjChunk = getChunk(adjPos);
                    if (adjChunk == null || adjChunk.getChunkState().compareTo(Chunk.State.LIGHT_PROPAGATION_PENDING) < 0) {
                        return;
                    }
                }
            }
            logger.log(Level.FINE, "Queueing for light propagation pass " + pos);
            chunkTasksQueue.offer(new AbstractChunkTask(pos, this) {
                @Override
                public void enact() {
                    WorldView worldView = WorldView.createLocalView(getPosition(), getProvider());
                    if (worldView == null) {
                        return;
                    }
                    worldView.lock();
                    try {
                        if (!worldView.isValidView()) {
                            return;
                        }
                        Chunk chunk = getProvider().getChunk(getPosition());
                        if (chunk.getChunkState() != Chunk.State.LIGHT_PROPAGATION_PENDING) {
                            return;
                        }

                        new LightPropagator(worldView).propagateOutOfTargetChunk();
                        chunk.setChunkState(Chunk.State.FULL_LIGHT_CONNECTIVITY_PENDING);
                        reviewChunkQueue.offer(new ChunkRequest(ChunkRequest.RequestType.REVIEW, Region3i.createFromCenterExtents(getPosition(), LOCAL_REGION_EXTENTS)));
                    } finally {
                        worldView.unlock();
                    }
                }
            });
        }
    }

    private void checkComplete(Vector3i pos) {
        Chunk chunk = getChunk(pos);
        if (chunk != null && chunk.getChunkState() == Chunk.State.FULL_LIGHT_CONNECTIVITY_PENDING) {
            for (Vector3i adjPos : Region3i.createFromCenterExtents(pos, LOCAL_REGION_EXTENTS)) {
                if (!adjPos.equals(pos)) {
                    Chunk adjChunk = getChunk(adjPos);
                    if (adjChunk == null || adjChunk.getChunkState().compareTo(Chunk.State.FULL_LIGHT_CONNECTIVITY_PENDING) < 0) {
                        return;
                    }
                }
            }
            logger.log(Level.FINE, "Now complete " + pos);
            chunk.setChunkState(Chunk.State.COMPLETE);
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
                return Region3i.createFromCenterExtents(worldToChunkPos(loc.getWorldPosition()), new Vector3i(distance / 2, 0, distance / 2));
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
            worldPos.x /= Chunk.SIZE_X;
            worldPos.y = 0;
            worldPos.z /= Chunk.SIZE_Z;
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

    private class ChunkTaskRelevanceComparator implements Comparator<ChunkTask> {

        @Override
        public int compare(ChunkTask o1, ChunkTask o2) {
            return score(o1.getPosition()) - score(o2.getPosition());
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
