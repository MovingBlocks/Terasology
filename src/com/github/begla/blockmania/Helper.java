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
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Helper {

    private static long timerTicksPerSecond = Sys.getTimerResolution();
    private static Helper instance = null;
    public static final float div = 1.0f / 16.0f;

    public static enum SIDE {

        LEFT, RIGHT, TOP, BOTTOM, FRONT, BACK;
    };

    public static Helper getInstance() {
        if (instance == null) {
            instance = new Helper();
        }

        return instance;
    }

    public Helper() {
    }

    public Vector2f calcOffsetForTextureAt(int x, int y) {
        return new Vector2f(x * div, y * div);
    }

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
                } else if (side == SIDE.TOP) {
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

    public Vector3f getColorOffsetFor(int type, SIDE side) {
        switch (type) {
            // Grass block
            case 0x1:
                if (side == SIDE.TOP) {
                    return new Vector3f(204f/255f, 255f/255f, 25f/255f);
                }
                break;
            case 0x6:
                return new Vector3f(71f/255f, 214f/255f, 0f/255f);
        }
        return new Vector3f(1.0f, 1.0f, 1.0f);
    }

    public boolean isBlockTypeTranslucent(int type) {
        switch (type) {
            // Grass block
            case 0x6:
                return true;
            default:
                return false;
        }
    }

    public Vector3f calcPlayerOrigin() {
        return new Vector3f(Chunk.chunkDimensions.x * Configuration.viewingDistanceInChunks.x / 2, 127, (Chunk.chunkDimensions.z * Configuration.viewingDistanceInChunks.z) / 2);
    }

    public long getTime() {
        return (Sys.getTime() * 1000) / timerTicksPerSecond;
    }
}
