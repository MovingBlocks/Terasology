/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.location;

import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;

public class ImmutableBlockLocation {
    public final int x;
    public final int y;
    public final int z;

    public ImmutableBlockLocation(Vector3i location) {
        this(location.x, location.y, location.z);
    }

    public ImmutableBlockLocation(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ImmutableBlockLocation move(Side side) {
        final Vector3i directionVector = side.getVector3i();
        return new ImmutableBlockLocation(x + directionVector.x, y + directionVector.y, z + directionVector.z);
    }

    public Vector3i toVector3i() {
        return new Vector3i(x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ImmutableBlockLocation that = (ImmutableBlockLocation) o;

        if (x != that.x) {
            return false;
        }

        if (y != that.y) {
            return false;
        }

        if (z != that.z) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }
}
