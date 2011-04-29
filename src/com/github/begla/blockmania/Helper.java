/*
 *  Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.github.begla.blockmania;

import org.lwjgl.Sys;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

/**
 * This is a simple helper class for various tasks.
 * 
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Helper {

    private static long _timerTicksPerSecond = Sys.getTimerResolution();
    private static Helper _instance = null;
    private static final float _div = 1.0f / 16.0f;

    public static enum SIDE {

        LEFT, RIGHT, TOP, BOTTOM, FRONT, BACK;
    };

    /**
     * Returns the static instance of this helper class.
     *
     * @return The instance
     */
    public static Helper getInstance() {
        if (_instance == null) {
            _instance = new Helper();
        }

        return _instance;
    }

    /**
     * Calculates the texture offset for a given position within
     * the texture atlas.
     * 
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return The texture offset
     */
    private Vector2f calcOffsetForTextureAt(int x, int y) {
        return new Vector2f(x * _div, y * _div);
    }

    /**
     * Calculates the texture offset for a given block type and a specific
     * side of the block.
     * 
     * @param type The type of the block
     * @param side The side of the block
     * @return The texture offset
     */
    public Vector2f getTextureOffsetFor(int type, SIDE side) {
        switch (type) {
            // Grass block
            case 0x1:
                if (side == SIDE.LEFT || side == SIDE.RIGHT || side == SIDE.FRONT || side == SIDE.BACK) {
                    return calcOffsetForTextureAt(3, 0);
                } else if (side == SIDE.TOP) {
                    return calcOffsetForTextureAt(0, 0);
                }
                break;
            // Dirt block
            case 0x2:
                return calcOffsetForTextureAt(2, 0);
            // Stone block
            case 0x3:
                return calcOffsetForTextureAt(1, 0);
            // Water block
            case 0x4:
                return calcOffsetForTextureAt(15, 13);
            // Tree block
            case 0x5:
                if (side == SIDE.LEFT || side == SIDE.RIGHT || side == SIDE.FRONT || side == SIDE.BACK) {
                    return calcOffsetForTextureAt(4, 1);
                } else if (side == SIDE.TOP || side == SIDE.BOTTOM) {
                    return calcOffsetForTextureAt(5, 1);
                }
                break;
            // Leaf block
            case 0x6:
                return calcOffsetForTextureAt(4, 3);
            // Sand block
            case 0x7:
                return calcOffsetForTextureAt(2, 1);
            default:
                return calcOffsetForTextureAt(2, 0);
        }

        return calcOffsetForTextureAt(2, 0);
    }

    /**
     * Calculates the color offset for a given block type and a speific
     * side of the block.
     * 
     * @param type The block type
     * @param side The block side
     * @return The color offset
     */
    public Vector3f getColorOffsetFor(int type, SIDE side) {
        switch (type) {
            // Grass block
            case 0x1:
                if (side == SIDE.TOP) {
                    return new Vector3f(204f / 255f, 255f / 255f, 25f / 255f);
                }
                break;
            case 0x6:
                return new Vector3f(90f / 255f, 190f / 255f, 89f / 255f);
        }
        return new Vector3f(1.0f, 1.0f, 1.0f);
    }

    /**
     * Returns true if a given block type is translucent.
     * 
     * @param type The block type
     * @return True if the block type is translucent
     */
    public boolean isBlockTypeTranslucent(int type) {
        switch (type) {
            // Grass block
            case 0x6:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns the spawning point of the player.
     * TODO: Should not determine the spawning point randomly
     * 
     * @return The coordinates of the spawning point
     */
    public Vector3f calcPlayerOrigin() {
        return new Vector3f(Configuration.CHUNK_DIMENSIONS.x * Configuration.VIEWING_DISTANCE_IN_CHUNKS.x / 2, 127, (Configuration.CHUNK_DIMENSIONS.z * Configuration.VIEWING_DISTANCE_IN_CHUNKS.z) / 2);
    }

    /**
     * Returns the system time.
     * 
     * @return The system time
     */
    public long getTime() {
        return (Sys.getTime() * 1000) / _timerTicksPerSecond;
    }

    /**
     * Applies Cantor's pairing function on 2D coordinates.
     *
     * @param k1 X-Coordinate
     * @param k2 Y-Coordinate
     * @return Unique 1D value
     */
    public int cantorize(int k1, int k2) {
        return ((k1 + k2) * (k1 + k2 + 1) / 2) + k2;
    }
}
