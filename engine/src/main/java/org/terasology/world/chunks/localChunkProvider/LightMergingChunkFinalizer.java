/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.world.chunks.localChunkProvider;

import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.internal.GeneratingChunkProvider;
import org.terasology.world.chunks.internal.ReadyChunkInfo;
import org.terasology.world.propagation.light.LightMerger;

class LightMergingChunkFinalizer implements ChunkFinalizer {

    private LightMerger<ReadyChunkInfo> lightMerger;

    @Override
    public void initialize(final GeneratingChunkProvider generatingChunkProvider) {
        lightMerger = new LightMerger<>(generatingChunkProvider);
    }

    @Override
    public ReadyChunkInfo completeFinalization() {
        return lightMerger.completeMerge();
    }

    @Override
    public void beginFinalization(final Chunk chunk, final ReadyChunkInfo readyChunkInfo) {
        lightMerger.beginMerge(chunk, readyChunkInfo);
    }

    @Override
    public void restart() {
        lightMerger.restart();
    }

    @Override
    public void shutdown() {
        lightMerger.shutdown();
    }
}
