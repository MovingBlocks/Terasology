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
package com.github.begla.blockmania.datastructures;

import javax.vecmath.Vector3f;

/**
 * Represents the position of a block. This class is used mainly in the
 * collision detection processes.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class BlockPosition implements Comparable<BlockPosition> {

    public final int x;
    public final int y;
    public final int z;
    private Vector3f _origin;

    public BlockPosition(Vector3f v) {
        this.x = (int) v.x;
        this.y = (int) v.y;
        this.z = (int) v.z;
    }

    public BlockPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPosition(int x, int y, int z, Vector3f origin) {
        this.x = x;
        this.y = y;
        this.z = z;
        this._origin = origin;
    }

    double getDistance() {
        if (_origin == null)
            return 0;

        return new Vector3f(x - _origin.x, y - _origin.y, z - _origin.z).length();
    }

    public Vector3f toVector3f() {
        return new Vector3f(x, y, z);
    }

    public boolean equals(Object o) {
        if (o.getClass() != BlockPosition.class)
            return false;

        BlockPosition p = (BlockPosition) o;
        return p.x == this.x && p.y == this.y && p.z == this.z;
    }

    public int hashCode() {
        return (((x * 33) ^ y) * 17) ^ z;
    }

    public int compareTo(BlockPosition o) {
        double distance = getDistance();
        double oDistance = o.getDistance();

        if (oDistance > distance)
            return -1;

        if (oDistance < distance)
            return 1;

        return 0;
    }
}
