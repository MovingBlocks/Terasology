// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.block;

import org.joml.Math;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.terasology.module.sandbox.API;

/**
 * Utility class for common block-related operations.
 */
@API
public final class Blocks {
    private Blocks() {
    }

    /**
     * Convert a continuous position in the world to the integer coordinates of the block this position is contained
     * in.
     *
     * @param worldPos the position in the world
     * @return a new vector with the integer block coordinates
     */
    static Vector3i toBlockPos(Vector3fc worldPos) {
        return toBlockPos(worldPos.x(), worldPos.y(), worldPos.z());
    }

    /**
     * Convert a continuous position in the world to the integer coordinates of the block this position is contained
     * in.
     *
     * @param x the x coordinate in the world
     * @param y the y coordinate in the world
     * @param z the z coordinate in the world
     * @return a new vector with the integer block coordinates
     */
    static Vector3i toBlockPos(float x, float y, float z) {
        return toBlockPos(x, y, z, new Vector3i());
    }

    /**
     * Convert a continuous position in the world to the integer coordinates of the block this position is contained
     * in.
     *
     * @param worldPos the position in the world
     * @return {@code dest} holding the integer block coordinates
     */
    static Vector3i toBlockPos(Vector3fc worldPos, Vector3i dest) {
        return toBlockPos(worldPos.x(), worldPos.y(), worldPos.z(), dest);
    }

    /**
     * Convert a continuous position in the world to the integer coordinates of the block this position is contained
     * in.
     *
     * @param x the x coordinate in the world
     * @param y the y coordinate in the world
     * @param z the z coordinate in the world
     * @return {@code dest} holding the integer block coordinates
     */
    static Vector3i toBlockPos(float x, float y, float z, Vector3i dest) {
        dest.x = Math.roundHalfUp(x);
        dest.y = Math.roundHalfUp(y);
        dest.z = Math.roundHalfUp(z);
        return dest;
    }

}
