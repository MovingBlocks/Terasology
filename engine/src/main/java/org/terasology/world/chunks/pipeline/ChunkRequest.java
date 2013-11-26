/*
 * Copyright 2013 MovingBlocks
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
import org.terasology.engine.CoreRegistry;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.network.NetworkSystem;
import org.terasology.utilities.concurrency.Task;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.internal.ChunkImpl;
import org.terasology.world.chunks.internal.GeneratingChunkProvider;

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
        EXIT
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
    public String getName() {
        return type.name() + " CHUNK";
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
        ChunkImpl chunk = provider.getChunkForProcessing(pos);
        if (chunk != null) {
            checkState(chunk);
        }
    }

    private void checkState(ChunkImpl chunk) {
        switch (chunk.getChunkState()) {
            case ADJACENCY_GENERATION_PENDING:
                checkReadyForSecondPass(chunk);
                break;
            case INTERNAL_LIGHT_GENERATION_PENDING:
                checkReadyToDoInternalLighting(chunk);
                break;
            default:
                break;
        }
    }

    private void checkOrCreateChunk(Vector3i chunkPos) {
        ChunkImpl chunk = provider.getChunkForProcessing(chunkPos);
        if (chunk == null) {
            provider.createOrLoadChunk(chunkPos);
        } else {
            checkState(chunk);
        }
    }

    private void checkReadyForSecondPass(ChunkImpl chunk) {
        Vector3i pos = chunk.getPos();
        if (chunk.getChunkState() == ChunkImpl.State.ADJACENCY_GENERATION_PENDING) {
            for (Vector3i adjPos : Region3i.createFromCenterExtents(pos, ChunkConstants.LOCAL_REGION_EXTENTS)) {
                if (!adjPos.equals(pos)) {
                    ChunkImpl adjChunk = provider.getChunkForProcessing(adjPos);
                    if (adjChunk == null) {
                        return;
                    }
                }
            }
            logger.debug("Queueing for adjacency generation {}", pos);
            pipeline.doTask(new SecondPassChunkTask(pipeline, pos, provider));
        }
    }

    private void checkReadyToDoInternalLighting(ChunkImpl chunk) {
        Vector3i pos = chunk.getPos();
        if (chunk.getChunkState() == ChunkImpl.State.INTERNAL_LIGHT_GENERATION_PENDING) {
            if (CoreRegistry.get(NetworkSystem.class).getMode().isAuthority()) {
                for (Vector3i adjPos : Region3i.createFromCenterExtents(pos, ChunkConstants.LOCAL_REGION_EXTENTS)) {
                    if (!adjPos.equals(pos)) {
                        ChunkImpl adjChunk = provider.getChunkForProcessing(adjPos);
                        if (adjChunk == null || adjChunk.getChunkState().compareTo(ChunkImpl.State.INTERNAL_LIGHT_GENERATION_PENDING) < 0) {
                            return;
                        }
                    }
                }
            }
            logger.debug("Queueing for internal light generation {}", pos);
            pipeline.doTask(new InternalLightingChunkTask(pipeline, pos, provider));
        }
    }

}
