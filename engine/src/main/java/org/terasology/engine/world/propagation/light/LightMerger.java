// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.propagation.light;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.math.Side;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.propagation.BatchPropagator;
import org.terasology.engine.world.propagation.LocalChunkView;
import org.terasology.engine.world.propagation.PropagationRules;
import org.terasology.engine.world.propagation.PropagatorWorldView;
import org.terasology.engine.world.propagation.StandardBatchPropagator;
import org.terasology.engine.world.propagation.SunlightRegenBatchPropagator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Merging light in chunks
 */
public class LightMerger {
    private static final int CENTER_INDEX = 13;
    private static final int LOCAL_CHUNKS_SIDE_LENGTH = 3;
    private static final int LOCAL_CHUNKS_ARRAY_LENGTH =
            LOCAL_CHUNKS_SIDE_LENGTH * LOCAL_CHUNKS_SIDE_LENGTH * LOCAL_CHUNKS_SIDE_LENGTH;

    private static final LightPropagationRules LIGHT_RULES = new LightPropagationRules();
    private static final SunlightRegenPropagationRules SUNLIGHT_REGEN_RULES = new SunlightRegenPropagationRules();

    private LightMerger() {

    }

    public static List<Vector3ic> requiredChunks(Vector3ic pos) {
        List<Vector3ic> positions = Lists.newArrayListWithCapacity(3 * 3 * 3);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    positions.add(new Vector3i(pos).add(x, y, z));
                }
            }
        }
        return positions;
    }

    /**
     * Get index in 3d array with side {@link LightMerger#LOCAL_CHUNKS_SIDE_LENGTH}
     *
     * @param side side for calculate
     * @return index of side
     */
    private static int indexOf(Side side) {
        Vector3i center = new Vector3i(1, 1, 1);
        Vector3i pos = side.getAdjacentPos(center, new Vector3i());
        return pos.x * LOCAL_CHUNKS_SIDE_LENGTH * LOCAL_CHUNKS_SIDE_LENGTH + pos.y * LOCAL_CHUNKS_SIDE_LENGTH + pos.z;
    }

    /**
     * Merge light for chunk.
     *
     * @param localChunks nearest chunks with target chunk
     * @throws IllegalArgumentException if {@code localChunks.length != LOCAL_CHUNKS_ARRAY_LENGTH} or {@code
     *         chunk} not in center of {@code localChunks}
     */
    public static Chunk merge(Chunk[] localChunks) {
        Preconditions.checkArgument(localChunks.length == LOCAL_CHUNKS_ARRAY_LENGTH,
                "Length of parameter [localChunks] must be equals [" + LOCAL_CHUNKS_ARRAY_LENGTH + "]");
        Preconditions.checkArgument(Arrays.stream(localChunks).noneMatch(Objects::isNull), "Parameter [localChunks] " +
                "must not contains nulls");

        Arrays.sort(localChunks, Comparator.<Chunk>comparingInt(c -> c.getPosition().x())
                .thenComparingInt(c -> c.getPosition().y())
                .thenComparing(c -> c.getPosition().z()));
        Chunk chunk = localChunks[CENTER_INDEX];

        List<BatchPropagator> propagators = Lists.newArrayList();
        propagators.add(new StandardBatchPropagator(new LightPropagationRules(), new LocalChunkView(localChunks,
                LIGHT_RULES)));
        PropagatorWorldView regenWorldView = new LocalChunkView(localChunks, SUNLIGHT_REGEN_RULES);
        PropagationRules sunlightRules = new SunlightPropagationRules(regenWorldView);
        PropagatorWorldView sunlightWorldView = new LocalChunkView(localChunks, sunlightRules);
        BatchPropagator sunlightPropagator = new StandardBatchPropagator(sunlightRules, sunlightWorldView);
        propagators.add(new SunlightRegenBatchPropagator(SUNLIGHT_REGEN_RULES, regenWorldView, sunlightPropagator,
                sunlightWorldView));
        propagators.add(sunlightPropagator);

        for (BatchPropagator propagator : propagators) {
            // Propagate Inwards
            for (Side side : Side.allSides()) {
                Chunk adjChunk = localChunks[indexOf(side)];
                if (adjChunk != null) {
                    propagator.propagateBetween(adjChunk, chunk, side.reverse(), false);
                }
            }

            // Propagate Outwards
            for (Side side : Side.allSides()) {
                Chunk adjChunk = localChunks[indexOf(side)];
                if (adjChunk != null) {
                    propagator.propagateBetween(chunk, adjChunk, side, true);
                }
            }
        }
        for (BatchPropagator propagator : propagators) {
            propagator.process();
        }
        chunk.deflateSunlight();
        return chunk;
    }
}
