/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.math;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;
import org.terasology.config.Config;
import org.terasology.context.internal.ContextImpl;
import org.terasology.context.internal.MockContext;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockRegions;
import org.terasology.world.chunks.Chunks;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChunkMathTest {


    @Test
    public void testRegionPositions() {
        CoreRegistry.setContext(new ContextImpl());
        CoreRegistry.put(Config.class, new Config(new MockContext()));

        assertEquals(BlockRegions.createFromMinAndMax(0,0,0,0,0,0), Chunks.toChunkRegion(BlockRegions.createFromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(0, 0, 0)), new BlockRegion()));
        assertEquals(BlockRegions.createFromMinAndMax(0,0,0,0,0,0), Chunks.toChunkRegion(BlockRegions.createFromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(31, 63, 31)), new BlockRegion()));
        assertEquals(BlockRegions.createFromMinAndMax(0,0,0,1,0,0), Chunks.toChunkRegion(BlockRegions.createFromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(32, 63, 31)), new BlockRegion()));
        assertEquals(BlockRegions.createFromMinAndMax(0,0,0,1,0,1), Chunks.toChunkRegion(BlockRegions.createFromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(32, 63, 32)), new BlockRegion()));
        assertEquals(BlockRegions.createFromMinAndMax(0,0,0,1,1,1), Chunks.toChunkRegion(BlockRegions.createFromMinAndMax(new Vector3i(0, 0, 0), new Vector3i(32, 64, 32)), new BlockRegion()));
        assertEquals(BlockRegions.createFromMinAndMax(-1,0,0,1,1,1), Chunks.toChunkRegion(BlockRegions.createFromMinAndMax(new Vector3i(-1, 0, 0), new Vector3i(32, 64, 32)), new BlockRegion()));

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
        assertEquals(Chunks.toChunkPos(700, 700, 700, temp), new Vector3i(21, 10, 21));
        assertEquals(Chunks.toChunkPos(200, 700, -1, temp), new Vector3i(6, 10, -1));
        assertEquals(Chunks.toChunkPos(200, 200, 200, temp), new Vector3i(6, 3, 6));
        assertEquals(Chunks.toChunkPos(10, 10, 10, temp), new Vector3i(0, 0, 0));
    }

    @Test
    public void testFloatingPointCalcChunkPos() {
        Vector3i temp = new Vector3i();
        assertEquals(Chunks.toChunkPos(31.9f, 64.1f, 32.5f, temp), new Vector3i(0, 1, 1));
        assertEquals(Chunks.toChunkPos(32.9f, 63.9f, 32.9f, temp), new Vector3i(1, 0, 1));
        assertEquals(Chunks.toChunkPos(31.3f, 63.9f, 31.9f, temp), new Vector3i(0, 0, 0));
        assertEquals(Chunks.toChunkPos(31.6f, 64.5f, 32.1f, temp), new Vector3i(0, 1, 1));
        assertEquals(Chunks.toChunkPos(.1f, -.2f, -.8f, temp), new Vector3i(0, -1, -1));
        assertEquals(Chunks.toChunkPos(-.1f, -.99f, 2f, temp), new Vector3i(-1, -1, 0));

        assertEquals(Chunks.toChunkPos(new Vector3f(31.9f, 64.1f, 32.5f), temp), new Vector3i(0, 1, 1));
        assertEquals(Chunks.toChunkPos(new Vector3f(32.9f, 63.9f, 32.9f), temp), new Vector3i(1, 0, 1));
        assertEquals(Chunks.toChunkPos(new Vector3f(31.3f, 63.9f, 31.9f), temp), new Vector3i(0, 0, 0));
        assertEquals(Chunks.toChunkPos(new Vector3f(31.6f, 64.5f, 32.1f), temp), new Vector3i(0, 1, 1));
        assertEquals(Chunks.toChunkPos(new Vector3f(.1f, -.2f, -.8f), temp), new Vector3i(0, -1, -1));
        assertEquals(Chunks.toChunkPos(new Vector3f(-.1f, -.99f, 2f), temp), new Vector3i(-1, -1, 0));
    }


}
