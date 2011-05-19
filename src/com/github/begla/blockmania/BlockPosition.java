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
 */
package com.github.begla.blockmania;

import com.github.begla.blockmania.utilities.VectorPool;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockPosition implements Comparable<BlockPosition> {

    /**
     * Position on the x-axis.
     */
    /**
     * osition on the y-axis.
     */
    /**
     * osition on the z-axis.
     */
    public int x, y, z;
    private Vector3f _origin;

    /**
     * 
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
     * 
     * @param origin
     */
    public void setOrigin(Vector3f origin) {
        this._origin = origin;
    }

    /**
     * 
     * @return
     */
    public Vector3f getOrigin() {
        return _origin;
    }

    /**
     * 
     * @return
     */
    public float getDistance() {
        return VectorPool.getVector((float) x - _origin.x, (float) y - _origin.y, (float) z - _origin.z).lengthSquared();
    }

    /**
     * 
     * @param o
     * @return
     */
    @Override
    public int compareTo(BlockPosition o) {
        return new Float(getDistance()).compareTo(new Float(o.getDistance()));
    }
}
