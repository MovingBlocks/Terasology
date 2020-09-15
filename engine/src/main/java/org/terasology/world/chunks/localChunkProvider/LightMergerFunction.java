// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.localChunkProvider;

import org.terasology.world.chunks.Chunk;

import java.util.function.Function;

public class LightMergerFunction implements Function<Chunk, Chunk> {
    @Override
    public Chunk apply(Chunk chunk) {
        return chunk;
    }
}
