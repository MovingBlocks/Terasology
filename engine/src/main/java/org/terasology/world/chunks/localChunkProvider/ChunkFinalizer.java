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

/**
 * Post-processor for loaded or generated chunks.
 * Can be used to add extra runtime-metadata like light merging to a chunk before the chunk is stored in memory.
 */
public interface ChunkFinalizer {

    void initialize(GeneratingChunkProvider generatingChunkProvider);

    ReadyChunkInfo completeFinalization();

    void beginFinalization(Chunk chunk, ReadyChunkInfo readyChunkInfo);

    void restart();

    void shutdown();
}
