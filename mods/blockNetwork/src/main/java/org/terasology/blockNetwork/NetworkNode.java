package org.terasology.blockNetwork;

import org.terasology.math.Vector3i;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class NetworkNode {
    public final Vector3i location;
    public final byte connectionSides;

    public NetworkNode(Vector3i location, byte connectionSides) {
        this.location = location;
        this.connectionSides = connectionSides;
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
