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

package org.terasology.world.chunks;

import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3i;
import org.terasology.module.sandbox.API;

/**
 */
@API
public final class ChunkConstants {
    public static final int SIZE_X = 32;
    public static final int SIZE_Y = 64;
    public static final int SIZE_Z = 32;

    public static final int INNER_CHUNK_POS_FILTER_X = TeraMath.ceilPowerOfTwo(SIZE_X) - 1;
    public static final int INNER_CHUNK_POS_FILTER_Y = TeraMath.ceilPowerOfTwo(SIZE_Y) - 1;
    public static final int INNER_CHUNK_POS_FILTER_Z = TeraMath.ceilPowerOfTwo(SIZE_Z) - 1;

    public static final int POWER_X = TeraMath.sizeOfPower(SIZE_X);
    public static final int POWER_Y = TeraMath.sizeOfPower(SIZE_Y);
    public static final int POWER_Z = TeraMath.sizeOfPower(SIZE_Z);

    public static final byte MAX_LIGHT = 0x0f;
    public static final byte MAX_SUNLIGHT = 0x0f;
    public static final byte MAX_SUNLIGHT_REGEN = 63;
    public static final byte SUNLIGHT_REGEN_THRESHOLD = 48;

    public static final Vector3i CHUNK_POWER = new Vector3i(POWER_X, POWER_Y, POWER_Z);
    public static final Vector3i CHUNK_SIZE = new Vector3i(SIZE_X, SIZE_Y, SIZE_Z);
    public static final Vector3i INNER_CHUNK_POS_FILTER = new Vector3i(INNER_CHUNK_POS_FILTER_X, INNER_CHUNK_POS_FILTER_Y, INNER_CHUNK_POS_FILTER_Z);
    public static final Region3i CHUNK_REGION = Region3i.createFromMinAndSize(Vector3i.zero(), CHUNK_SIZE);

    public static final Vector3i LOCAL_REGION_EXTENTS = new Vector3i(1, 1, 1);

    private ChunkConstants() {
    }
}
