package org.terasology.blockNetwork;

import org.terasology.math.Vector3i;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Multimap;

public interface NetworkTopologyListener {
    public void networkAdded(Network network);
    public void networkingNodesAdded(Network network, Set<NetworkNode> networkingNodes);
    public void networkingNodesRemoved(Network network, Set<NetworkNode> networkingNodes);
    public void leafNodesAdded(Network network, Set<NetworkNode> leafNodes);
    public void leafNodesRemoved(Network network, Set<NetworkNode> leafNodes);
    public void networkRemoved(Network network);
}
