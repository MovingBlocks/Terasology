package org.terasology.blockNetwork;

import org.terasology.math.Vector3i;

public interface Network {
    public boolean hasNetworkingNode(Vector3i location);
    public boolean hasLeafNode(Vector3i location, byte connectingOnSides);
    public int getNetworkSize();
    // TODO
//    public int getDistance(Vector3i from, Vector3i to);
//    public boolean isInDistance(int distance, Vector3i from, Vector3i to);
}
