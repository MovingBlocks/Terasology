/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.world.chunks.remoteChunkProvider;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.ChunkRegionListener;
import org.terasology.world.chunks.GeneratingChunkProvider;
import org.terasology.world.chunks.pipeline.ChunkGenerationPipeline;
import org.terasology.world.chunks.pipeline.ChunkTask;

import java.util.Comparator;
import java.util.Map;

/**
 * @author Immortius
 */
public class RemoteChunkProvider implements GeneratingChunkProvider {

    private static final Logger logger = LoggerFactory.getLogger(RemoteChunkProvider.class);
    private Map<Vector3i, Chunk> chunkCache = Maps.newHashMap();
    private ChunkGenerationPipeline pipeline;

    public RemoteChunkProvider() {
        pipeline = new ChunkGenerationPipeline(this, null, new ChunkTaskRelevanceComparator());
    }

    public void receiveChunk(Chunk chunk) {
        chunkCache.put(chunk.getPos(), chunk);
        pipeline.requestReview(Region3i.createFromCenterExtents(chunk.getPos(), ChunkConstants.LOCAL_REGION_EXTENTS));
    }

    public void invalidateChunks(Vector3i pos) {
        chunkCache.remove(pos);
    }

    @Override
    public void update() {

    }

    @Override
    public boolean isChunkAvailable(Vector3i pos) {
        return chunkCache.containsKey(pos);
    }

    @Override
    public Chunk getChunk(int x, int y, int z) {
        return chunkCache.get(new Vector3i(x, y, z));
    }

    @Override
    public Chunk getChunk(Vector3i chunkPos) {
        return chunkCache.get(chunkPos);
    }

    @Override
    public float size() {
        return chunkCache.size();
    }

    @Override
    public boolean isChunkReady(Vector3i pos) {
        Chunk chunk = getChunk(pos);
        if (chunk == null || chunk.getChunkState() != Chunk.State.COMPLETE) {
            return false;
        }
        for (Vector3i adjPos : Region3i.createFromCenterExtents(pos, ChunkConstants.LOCAL_REGION_EXTENTS)) {
            Chunk adjChunk = getChunk(adjPos);
            if (adjChunk == null || adjChunk.getChunkState() != Chunk.State.COMPLETE) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void setWorldEntity(EntityRef entity) {

    }

    @Override
    public void addRelevanceEntity(EntityRef entity, int distance) {
    }

    @Override
    public void addRelevanceEntity(EntityRef entity, int distance, ChunkRegionListener listener) {
    }

    @Override
    public void updateRelevanceEntity(EntityRef entity, int distance) {
    }

    @Override
    public void removeRelevanceEntity(EntityRef entity) {

    }

    @Override
    public void createOrLoadChunk(Vector3i position) {
    }

    @Override
    public void chunkIsReady(Vector3i position) {
    }

    private class ChunkTaskRelevanceComparator implements Comparator<ChunkTask> {

        private LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);

        @Override
        public int compare(ChunkTask o1, ChunkTask o2) {
            return score(o1.getPosition()) - score(o2.getPosition());
        }

        private int score(Vector3i chunk) {
            Vector3i playerChunk = TeraMath.calcChunkPos(new Vector3i(localPlayer.getPosition(), 0.5f));
            return playerChunk.distanceSquared(chunk);
        }
    }
}
