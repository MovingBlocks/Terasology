// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.math;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.terasology.TestResourceLocks;
import org.terasology.config.Config;
import org.terasology.context.internal.ContextImpl;
import org.terasology.context.internal.MockContext;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.chunks.Chunks;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ResourceLock(TestResourceLocks.CORE_REGISTRY)
public class ChunkMathTest {
    @Test
    public void testRegionPositions() {
        CoreRegistry.setContext(new ContextImpl());
        CoreRegistry.put(Config.class, new Config(new MockContext()));

        assertEquals(new BlockRegion(0, 0, 0, 0, 0, 0), Chunks.toChunkRegion(new BlockRegion(new Vector3i(0, 0, 0), new Vector3i(0, 0, 0)), new BlockRegion(BlockRegion.INVALID)));
        assertEquals(new BlockRegion(0, 0, 0, 0, 0, 0), Chunks.toChunkRegion(new BlockRegion(new Vector3i(0, 0, 0), new Vector3i(31, 63, 31)), new BlockRegion(BlockRegion.INVALID)));
        assertEquals(new BlockRegion(0, 0, 0, 1, 0, 0), Chunks.toChunkRegion(new BlockRegion(new Vector3i(0, 0, 0), new Vector3i(32, 63, 31)), new BlockRegion(BlockRegion.INVALID)));
        assertEquals(new BlockRegion(0, 0, 0, 1, 0, 1), Chunks.toChunkRegion(new BlockRegion(new Vector3i(0, 0, 0), new Vector3i(32, 63, 32)), new BlockRegion(BlockRegion.INVALID)));
        assertEquals(new BlockRegion(0, 0, 0, 1, 1, 1), Chunks.toChunkRegion(new BlockRegion(new Vector3i(0, 0, 0), new Vector3i(32, 64, 32)), new BlockRegion(BlockRegion.INVALID)));
        assertEquals(new BlockRegion(-1, 0, 0, 1, 1, 1), Chunks.toChunkRegion(new BlockRegion(new Vector3i(-1, 0, 0), new Vector3i(32, 64, 32)), new BlockRegion(BlockRegion.INVALID)));
    }

    @Test
    public void testCalcChunk() {
        assertEquals(0, Chunks.toChunkPos(10, 6));
        assertEquals(-1, Chunks.toChunkPos(-1, 6));
        assertEquals(1, Chunks.toChunkPos(100, 6));
        assertEquals(3, Chunks.toChunkPos(200, 6));
    }

    @Test
    public void testCalcChunkPos() {
        org.joml.Vector3i temp = new org.joml.Vector3i();
        assertEquals(new Vector3i(21, 10, 21), Chunks.toChunkPos(700, 700, 700, temp));
        assertEquals(new Vector3i(6, 10, -1), Chunks.toChunkPos(200, 700, -1, temp));
        assertEquals(new Vector3i(6, 3, 6), Chunks.toChunkPos(200, 200, 200, temp));
        assertEquals(new Vector3i(0, 0, 0), Chunks.toChunkPos(10, 10, 10, temp));
    }

    @Test
    public void testFloatingPointCalcChunkPos() {
        Vector3i temp = new Vector3i();
        assertEquals(new Vector3i(0, 1, 1), Chunks.toChunkPos(31.9f, 64.1f, 32.5f, temp));
        assertEquals(new Vector3i(1, 0, 1), Chunks.toChunkPos(32.9f, 63.9f, 32.9f, temp));
        assertEquals(new Vector3i(0, 0, 0), Chunks.toChunkPos(31.3f, 63.9f, 31.9f, temp));
        assertEquals(new Vector3i(0, 1, 1), Chunks.toChunkPos(31.6f, 64.5f, 32.1f, temp));
        assertEquals(new Vector3i(0, -1, -1), Chunks.toChunkPos(.1f, -.2f, -.8f, temp));
        assertEquals(new Vector3i(-1, -1, 0), Chunks.toChunkPos(-.1f, -.99f, 2f, temp));

        assertEquals(new Vector3i(0, 1, 1), Chunks.toChunkPos(new Vector3f(31.9f, 64.1f, 32.5f), temp));
        assertEquals(new Vector3i(1, 0, 1), Chunks.toChunkPos(new Vector3f(32.9f, 63.9f, 32.9f), temp));
        assertEquals(new Vector3i(0, 0, 0), Chunks.toChunkPos(new Vector3f(31.3f, 63.9f, 31.9f), temp));
        assertEquals(new Vector3i(0, 1, 1), Chunks.toChunkPos(new Vector3f(31.6f, 64.5f, 32.1f), temp));
        assertEquals(new Vector3i(0, -1, -1), Chunks.toChunkPos(new Vector3f(.1f, -.2f, -.8f), temp));
        assertEquals(new Vector3i(-1, -1, 0), Chunks.toChunkPos(new Vector3f(-.1f, -.99f, 2f), temp));
    }
}
