// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.tasks;

import org.joml.Vector3i;
import org.terasology.math.JomlUtil;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockRegionIterable;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.internal.GeneratingChunkProvider;
import org.terasology.world.chunks.pipeline.AbstractChunkTask;
import org.terasology.world.chunks.pipeline.ChunkTask;
import org.terasology.world.propagation.light.LightMerger;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LightMergerChunkTask extends AbstractChunkTask {

    private final GeneratingChunkProvider chunkProvider;
    private final LightMerger lightMerger;

    public LightMergerChunkTask(Chunk chunk, GeneratingChunkProvider chunkProvider, LightMerger lightMerger) {
        super(chunk);
        this.chunkProvider = chunkProvider;
        this.lightMerger = lightMerger;
    }

    public static Function<Chunk, ChunkTask> stage(GeneratingChunkProvider chunkProvider, LightMerger lightMerger) {
        return (c) -> new LightMergerChunkTask(c, chunkProvider, lightMerger);
    }

    @Override
    public String getName() {
        return "Light Merging";
    }

    @Override
    public void run() {
        lightMerger.merge(chunk, getNearestChunkStream()
                .toArray(Chunk[]::new));
    }

    private Stream<Chunk> getNearestChunkStream() {
        Vector3i chunkPosition = new Vector3i();
        chunk.getPosition(chunkPosition);
        BlockRegion extentsRegion = new BlockRegion(
                chunkPosition.x - 1, chunkPosition.y - 1, chunkPosition.z - 1,
                chunkPosition.x + 1, chunkPosition.y + 1, chunkPosition.z + 1);
        return StreamSupport.stream(BlockRegionIterable
                        .region(extentsRegion)
                        .build()
                        .spliterator(),
                false)
                .map(Vector3i::new)
                .map((v) -> chunkProvider.getChunkUnready(JomlUtil.from(v))); //TODO make chunkProvider Joml ready.
    }

    private boolean isReadyForLightMerging() {
        return getNearestChunkStream().allMatch(Objects::nonNull);
    }
}
