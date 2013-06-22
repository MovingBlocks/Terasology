package org.terasology.blockNetwork;

import org.terasology.math.Vector3i;

public interface Network {
    public boolean hasNetworkingNode(NetworkNode networkNode);
    public boolean hasLeafNode(NetworkNode networkNode);
    public int getNetworkSize();
    public int getDistance(NetworkNode from, NetworkNode to);
    public boolean isInDistance(int distance, NetworkNode from, NetworkNode to);
}
