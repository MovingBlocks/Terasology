/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.world.chunks.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.AdvancedConfig;
import org.terasology.game.CoreRegistry;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.utilities.concurrency.Task;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.GeneratingChunkProvider;

/**
 * @author Immortius
 */
public class ChunkRequest implements Task, Comparable<ChunkRequest> {

    private static final Logger logger = LoggerFactory.getLogger(ChunkRequest.class);

    public enum Type {
        /**
         * If available, check whether the chunks can be further generated
         */
        REVIEW,
        /**
         * Retrieve the chunks from the chunk store or generate them if missing
         */
        PRODUCE,
        /**
         * End the processing of chunks
         */
        EXIT;
    }

    private ChunkGenerationPipeline pipeline;
    private GeneratingChunkProvider provider;
    private Type type;
    private Region3i region;

    public ChunkRequest(ChunkGenerationPipeline pipeline, GeneratingChunkProvider provider, Type type, Region3i region) {
        this.pipeline = pipeline;
        this.provider = provider;
        this.type = type;
        this.region = region;
    }

    public Type getType() {
        return type;
    }

    public Region3i getRegion() {
        return region;
    }


    @Override
    public void enact() {
        switch (type) {
            case REVIEW:
                for (Vector3i pos : region) {
                    checkState(pos);
                }
                break;
            case PRODUCE:
                for (Vector3i pos : region) {
                    checkOrCreateChunk(pos);
                }
                break;
        }
    }

    @Override
    public boolean isTerminateSignal() {
        return type == Type.EXIT;
    }

    @Override
    public int compareTo(ChunkRequest o) {
        return type.compareTo(o.type);
    }

    private void checkState(Vector3i pos) {
        Chunk chunk = provider.getChunk(pos);
        if (chunk != null) {
            checkState(chunk);
        }
    }

    private void checkState(Chunk chunk) {
        switch (chunk.getChunkState()) {
            case ADJACENCY_GENERATION_PENDING:
                checkReadyForSecondPass(chunk);
                break;
            case INTERNAL_LIGHT_GENERATION_PENDING:
                checkReadyToDoInternalLighting(chunk);
                break;
            case LIGHT_PROPAGATION_PENDING:
                checkReadyToPropagateLighting(chunk);
                break;
            case FULL_LIGHT_CONNECTIVITY_PENDING:
                checkComplete(chunk);
                break;
            default:
                break;
        }
    }

    private void checkOrCreateChunk(Vector3i chunkPos) {
        Chunk chunk = provider.getChunk(chunkPos);
        if (chunk == null) {
            provider.createOrLoadChunk(chunkPos);
        } else {
            checkState(chunk);
        }
    }

    private void checkReadyForSecondPass(Chunk chunk) {
        Vector3i pos = chunk.getPos();
        if (chunk != null && chunk.getChunkState() == Chunk.State.ADJACENCY_GENERATION_PENDING) {
            for (Vector3i adjPos : Region3i.createFromCenterExtents(pos, ChunkConstants.LOCAL_REGION_EXTENTS)) {
                if (!adjPos.equals(pos)) {
                    Chunk adjChunk = provider.getChunk(adjPos);
                    if (adjChunk == null) {
                        return;
                    }
                }
            }
            logger.debug("Queueing for adjacency generation {}", pos);
            pipeline.doTask(new SecondPassChunkTask(pipeline, pos, provider));
        }
    }

    private void checkReadyToDoInternalLighting(Chunk chunk) {
        Vector3i pos = chunk.getPos();
        if (chunk != null && chunk.getChunkState() == Chunk.State.INTERNAL_LIGHT_GENERATION_PENDING) {
            for (Vector3i adjPos : Region3i.createFromCenterExtents(pos, ChunkConstants.LOCAL_REGION_EXTENTS)) {
                if (!adjPos.equals(pos)) {
                    Chunk adjChunk = provider.getChunk(adjPos);
                    if (adjChunk == null || adjChunk.getChunkState().compareTo(Chunk.State.INTERNAL_LIGHT_GENERATION_PENDING) < 0) {
                        return;
                    }
                }
            }
            logger.debug("Queueing for internal light generation {}", pos);
            pipeline.doTask(new InternalLightingChunkTask(pipeline, pos, provider));
        }
    }

    private void checkReadyToPropagateLighting(Chunk chunk) {
        Vector3i pos = chunk.getPos();
        if (chunk != null && chunk.getChunkState() == Chunk.State.LIGHT_PROPAGATION_PENDING) {
            for (Vector3i adjPos : Region3i.createFromCenterExtents(pos, ChunkConstants.LOCAL_REGION_EXTENTS)) {
                if (!adjPos.equals(pos)) {
                    Chunk adjChunk = provider.getChunk(adjPos);
                    if (adjChunk == null || adjChunk.getChunkState().compareTo(Chunk.State.LIGHT_PROPAGATION_PENDING) < 0) {
                        return;
                    }
                }
            }
            logger.debug("Queueing for light propagation pass {}", pos);

            pipeline.doTask(new PropagateLightingChunkTask(pipeline, pos, provider));
        }
    }

    private void checkComplete(Chunk chunk) {
        Vector3i pos = chunk.getPos();
        if (chunk != null && chunk.getChunkState() == Chunk.State.FULL_LIGHT_CONNECTIVITY_PENDING) {
            for (Vector3i adjPos : Region3i.createFromCenterExtents(pos, ChunkConstants.LOCAL_REGION_EXTENTS)) {
                if (!adjPos.equals(pos)) {
                    Chunk adjChunk = provider.getChunk(adjPos);
                    if (adjChunk == null || adjChunk.getChunkState().compareTo(Chunk.State.FULL_LIGHT_CONNECTIVITY_PENDING) < 0) {
                        return;
                    }
                }
            }
            logger.debug("Now complete {}", pos);
            chunk.setChunkState(Chunk.State.COMPLETE);
            AdvancedConfig config = CoreRegistry.get(org.terasology.config.Config.class).getAdvanced();
            if (config.isChunkDeflationEnabled()) {
                pipeline.doTask(new DeflateChunkTask(pipeline, pos, provider));
            }
            for (Vector3i adjPos : Region3i.createFromCenterExtents(pos, ChunkConstants.LOCAL_REGION_EXTENTS)) {
                if (provider.isChunkReady(adjPos)) {
                    provider.chunkIsReady(pos);
                }
            }
        }
    }

}
