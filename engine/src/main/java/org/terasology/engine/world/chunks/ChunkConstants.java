// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks;

import org.terasology.engine.math.Region3i;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3i;

/**
 *
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

    public static final byte MAX_LIGHT = 0x0f; // max light for a light source 0-15
    public static final byte MAX_SUNLIGHT = 0x0f; // max sunlight for sunlight bounded 0-15
    public static final byte MAX_SUNLIGHT_REGEN = 63;
    public static final byte SUNLIGHT_REGEN_THRESHOLD = 48;

    public static final Vector3i CHUNK_POWER = new Vector3i(POWER_X, POWER_Y, POWER_Z);
    public static final Vector3i CHUNK_SIZE = new Vector3i(SIZE_X, SIZE_Y, SIZE_Z);
    public static final Vector3i INNER_CHUNK_POS_FILTER = new Vector3i(INNER_CHUNK_POS_FILTER_X,
            INNER_CHUNK_POS_FILTER_Y, INNER_CHUNK_POS_FILTER_Z);
    public static final Region3i CHUNK_REGION = Region3i.createFromMinAndSize(Vector3i.zero(), CHUNK_SIZE);

    public static final Vector3i LOCAL_REGION_EXTENTS = new Vector3i(1, 1, 1);

    private ChunkConstants() {
    }
}
