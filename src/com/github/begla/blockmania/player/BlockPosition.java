/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
 * 
 */
package com.github.begla.blockmania.player;

import com.github.begla.blockmania.rendering.VectorPool;
import org.lwjgl.util.vector.Vector3f;

/**
 * Represents the position of a block. This class is used within the
 * collision detection process.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class BlockPosition implements Comparable<BlockPosition> {

    /**
     * Position on the x-axis.
     */
    /**
     * Position on the y-axis.
     */
    /**
     * Position on the z-axis.
     */
    public final int x;
    public final int y;
    public final int z;
    private final Vector3f _origin;

    /**
     * @param x
     * @param y
     * @param z
     * @param origin
     */
    public BlockPosition(int x, int y, int z, Vector3f origin) {
        this.x = x;
        this.y = y;
        this.z = z;
        this._origin = origin;
    }

    /**
     * @return
     */
    float getDistance() {
        return VectorPool.getVector((float) x - _origin.x, (float) y - _origin.y, (float) z - _origin.z).lengthSquared();
    }

    /**
     * @param o
     * @return
     */
    public int compareTo(BlockPosition o) {
        return new Float(getDistance()).compareTo(o.getDistance());
    }
}
