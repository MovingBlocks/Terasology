package org.terasology.blockNetwork;

import com.google.common.collect.*;
import org.terasology.math.Vector3i;

import java.util.*;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BlockNetwork {
    private Set<SimpleNetwork> networks = Sets.newHashSet();
    private Set<NetworkNode> leafNodes = Sets.newHashSet();
    private Set<NetworkNode> networkingNodes = Sets.newHashSet();

    private Set<NetworkTopologyListener> listeners = new HashSet<NetworkTopologyListener>();

    private boolean mutating = false;

    public void addTopologyListener(NetworkTopologyListener listener) {
        listeners.add(listener);
    }

    public void removeTopologyListener(NetworkTopologyListener listener) {
        listeners.remove(listener);
    }

    private void validateNotMutating() {
        if (mutating)
            throw new IllegalStateException("Can't modify block network while modification is in progress");
    }

    public void addNetworkingBlock(Vector3i location, byte connectingOnSides) {
        validateNotMutating();
        mutating = true;
        try {
            final NetworkNode networkNode = new NetworkNode(location, connectingOnSides);
            networkingNodes.add(networkNode);

            addNetworkingBlockInternal(networkNode);
        } finally {
            mutating = false;
        }
    }

    private Multimap<Vector3i, Byte> toMultimap(Set<NetworkNode> networkNodes) {
        Multimap<Vector3i, Byte> result = HashMultimap.create();
        for (NetworkNode networkNode : networkNodes) {
            result.put(networkNode.location.toVector3i(), networkNode.connectionSides);
        }
        return result;
    }

    private void addNetworkingBlockInternal(NetworkNode networkNode) {
        SimpleNetwork addToNetwork = null;

        Set<NetworkNode> networkingNodesToAdd = Sets.newHashSet();
        networkingNodesToAdd.add(networkNode);

        Set<NetworkNode> newLeafNodes = Sets.newHashSet();

        // Try adding to existing networks
        final Iterator<SimpleNetwork> networkIterator = networks.iterator();
        while (networkIterator.hasNext()) {
            final SimpleNetwork network = networkIterator.next();
            if (network.canAddNetworkingNode(networkNode)) {
                if (addToNetwork == null) {
                    addToNetwork = network;
                } else {
                    Set<NetworkNode> networkingNodes = Sets.newHashSet(network.getNetworkingNodes());
                    Set<NetworkNode> leafNodes = Sets.newHashSet(network.getLeafNodes());

                    networkingNodesToAdd.addAll(networkingNodes);
                    newLeafNodes.addAll(leafNodes);

                    network.removeAllLeafNodes();
                    notifyLeafNodesRemoved(network, leafNodes);
                    network.removeAllNetworkingNodes();
                    notifyNetworkingNodesRemoved(network, networkingNodes);

                    networkIterator.remove();
                    notifyNetworkRemoved(network);
                }
            }
        }

        // If it's not in any networks, create a new one
        if (addToNetwork == null) {
            SimpleNetwork newNetwork = new SimpleNetwork();
            networks.add(newNetwork);
            notifyNetworkAdded(newNetwork);
            addToNetwork = newNetwork;
        }

        for (NetworkNode networkingNode : networkingNodesToAdd)
            addToNetwork.addNetworkingNode(networkingNode);
        notifyNetworkingNodesAdded(addToNetwork, networkingNodesToAdd);

        for (NetworkNode leafNode : newLeafNodes)
            addToNetwork.addLeafNode(leafNode);

        // Find all leaf nodes that it joins to its network
        for (NetworkNode leafNode : leafNodes) {
            if (addToNetwork.canAddLeafNode(leafNode)) {
                addToNetwork.addLeafNode(leafNode);
                newLeafNodes.add(leafNode);
            }
        }

        if (newLeafNodes.size() > 0)
            notifyLeafNodesAdded(addToNetwork, newLeafNodes);
    }

    public void addLeafBlock(Vector3i location, byte connectingOnSides) {
        validateNotMutating();
        mutating = true;
        try {
            final NetworkNode networkNode = new NetworkNode(location, connectingOnSides);

            for (SimpleNetwork network : networks) {
                if (network.canAddLeafNode(networkNode)) {
                    network.addLeafNode(networkNode);
                    notifyLeafNodesAdded(network, Collections.singleton(networkNode));
                }
            }

            // Check for new degenerated networks
            for (NetworkNode leafNode : leafNodes) {
                if (SimpleNetwork.areNodesConnecting(networkNode, leafNode)) {
                    SimpleNetwork degenerateNetwork = SimpleNetwork.createDegenerateNetwork(networkNode, leafNode);
                    networks.add(degenerateNetwork);
                    notifyNetworkAdded(degenerateNetwork);
                    notifyLeafNodesAdded(degenerateNetwork, Sets.newHashSet(networkNode, leafNode));
                }
            }

            leafNodes.add(networkNode);
        } finally {
            mutating = false;
        }
    }

    public void updateNetworkingBlock(Vector3i location, byte oldConnectingOnSides, byte newConnectingOnSides) {
        removeNetworkingBlock(location, oldConnectingOnSides);
        addNetworkingBlock(location, newConnectingOnSides);
    }

    public void updateLeafBlock(Vector3i location, byte oldConnectingOnSides, byte newConnectingOnSides) {
        removeLeafBlock(location, oldConnectingOnSides);
        addLeafBlock(location, newConnectingOnSides);
    }

    public void removeNetworkingBlock(Vector3i location, byte connectingOnSides) {
        validateNotMutating();
        mutating = true;
        try {
            NetworkNode networkNode = new NetworkNode(location, connectingOnSides);

            SimpleNetwork networkWithBlock = findNetworkWithNetworkingBlock(networkNode);

            if (networkWithBlock == null)
                throw new IllegalStateException("Trying to remove a networking block that doesn't belong to any network");

            networkingNodes.remove(networkNode);

            // Naive implementation, just remove everything and start over
            // TODO: Improve to actually detects the branches of splits and build separate network for each disjunctioned
            // TODO: network
            Set<NetworkNode> networkingNodes = Sets.newHashSet(networkWithBlock.getNetworkingNodes());
            Set<NetworkNode> leafNodes = Sets.newHashSet(networkWithBlock.getLeafNodes());

            networkWithBlock.removeAllLeafNodes();
            notifyLeafNodesRemoved(networkWithBlock, leafNodes);
            networkWithBlock.removeAllNetworkingNodes();
            notifyNetworkingNodesRemoved(networkWithBlock, Collections.unmodifiableSet(networkingNodes));

            networks.remove(networkWithBlock);
            notifyNetworkRemoved(networkWithBlock);

            for (NetworkNode networkingNode : networkingNodes) {
                if (!networkingNode.equals(networkNode))
                    addNetworkingBlockInternal(networkingNode);
            }
        } finally {
            mutating = false;
        }
    }

    public void removeLeafBlock(Vector3i location, byte connectingOnSides) {
        validateNotMutating();
        mutating = true;
        try {
            NetworkNode networkNode = new NetworkNode(location, connectingOnSides);

            leafNodes.remove(networkNode);
            final Iterator<SimpleNetwork> networkIterator = networks.iterator();
            while (networkIterator.hasNext()) {
                final SimpleNetwork network = networkIterator.next();
                if (network.hasLeafNode(networkNode)) {
                    boolean degenerate = network.removeLeafNode(networkNode);
                    if (!degenerate)
                        notifyLeafNodesRemoved(network, Collections.singleton(networkNode));
                    else {
                        NetworkNode onlyLeafNode = network.getLeafNodes().iterator().next();
                        notifyLeafNodesRemoved(network, Sets.<NetworkNode>newHashSet(networkNode, onlyLeafNode));

                        networkIterator.remove();
                        notifyNetworkRemoved(network);
                    }
                }
            }
        } finally {
            mutating = false;
        }
    }

    public Collection<? extends Network> getNetworks() {
        return Collections.unmodifiableCollection(networks);
    }

    public boolean isNetworkActive(Network network) {
        return networks.contains(network);
    }

    private SimpleNetwork findNetworkWithNetworkingBlock(NetworkNode networkNode) {
        for (SimpleNetwork network : networks) {
            if (network.hasNetworkingNode(networkNode))
                return network;
        }
        return null;
    }

    private void notifyNetworkAdded(SimpleNetwork network) {
        for (NetworkTopologyListener listener : listeners)
            listener.networkAdded(network);
    }

    private void notifyNetworkRemoved(SimpleNetwork network) {
        for (NetworkTopologyListener listener : listeners)
            listener.networkRemoved(network);
    }

    private void notifyNetworkingNodesAdded(SimpleNetwork network, Set<NetworkNode> networkingNodes) {
        for (NetworkTopologyListener listener : listeners)
            listener.networkingNodesAdded(network, networkingNodes);
    }

    private void notifyNetworkingNodesRemoved(SimpleNetwork network, Set<NetworkNode> networkingNodes) {
        for (NetworkTopologyListener listener : listeners)
            listener.networkingNodesRemoved(network, networkingNodes);
    }

    private void notifyLeafNodesAdded(SimpleNetwork network, Set<NetworkNode> leafNodes) {
        for (NetworkTopologyListener listener : listeners)
            listener.leafNodesAdded(network, leafNodes);
    }

    private void notifyLeafNodesRemoved(SimpleNetwork network, Set<NetworkNode> leafNodes) {
        for (NetworkTopologyListener listener : listeners)
            listener.leafNodesRemoved(network, leafNodes);
    }
}
