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
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;

import javax.vecmath.Vector3f;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * @author Immortius
 */
public class LocalChunkProvider implements NewChunkProvider
{
    private Logger logger = Logger.getLogger(getClass().getName());
    protected NewChunkCache farCache;
    AtomicBoolean building = new AtomicBoolean(false);

    private Set<CacheRegion> regions = Sets.newHashSet();

    private ConcurrentMap<Vector3i, NewChunk> nearCache = Maps.newConcurrentMap();
    private NewChunkGeneratorManager generator;

    public LocalChunkProvider(NewChunkCache farCache, NewChunkGeneratorManager generator) {
        this.farCache = farCache;
        this.generator = generator;
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
                checkRegion(cacheRegion);
            }
        }
    }

    /**
     * Ensure that all the chunks within the region are fully available and fully loaded
     * @param cacheRegion
     */
    private void checkRegion(CacheRegion cacheRegion) {
        // TODO: Background thread these operations
        for (Vector3i chunkPos : cacheRegion.getRegion()) {
            NewChunk chunk = getChunk(chunkPos);
            if (chunk == null) {
                chunk = createChunk(chunkPos);
                // TODO: Further generation
            }
        }
    }

    private NewChunk createChunk(Vector3i pos) {
        NewChunk newChunk = generator.generateChunk(pos);
        nearCache.putIfAbsent(pos, newChunk);
        return newChunk;
    }

    @Override
    public boolean isChunkAvailable(Vector3i pos) {
        return nearCache.containsKey(pos);
    }

    @Override
    public NewChunk getChunk(int x, int y, int z) {
        return getChunk(new Vector3i(x,y,z));
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
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public float size() {
        return farCache.size();
    }

    @Override
    public boolean isBuildingChunks() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
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
                return Region3i.createFromCenterExtents(worldToChunkPos(loc.getWorldPosition()), new Vector3i(distance,distance,distance));
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
            worldPos.y /= NewChunk.CHUNK_DIMENSION_Y;
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
}
