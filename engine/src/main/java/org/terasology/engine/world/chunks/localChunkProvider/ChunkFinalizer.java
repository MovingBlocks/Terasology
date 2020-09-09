// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks.localChunkProvider;

import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.internal.GeneratingChunkProvider;
import org.terasology.engine.world.chunks.internal.ReadyChunkInfo;

import java.util.List;

/**
 * Post-processor for loaded or generated chunks. Can be used to add extra runtime-metadata like light merging to a
 * chunk before the chunk is stored in memory.
 */
public interface ChunkFinalizer {

    void initialize(GeneratingChunkProvider generatingChunkProvider);

    List<ReadyChunkInfo> completeFinalization();

    void beginFinalization(Chunk chunk, ReadyChunkInfo readyChunkInfo);

    void restart();

    void shutdown();
}
