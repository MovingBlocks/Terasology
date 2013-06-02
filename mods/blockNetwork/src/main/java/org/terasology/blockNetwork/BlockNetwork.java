package org.terasology.blockNetwork;

import com.google.common.collect.*;
import org.terasology.math.Vector3i;

import java.util.*;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BlockNetwork {
    private Set<SimpleNetwork> networks = Sets.newHashSet();
    private Multimap<Vector3i, Byte> leafNodes = HashMultimap.create();
    private Set<Vector3i> networkingNodes = Sets.newHashSet();

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
            networkingNodes.add(location);

            addNetworkingBlockInternal(location, connectingOnSides);
        } finally {
            mutating = false;
        }
    }

    private void addNetworkingBlockInternal(Vector3i location, byte connectingOnSides) {
        SimpleNetwork addToNetwork = null;

        Map<Vector3i, Byte> networkingNodesToAdd = Maps.newHashMap();
        networkingNodesToAdd.put(location, connectingOnSides);

        Multimap<Vector3i, Byte> newLeafNodes = HashMultimap.create();

        // Try adding to existing networks
        final Iterator<SimpleNetwork> networkIterator = networks.iterator();
        while (networkIterator.hasNext()) {
            final SimpleNetwork network = networkIterator.next();
            if (network.canAddNetworkingNode(location, connectingOnSides)) {
                if (addToNetwork == null) {
                    addToNetwork = network;
                } else {
                    Map<Vector3i, Byte> networkingNodes = Maps.newHashMap(network.getNetworkingNodes());
                    Multimap<Vector3i, Byte> leafNodes = HashMultimap.create(network.getLeafNodes());

                    networkingNodesToAdd.putAll(networkingNodes);
                    newLeafNodes.putAll(leafNodes);

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

        for (Map.Entry<Vector3i, Byte> networkingNode : networkingNodesToAdd.entrySet())
            addToNetwork.addNetworkingNode(networkingNode.getKey(), networkingNode.getValue());
        notifyNetworkingNodesAdded(addToNetwork, networkingNodesToAdd);

        for (Map.Entry<Vector3i, Byte> leafNode : newLeafNodes.entries())
            addToNetwork.addLeafNode(leafNode.getKey(), leafNode.getValue());

        // Find all leaf nodes that it joins to its network
        for (Map.Entry<Vector3i, Byte> leafNode : leafNodes.entries()) {
            final Vector3i leafNodeLocation = leafNode.getKey();
            final byte leafNodeConnectingOnSides = leafNode.getValue();
            if (addToNetwork.canAddLeafNode(leafNodeLocation, leafNodeConnectingOnSides)) {
                addToNetwork.addLeafNode(leafNodeLocation, leafNodeConnectingOnSides);
                newLeafNodes.put(leafNodeLocation, leafNodeConnectingOnSides);
            }
        }

        if (newLeafNodes.size() > 0)
            notifyLeafNodesAdded(addToNetwork, newLeafNodes);
    }

    public void addLeafBlock(Vector3i location, byte connectingOnSides) {
        validateNotMutating();
        mutating = true;
        try {
            for (SimpleNetwork network : networks) {
                if (network.canAddLeafNode(location, connectingOnSides)) {
                    network.addLeafNode(location, connectingOnSides);
                    notifyLeafNodesAdded(network, ImmutableMultimap.of(location, connectingOnSides));
                }
            }

            // Check for new degenerated networks
            for (Map.Entry<Vector3i, Byte> leafNode : leafNodes.entries()) {
                final Vector3i leafLocation = leafNode.getKey();
                final byte leafConnectingOnSides = leafNode.getValue();
                if (SimpleNetwork.areNodesConnecting(location, connectingOnSides, leafLocation, leafConnectingOnSides)) {
                    SimpleNetwork degenerateNetwork = SimpleNetwork.createDegenerateNetwork(location, connectingOnSides, leafLocation, leafConnectingOnSides);
                    networks.add(degenerateNetwork);
                    notifyNetworkAdded(degenerateNetwork);
                    notifyLeafNodesAdded(degenerateNetwork, ImmutableMultimap.of(location, connectingOnSides, leafLocation, leafConnectingOnSides));
                }
            }

            leafNodes.put(location, connectingOnSides);
        } finally {
            mutating = false;
        }
    }

    public void updateNetworkingBlock(Vector3i location, byte connectingOnSides) {
        removeNetworkingBlock(location);
        addNetworkingBlock(location, connectingOnSides);
    }

    public void updateLeafBlock(Vector3i location, byte oldConnectingOnSides, byte newConnectingOnSides) {
        removeLeafBlock(location, oldConnectingOnSides);
        addLeafBlock(location, newConnectingOnSides);
    }

    public void removeNetworkingBlock(Vector3i location) {
        validateNotMutating();
        mutating = true;
        try {
            SimpleNetwork networkWithBlock = findNetworkWithNetworkingBlock(location);

            if (networkWithBlock == null)
                throw new IllegalStateException("Trying to remove a networking block that doesn't belong to any network");

            networkingNodes.remove(location);

            // Naive implementation, just remove everything and start over
            // TODO: Improve to actually detects the branches of splits and build separate network for each disjunctioned
            // TODO: network
            Map<Vector3i, Byte> networkingNodes = Maps.newHashMap(networkWithBlock.getNetworkingNodes());
            Multimap<Vector3i, Byte> leafNodes = HashMultimap.create(networkWithBlock.getLeafNodes());

            networkWithBlock.removeAllLeafNodes();
            notifyLeafNodesRemoved(networkWithBlock, leafNodes);
            networkWithBlock.removeAllNetworkingNodes();
            notifyNetworkingNodesRemoved(networkWithBlock, networkingNodes);

            networks.remove(networkWithBlock);
            notifyNetworkRemoved(networkWithBlock);

            for (Map.Entry<Vector3i, Byte> networkingNode : networkingNodes.entrySet()) {
                if (!networkingNode.getKey().equals(location))
                    addNetworkingBlockInternal(networkingNode.getKey(), networkingNode.getValue());
            }
        } finally {
            mutating = false;
        }
    }

    public void removeLeafBlock(Vector3i location, byte connectingOnSides) {
        validateNotMutating();
        mutating = true;
        try {
            leafNodes.remove(location, connectingOnSides);
            final Iterator<SimpleNetwork> networkIterator = networks.iterator();
            while (networkIterator.hasNext()) {
                final SimpleNetwork network = networkIterator.next();
                if (network.hasLeafNode(location, connectingOnSides)) {
                    boolean degenerate = network.removeLeafNode(location, connectingOnSides);
                    if (!degenerate)
                        notifyLeafNodesRemoved(network, ImmutableMultimap.of(location, connectingOnSides));
                    else {
                        Map.Entry<Vector3i, Byte> onlyLeafNode = network.getLeafNodes().entries().iterator().next();
                        notifyLeafNodesRemoved(network, ImmutableMultimap.of(location, connectingOnSides, onlyLeafNode.getKey(), onlyLeafNode.getValue()));
                    }

                    networkIterator.remove();
                    notifyNetworkRemoved(network);
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

    private SimpleNetwork findNetworkWithNetworkingBlock(Vector3i location) {
        for (SimpleNetwork network : networks) {
            if (network.hasNetworkingNode(location))
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

    private void notifyNetworkingNodesAdded(SimpleNetwork network, Map<Vector3i, Byte> networkingNodes) {
        for (NetworkTopologyListener listener : listeners)
            listener.networkingNodesAdded(network, networkingNodes);
    }

    private void notifyNetworkingNodesRemoved(SimpleNetwork network, Map<Vector3i, Byte> networkingNodes) {
        for (NetworkTopologyListener listener : listeners)
            listener.networkingNodesRemoved(network, networkingNodes);
    }

    private void notifyLeafNodesAdded(SimpleNetwork network, Multimap<Vector3i, Byte> leafNodes) {
        for (NetworkTopologyListener listener : listeners)
            listener.leafNodesAdded(network, leafNodes);
    }

    private void notifyLeafNodesRemoved(SimpleNetwork network, Multimap<Vector3i, Byte> leafNodes) {
        for (NetworkTopologyListener listener : listeners)
            listener.leafNodesRemoved(network, leafNodes);
    }
}
