package org.terasology.blockNetwork;

import com.google.common.collect.*;

import java.util.*;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BlockNetwork {
    private Set<SimpleNetwork> networks = Sets.newHashSet();
    private Multimap<ImmutableBlockLocation, NetworkNode> leafNodes = HashMultimap.create();
    private Multimap<ImmutableBlockLocation, NetworkNode> networkingNodes = HashMultimap.create();

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

    public void addNetworkingBlock(NetworkNode networkNode) {
        validateNotMutating();
        mutating = true;
        try {
            validateNoNetworkingOverlap(networkNode);
            networkingNodes.put(networkNode.location, networkNode);

            addNetworkingBlockInternal(networkNode);
        } finally {
            mutating = false;
        }
    }

    public void addNetworkingBlocks(Collection<NetworkNode> networkNodes) {
        // No major optimization possible here
        for (NetworkNode networkNode : networkNodes) {
            addNetworkingBlock(networkNode);
        }
    }

    private void validateNoNetworkingOverlap(NetworkNode networkNode) {
        for (NetworkNode nodeAtSamePosition : networkingNodes.get(networkNode.location)) {
            if ((nodeAtSamePosition.connectionSides & networkNode.connectionSides) > 0)
                throw new IllegalStateException("There is a networking block at that position connecting to some of the same sides already");
        }
    }

    private void validateNoLeafOverlap(NetworkNode networkNode) {
        for (NetworkNode nodeAtSamePosition : leafNodes.get(networkNode.location)) {
            if ((nodeAtSamePosition.connectionSides & networkNode.connectionSides) > 0)
                throw new IllegalStateException("There is a leaf block at that position connecting to some of the same sides already");
        }
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
        for (NetworkNode leafNode : leafNodes.values()) {
            if (addToNetwork.canAddLeafNode(leafNode)) {
                addToNetwork.addLeafNode(leafNode);
                newLeafNodes.add(leafNode);
            }
        }

        if (newLeafNodes.size() > 0)
            notifyLeafNodesAdded(addToNetwork, newLeafNodes);
    }

    public void addLeafBlock(NetworkNode networkNode) {
        validateNotMutating();
        mutating = true;
        try {
            validateNoLeafOverlap(networkNode);

            for (SimpleNetwork network : networks) {
                if (network.canAddLeafNode(networkNode)) {
                    network.addLeafNode(networkNode);
                    notifyLeafNodesAdded(network, Collections.singleton(networkNode));
                }
            }

            // Check for new degenerated networks
            for (NetworkNode leafNode : leafNodes.values()) {
                if (SimpleNetwork.areNodesConnecting(networkNode, leafNode)) {
                    SimpleNetwork degenerateNetwork = SimpleNetwork.createDegenerateNetwork(networkNode, leafNode);
                    networks.add(degenerateNetwork);
                    notifyNetworkAdded(degenerateNetwork);
                    notifyLeafNodesAdded(degenerateNetwork, Sets.newHashSet(networkNode, leafNode));
                }
            }

            leafNodes.put(networkNode.location, networkNode);
        } finally {
            mutating = false;
        }
    }

    public void addLeafBlocks(Collection<NetworkNode> networkNodes) {
        // No optimizations can be made here
        for (NetworkNode networkNode : networkNodes) {
            addLeafBlock(networkNode);
        }
    }

    public void updateNetworkingBlock(NetworkNode oldNode, NetworkNode newNode) {
        removeNetworkingBlock(oldNode);
        addNetworkingBlock(newNode);
    }

    public void updateLeafBlock(NetworkNode oldNode, NetworkNode newNode) {
        removeLeafBlock(oldNode);
        addLeafBlock(newNode);
    }

    public void removeNetworkingBlock(NetworkNode networkNode) {
        validateNotMutating();
        mutating = true;
        try {
            SimpleNetwork networkWithBlock = findNetworkWithNetworkingBlock(networkNode);

            if (networkWithBlock == null)
                throw new IllegalStateException("Trying to remove a networking block that doesn't belong to any network");

            networkingNodes.remove(networkNode.location, networkNode);

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

    public void removeNetworkingBlocks(Collection<NetworkNode> networkNodes) {
        if (networkNodes.size() == 0)
            return;
        // This performance improvement is needed until the split detection (above) is improved, after that it can be
        // removed
        validateNotMutating();
        mutating = true;
        try {
            Set<SimpleNetwork> affectedNetworks = Sets.newHashSet();
            for (NetworkNode networkNode : networkNodes) {
                final SimpleNetwork networkWithBlock = findNetworkWithNetworkingBlock(networkNode);
                if (networkWithBlock == null)
                    throw new IllegalStateException("Trying to remove a networking block that doesn't belong to any network");

                affectedNetworks.add(networkWithBlock);
                networkingNodes.remove(networkNode.location, networkNode);
            }

            List<Set<NetworkNode>> listOfNodesFromModifiedNetworks = Lists.newLinkedList();
            for (SimpleNetwork networkWithBlock : affectedNetworks) {
                Set<NetworkNode> leafNodes = Sets.newHashSet(networkWithBlock.getLeafNodes());
                Set<NetworkNode> networkingNodes = Sets.newHashSet(networkWithBlock.getNetworkingNodes());

                networkWithBlock.removeAllLeafNodes();
                notifyLeafNodesAdded(networkWithBlock, leafNodes);
                networkWithBlock.removeAllNetworkingNodes();
                notifyNetworkingNodesRemoved(networkWithBlock, Collections.unmodifiableSet(networkingNodes));

                networks.remove(networkWithBlock);
                notifyNetworkRemoved(networkWithBlock);
            }

            for (Set<NetworkNode> networkingNodesInModifiedNetwork : listOfNodesFromModifiedNetworks) {
                for (NetworkNode networkingNode : networkingNodesInModifiedNetwork) {
                    if (!networkNodes.contains(networkingNode))
                        addNetworkingBlockInternal(networkingNode);
                }
            }
        } finally {
            mutating = false;
        }
    }

    public void removeLeafBlock(NetworkNode networkNode) {
        validateNotMutating();
        mutating = true;
        try {
            leafNodes.remove(networkNode.location, networkNode);
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

    public void removeLeafBlocks(Collection<NetworkNode> networkNodes) {
        // No optimization can be made here
        for (NetworkNode networkNode : networkNodes) {
            removeLeafBlock(networkNode);
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
