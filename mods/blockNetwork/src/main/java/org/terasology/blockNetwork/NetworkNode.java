package org.terasology.blockNetwork;

import org.terasology.math.Direction;
import org.terasology.math.DirectionsUtil;
import org.terasology.math.Vector3i;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class NetworkNode {
    public final ImmutableBlockLocation location;
    public final byte connectionSides;

    public NetworkNode(Vector3i location, byte connectionSides) {
        this.location = new ImmutableBlockLocation(location.x, location.y, location.z);
        this.connectionSides = connectionSides;
    }

    public NetworkNode(Vector3i location, Direction ... directions) {
        this(location, DirectionsUtil.getDirections(directions));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NetworkNode that = (NetworkNode) o;

        if (connectionSides != that.connectionSides) return false;
        if (location != null ? !location.equals(that.location) : that.location != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = location != null ? location.hashCode() : 0;
        result = 31 * result + (int) connectionSides;
        return result;
    }
}
