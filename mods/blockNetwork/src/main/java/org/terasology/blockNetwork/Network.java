package org.terasology.blockNetwork;

import org.terasology.math.Vector3i;

public interface Network {
    public boolean hasNetworkingNode(Vector3i location);
    public boolean hasLeafNode(Vector3i location, byte connectingOnSides);
    public int getNetworkSize();
    public int getDistance(Vector3i from, byte fromConnectionSides, Vector3i to, byte toConnectionSides);
    public boolean isInDistance(int distance, Vector3i from, byte fromConnectionSides, Vector3i to, byte toConnectionSides);
}
