package org.terasology.blockNetwork;

import org.terasology.math.Vector3i;

import java.util.Map;

import com.google.common.collect.Multimap;

public interface NetworkTopologyListener {
    public void networkAdded(Network network);
    public void networkingNodesAdded(Network network, Multimap<Vector3i, Byte> networkingNodes);
    public void networkingNodesRemoved(Network network, Multimap<Vector3i, Byte> networkingNodes);
    public void leafNodesAdded(Network network, Multimap<Vector3i, Byte> leafNodes);
    public void leafNodesRemoved(Network network, Multimap<Vector3i, Byte> leafNodes);
    public void networkRemoved(Network network);
}
