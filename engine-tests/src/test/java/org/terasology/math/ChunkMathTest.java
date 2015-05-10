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

import org.junit.Test;
import org.terasology.config.Config;
import org.terasology.context.internal.ContextImpl;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;

import static org.junit.Assert.assertEquals;

public class ChunkMathTest {

    @Test
    public void getEdgeRegion() {
        Region3i region = Region3i.createFromMinAndSize(new Vector3i(16, 0, 16), new Vector3i(16, 128, 16));
        assertEquals(Region3i.createFromMinMax(new Vector3i(16, 0, 16), new Vector3i(16, 127, 31)), ChunkMath.getEdgeRegion(region, Side.LEFT));
    }

    @Test
    public void regionPositions() {
        CoreRegistry.setContext(new ContextImpl());
        CoreRegistry.put(Config.class, new Config());

        assertEquals(1, ChunkMath.calcChunkPos(Region3i.createFromMinMax(new Vector3i(0, 0, 0), new Vector3i(0, 0, 0))).length);
        assertEquals(1, ChunkMath.calcChunkPos(Region3i.createFromMinMax(new Vector3i(0, 0, 0), new Vector3i(31, 63, 31))).length);
        assertEquals(2, ChunkMath.calcChunkPos(Region3i.createFromMinMax(new Vector3i(0, 0, 0), new Vector3i(32, 63, 31))).length);
        assertEquals(4, ChunkMath.calcChunkPos(Region3i.createFromMinMax(new Vector3i(0, 0, 0), new Vector3i(32, 63, 32))).length);
        assertEquals(8, ChunkMath.calcChunkPos(Region3i.createFromMinMax(new Vector3i(0, 0, 0), new Vector3i(32, 64, 32))).length);
        assertEquals(12, ChunkMath.calcChunkPos(Region3i.createFromMinMax(new Vector3i(-1, 0, 0), new Vector3i(32, 64, 32))).length);

        Vector3i[] chunks = ChunkMath.calcChunkPos(Region3i.createFromMinMax(new Vector3i(0, 0, 0), new Vector3i(32, 63, 31)));
        assertEquals(new Vector3i(0, 0, 0), chunks[0]);
        assertEquals(new Vector3i(1, 0, 0), chunks[1]);
    }
}
