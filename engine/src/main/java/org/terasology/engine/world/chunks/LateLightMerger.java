// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks;

import com.google.common.collect.Sets;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.world.propagation.light.LightMerger;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Merges light for chunks after they are already visible, instead of before.
 * <p>
 * {@link LightMerger} used to run as the last stage of {@code ChunkProcessingPipeline}, which meant a
 * chunk could not become ready until all 26 neighbours needed for its merge existed too - one slow or
 * never-requested neighbour stalled the chunk indefinitely. Now a chunk goes ready as soon as its own
 * generation finishes, and this class merges it with its neighbours afterwards, incrementally, as those
 * neighbours arrive. A chunk at the edge of the loaded world simply stays unmerged (visible, with
 * imperfect edge lighting) instead of never becoming ready at all.
 * <p>
 * Shared by {@code LocalChunkProvider} and {@code RemoteChunkProvider}, which otherwise need
 * byte-for-byte the same bookkeeping over their own {@code chunkCache}. {@link #chunkReady} and {@link
 * #processPending} are meant to be called only from the owning provider's {@code update()} - always the
 * same thread, so no internal synchronization here. That matters beyond tidiness: merging now touches
 * chunks that are live, not chunks only the pipeline can see. The renderer may be tessellating them on
 * another thread, and deflated light storage can reallocate on write, so a concurrent merge would not be
 * merely a stale-value glitch. Running on the thread that already owns {@code chunkCache} sidesteps
 * that, mirroring how runtime light propagation for block changes ({@code
 * WorldProviderCoreImpl.processPropagation()}) also runs on the main thread rather than in parallel.
 */
public final class LateLightMerger {
    /**
     * Per-tick budget for {@link #processPending}, in the spirit of the ready-chunk drain loop it runs
     * alongside. Merging a position re-propagates light across a full 3x3x3 of chunks, which is not
     * free, and a single newly-ready chunk can complete several neighbourhoods at once (see {@link
     * #chunkReady}), so this is drained gradually rather than all at once.
     */
    private static final int PROCESSING_DEADLINE_MS = 24;
    private static final Logger logger = LoggerFactory.getLogger(LateLightMerger.class);

    private final Map<Vector3ic, Chunk> chunkCache;
    /** Ready positions still waiting on part of their own 27-chunk neighbourhood. */
    private final Set<Vector3ic> needsMerging = Sets.newHashSet();
    /**
     * Positions whose neighbourhood is complete, awaiting the actual merge. Kept separate from {@link
     * #needsMerging} so discovering a mergeable position (cheap) is decoupled from performing the merge
     * (not cheap) - see {@link #processPending}.
     */
    private final Deque<Vector3ic> readyToMerge = new ArrayDeque<>();

    public LateLightMerger(Map<Vector3ic, Chunk> chunkCache) {
        this.chunkCache = chunkCache;
    }

    /**
     * Record that {@code chunkPos} is ready and already in the chunk cache, and queue a merge for any
     * position this completes the neighbourhood of.
     * <p>
     * The scan below covers {@code chunkPos}'s own 3x3x3 neighbourhood, not just {@code chunkPos}
     * itself - a newly-ready chunk can just as easily complete one of its neighbours' neighbourhoods as
     * its own.
     */
    public void chunkReady(Vector3ic chunkPos) {
        needsMerging.add(new Vector3i(chunkPos));
        for (Vector3ic candidate : LightMerger.requiredChunks(chunkPos)) {
            if (needsMerging.contains(candidate) && hasFullNeighbourhood(candidate)) {
                needsMerging.remove(candidate);
                readyToMerge.add(candidate);
            }
        }
    }

    private boolean hasFullNeighbourhood(Vector3ic pos) {
        for (Vector3ic neighbour : LightMerger.requiredChunks(pos)) {
            if (!chunkCache.containsKey(neighbour)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Drain queued merges until {@link #PROCESSING_DEADLINE_MS} of wall-clock time has been spent.
     */
    public void processPending() {
        long processingStartTime = System.currentTimeMillis();
        Vector3ic pos;
        while ((pos = readyToMerge.poll()) != null) {
            mergeAt(pos);
            long totalProcessingTime = System.currentTimeMillis() - processingStartTime;
            if (!readyToMerge.isEmpty() && totalProcessingTime > PROCESSING_DEADLINE_MS) {
                // Debug, not warn, unlike the ready-chunk drain this sits beside: there, overrunning
                // the budget means cheap per-chunk work took implausibly long and something is wrong.
                // Here a backlog is the designed steady state - merging is expensive and world
                // generation queues it faster than a frame can absorb - so warning would fire every
                // tick for the whole of a normal world load.
                logger.debug("Light merging hit its budget this tick ({}/{}ms). {} positions remain.",
                        totalProcessingTime, PROCESSING_DEADLINE_MS, readyToMerge.size());
                break;
            }
        }
    }

    /**
     * {@link #chunkReady} only checks the neighbourhood is complete at queue time; a relevance change
     * can unload a neighbour before this runs. Re-checking against {@code chunkCache} here, rather than
     * trusting the queue, avoids merging with a hole in the neighbourhood.
     * <p>
     * A position that loses a neighbour that way goes back into {@link #needsMerging} rather than being
     * dropped. It is queued from neither set otherwise, and only a chunk becoming ready re-queues
     * anything - so simply returning would leave it permanently unmerged even once the neighbour
     * reloads, showing as a lighting seam that never heals. The window is not narrow: {@code
     * checkForUnload()} runs every tick ahead of {@link #processPending}, and the budget below routinely
     * leaves positions queued across several ticks.
     */
    private void mergeAt(Vector3ic pos) {
        List<Vector3ic> neighbourhood = LightMerger.requiredChunks(pos);
        Chunk[] chunks = new Chunk[neighbourhood.size()];
        for (int i = 0; i < chunks.length; i++) {
            chunks[i] = chunkCache.get(neighbourhood.get(i));
            if (chunks[i] == null) {
                needsMerging.add(pos);
                return;
            }
        }
        // Chunks whose light this actually moves are marked dirty by LocalChunkView as it writes, so
        // ChunkMeshWorker re-meshes them (it only re-emits chunks that are isReady() && isDirty()).
        // Deliberately not marking the whole neighbourhood here instead: a chunk belongs to 27 of
        // them, so that re-meshes each chunk many times over during a world load - enough to exhaust
        // the heap in mesh generation - and most of those merges never touch its light at all.
        LightMerger.merge(chunks);
    }

    /** Drop any bookkeeping for {@code pos}. Call on unload, or edge positions leak forever. */
    public void chunkUnloaded(Vector3ic pos) {
        needsMerging.remove(pos);
        readyToMerge.remove(pos);
    }

    /** Discard all bookkeeping, e.g. when the world is purged or the provider restarts. */
    public void clear() {
        needsMerging.clear();
        readyToMerge.clear();
    }
}
