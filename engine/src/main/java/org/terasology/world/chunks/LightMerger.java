/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.world.chunks;

import com.google.common.collect.Lists;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.world.chunks.internal.ChunkImpl;
import org.terasology.world.chunks.internal.GeneratingChunkProvider;
import org.terasology.world.propagation.BatchPropagator;
import org.terasology.world.propagation.LocalChunkView;
import org.terasology.world.propagation.PropagationRules;
import org.terasology.world.propagation.PropagatorWorldView;
import org.terasology.world.propagation.StandardBatchPropagator;
import org.terasology.world.propagation.SunlightRegenBatchPropagator;
import org.terasology.world.propagation.light.LightPropagationRules;
import org.terasology.world.propagation.light.SunlightPropagationRules;
import org.terasology.world.propagation.light.SunlightRegenPropagationRules;

import java.util.List;

/**
 * @author Immortius
 */
public class LightMerger {

    private GeneratingChunkProvider chunkProvider;
    private LightPropagationRules lightRules = new LightPropagationRules();
    private SunlightRegenPropagationRules sunlightRegenRules = new SunlightRegenPropagationRules();

    public LightMerger(GeneratingChunkProvider chunkProvider) {
        this.chunkProvider = chunkProvider;
    }

    public void merge(ChunkImpl chunk) {
        PerformanceMonitor.startActivity("Light Merge");
        ChunkImpl[] localChunks = assembleLocalChunks(chunk);

        List<BatchPropagator> propagators = Lists.newArrayList();
        propagators.add(new StandardBatchPropagator(new LightPropagationRules(), new LocalChunkView(localChunks, lightRules)));
        PropagatorWorldView regenWorldView = new LocalChunkView(localChunks, sunlightRegenRules);
        PropagationRules sunlightRules = new SunlightPropagationRules(regenWorldView);
        PropagatorWorldView sunlightWorldView = new LocalChunkView(localChunks, sunlightRules);
        BatchPropagator sunlightPropagator = new StandardBatchPropagator(sunlightRules, sunlightWorldView);
        propagators.add(new SunlightRegenBatchPropagator(sunlightRegenRules, regenWorldView, sunlightPropagator, sunlightWorldView));
        propagators.add(sunlightPropagator);

        for (BatchPropagator propagator : propagators) {
            // Propagate Inwards
            for (Side side : Side.values()) {
                Vector3i adjChunkPos = side.getAdjacentPos(chunk.getPos());
                ChunkImpl adjChunk = chunkProvider.getChunkUnready(adjChunkPos);
                if (adjChunk != null) {
                    propagator.propagateBetween(adjChunk, chunk, side.reverse(), false);
                }
            }

            // Propagate Outwards
            for (Side side : Side.values()) {
                Vector3i adjChunkPos = side.getAdjacentPos(chunk.getPos());
                ChunkImpl adjChunk = chunkProvider.getChunk(adjChunkPos);
                if (adjChunk != null) {
                    propagator.propagateBetween(chunk, adjChunk, side, true);
                }
            }
        }
        for (BatchPropagator propagator : propagators) {
            propagator.process();
        }
        chunk.deflateSunlight();
        PerformanceMonitor.endActivity();
    }

    private ChunkImpl[] assembleLocalChunks(ChunkImpl chunk) {
        ChunkImpl[] localChunks = new ChunkImpl[27];
        int index = 0;
        for (int z = -1; z < 2; ++z) {
            for (int y = -1; y < 2; ++y) {
                for (int x = -1; x < 2; ++x) {
                    ChunkImpl localChunk = chunkProvider.getChunk(chunk.getPos().x + x, chunk.getPos().y + y, chunk.getPos().z + z);
                    if (localChunk != null) {
                        localChunks[index] = localChunk;
                    }
                    index++;
                }
            }
        }
        return localChunks;
    }

}
