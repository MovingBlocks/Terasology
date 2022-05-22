// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EntityScope;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkRegionListener;
import org.terasology.engine.world.chunks.localChunkProvider.RelevanceSystem;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Completes when all the chunks in a region are loaded.
 *
 * @see MainLoop#makeBlocksRelevant
 * @see MainLoop#makeChunksRelevant
 */
@SuppressWarnings("checkstyle:finalclass")
public class ChunkRegionFuture {
    public static final int REQUIRED_CHUNK_MARGIN = 1;

    private static final Logger logger = LoggerFactory.getLogger(ChunkRegionFuture.class);

    protected final SettableFuture<ChunkRegionFuture> future = SettableFuture.create();
    protected final Set<Chunk> loadedChunks = new HashSet<>();
    protected final BlockRegion chunks = new BlockRegion(BlockRegion.INVALID);

    private final EntityRef entity;

    private ChunkRegionFuture(EntityRef entity, Function<ChunkRegionListener, BlockRegionc> chunks) {
        this.entity = entity;
        this.chunks.set(chunks.apply(new Listener(this::onChunkRelevant)));
    }

    /**
     * Load an area of the world.
     * <p>
     * The area is defined as a {@index "relevance region"} and will not be unloaded as long as {@link #entity} exists
     * and has a {@link LocationComponent}.
     *
     * @param entityManager used to create the entity that depends on this region
     * @param relevanceSystem the authority on what is relevant
     * @param center a point to center the region around, in block coordinates
     * @param sizeInChunks the size of the region, in chunks
     */
    static ChunkRegionFuture create(EntityManager entityManager, RelevanceSystem relevanceSystem, Vector3fc center,
                                            Vector3ic sizeInChunks) {
        EntityRef entity = entityManager.create(new LocationComponent(center));
        entity.setScope(EntityScope.GLOBAL);

        Vector3ic correctedSizeInChunks = addMargin(sizeInChunks);

        Function<ChunkRegionListener, BlockRegionc> makeChunksRelevant = listener -> {
            BlockRegionc paddedRegion =
                    relevanceSystem.addRelevanceEntity(entity, correctedSizeInChunks, listener);
            return removeMargin(paddedRegion);
        };

        return new ChunkRegionFuture(entity, makeChunksRelevant);
    }

    /**
     * Removes the margin added by {@link #addMargin}.
     *
     * @return new instance of the contained region
     */
    private static BlockRegionc removeMargin(BlockRegionc relRegion) {
        return relRegion.expand(-REQUIRED_CHUNK_MARGIN, -REQUIRED_CHUNK_MARGIN, -REQUIRED_CHUNK_MARGIN,
                new BlockRegion(BlockRegion.INVALID));
    }

    private static Vector3ic addMargin(Vector3ic sizeInChunks) {
        Vector3i desiredSize = new Vector3i(sizeInChunks);

        // FIXME: add an interface to RelevanceSystem that takes radii as inputs,
        //     so we don't have to reverse-engineer its rounding algorithms.
        // Dimensions of relevance regions are odd-numbered so they can be symmetrical around
        // their center.
        desiredSize.x |= 1;
        desiredSize.y |= 1;
        desiredSize.z |= 1;

        // FIXME: Is the complete relevance region not actually loaded‽
        // Need a buffer on either side.
        desiredSize.add(2 * REQUIRED_CHUNK_MARGIN, 2 * REQUIRED_CHUNK_MARGIN, 2 * REQUIRED_CHUNK_MARGIN);
        return desiredSize;
    }

    /**
     * Completes when all expected chunks have loaded.
     * <p>
     * <b>Experimental:</b> Unsure which objects are useful to return, I made a bunch of them available
     * through this class and we return the whole thing. Though returning a future for an object
     * the caller already has doesn't make a lot of sense.
     *
     * @return complete when all expected chunks have loaded
     */
    public ListenableFuture<ChunkRegionFuture> getFuture() {
        return future;
    }

    @SuppressWarnings("unused")
    public Set<Chunk> getLoadedChunks() {
        return Collections.unmodifiableSet(loadedChunks);
    }

    /** The entity defining the relevance region. */
    public EntityRef getEntity() {
        return entity;
    }

    @SuppressWarnings("unused")
    public BlockRegionc getChunkRegion() {
        return chunks;
    }

    protected void onChunkRelevant(Chunk chunk) {
        loadedChunks.add(chunk);
        if (chunks.isValid()) {
            logger.debug("Got chunk {} / {}", loadedChunks.size(), chunks.volume());
            if (loadedChunks.size() >= chunks.volume() && !future.isDone()) {
                future.set(this);
            }
        }
    }

    /** Adapts a {@code Consumer<Chunk>} to a {@code ChunkRegionListener}. */
    private static class Listener implements ChunkRegionListener {
        private final Consumer<Chunk> onChunk;

        Listener(Consumer<Chunk> onChunk) {
            this.onChunk = onChunk;
        }

        @Override
        public void onChunkRelevant(Vector3ic pos, Chunk chunk) {
            onChunk.accept(chunk);
        }

        @Override
        public void onChunkIrrelevant(Vector3ic pos) {
            // FIXME: Document why this IS, in fact, called regularly.
            //     We thought this would be called when the location of the entity changes,
            //     meaning previously-relevant chunks are no longer in range and become irrelevant.
            //     But in practice, we see this being called even when we aren't doing anything to
            //     change the location of the region's entity.
//            UnsupportedOperationException error = new UnsupportedOperationException(String.format(
//                    "No chunks in this region should be irrelevant! That was the whole point!" +
//                            "Position: %s",
//                    pos
//            ));
//            if (failOnIrrelevantEvent) {
//                future.setException(error);
//                throw error;
//            }
            // logger.warn("Irrelevant???", error);
        }
    }
}
