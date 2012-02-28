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
package org.terasology.model.structures;

import org.terasology.math.Vector3i;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * Represents the position of a block. This class is used mainly in the
 * collision detection processes.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
// TODO: Move origin-distance functionality out of BlockPosition, remove this class and use Vector3i instead
public final class BlockPosition extends Vector3i implements Comparable<BlockPosition> {

    private Vector3d _origin;

    public BlockPosition() {
    }

    public BlockPosition(Vector3f v) {
        this.x = (int) v.x;
        this.y = (int) v.y;
        this.z = (int) v.z;
    }

    public BlockPosition(Vector3d v) {
        this.x = (int) v.x;
        this.y = (int) v.y;
        this.z = (int) v.z;
    }

    public BlockPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPosition(int x, int y, int z, Vector3d origin) {
        this.x = x;
        this.y = y;
        this.z = z;
        this._origin = origin;
    }

    public BlockPosition(int x, int y, int z, Vector3f origin) {
        this.x = x;
        this.y = y;
        this.z = z;
        this._origin = new Vector3d(origin);
    }

    double calcDistanceToOrigin() {
        if (_origin == null)
            return 0;

        return new Vector3d(x - _origin.x, y - _origin.y, z - _origin.z).length();
    }

    public int compareTo(BlockPosition o) {
        double distance = calcDistanceToOrigin();
        double oDistance = o.calcDistanceToOrigin();

        if (oDistance > distance)
            return -1;

        if (oDistance < distance)
            return 1;

        return 0;
    }
}
