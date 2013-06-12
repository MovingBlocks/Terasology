package org.terasology.blockNetwork;

import org.terasology.math.Direction;
import org.terasology.math.Vector3i;

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

    public ImmutableBlockLocation move(Direction direction) {
        final Vector3i directionVector = direction.getVector3i();
        return new ImmutableBlockLocation(x+directionVector.x, y+directionVector.y, z+directionVector.z);
    }

    public Vector3i toVector3i() {
        return new Vector3i(x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImmutableBlockLocation that = (ImmutableBlockLocation) o;

        if (x != that.x) return false;
        if (y != that.y) return false;
        if (z != that.z) return false;

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
