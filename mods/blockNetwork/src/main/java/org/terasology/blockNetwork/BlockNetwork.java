package org.terasology.blockNetwork;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.terasology.math.Vector3i;

import java.util.*;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BlockNetwork {
    private Set<SimpleNetwork> networks = Sets.newHashSet();
    private Multimap<Vector3i, Byte> leafNodes = HashMultimap.create();
    private Set<Vector3i> networkingNodes = Sets.newHashSet();

    private Set<BlockNetworkTopologyListener> listeners = new HashSet<BlockNetworkTopologyListener>();

    public void addTopologyListener(BlockNetworkTopologyListener listener) {
        listeners.add(listener);
    }

    public void removeTopologyListener(BlockNetworkTopologyListener listener) {
        listeners.remove(listener);
    }

    public void addNetworkingBlock(Vector3i location, byte connectingOnSides) {
        networkingNodes.add(location);
        SimpleNetwork addedToNetwork = null;

        // Try adding to existing networks
        final Iterator<SimpleNetwork> networkIterator = networks.iterator();
        while (networkIterator.hasNext()) {
            final SimpleNetwork network = networkIterator.next();
            if (network.canAddNetworkingNode(location, connectingOnSides)) {
                if (addedToNetwork == null) {
                    network.addNetworkingNode(location, connectingOnSides);
                    notifyUpdate(network);
                    addedToNetwork = network;
                } else {
                    addedToNetwork.mergeInNetwork(network);
                    notifyMerge(addedToNetwork, network);
                    networkIterator.remove();
                }
            }
        }

        // If it's not in any networks, create a new one
        if (addedToNetwork == null) {
            SimpleNetwork newNetwork = new SimpleNetwork();
            newNetwork.addNetworkingNode(location, connectingOnSides);
            networks.add(newNetwork);
            notifyAdd(newNetwork);
            addedToNetwork = newNetwork;
        }

        // Find all leaf nodes that it joins to its network
        for (Map.Entry<Vector3i, Byte> leafNode : leafNodes.entries()) {
            final Vector3i leafNodeLocation = leafNode.getKey();
            final byte leafNodeConnectingOnSides = leafNode.getValue();
            if (addedToNetwork.canAddLeafNode(leafNodeLocation, leafNodeConnectingOnSides))
                addedToNetwork.addLeafNode(leafNodeLocation, leafNodeConnectingOnSides);
        }
    }

    private void notifyAdd(SimpleNetwork network) {
        for (BlockNetworkTopologyListener listener : listeners)
            listener.networkAdded(network);
    }

    private void notifyUpdate(SimpleNetwork network) {
        for (BlockNetworkTopologyListener listener : listeners)
            listener.networkUpdated(network);
    }

    private void notifyRemove(SimpleNetwork network) {
        for (BlockNetworkTopologyListener listener : listeners)
            listener.networkRemoved(network);
    }

    private void notifyMerge(SimpleNetwork mainNetwork, SimpleNetwork mergedNetwork) {
        for (BlockNetworkTopologyListener listener : listeners)
            listener.networksMerged(mainNetwork, mergedNetwork);
    }

    private void notifySplit(SimpleNetwork mainNetwork, Collection<SimpleNetwork> resultNetworks) {
        for (BlockNetworkTopologyListener listener : listeners)
            listener.networkSplit(mainNetwork, resultNetworks);
    }

    public void addLeafBlock(Vector3i location, byte connectingOnSides) {
        for (SimpleNetwork network : networks) {
            if (network.canAddLeafNode(location, connectingOnSides)) {
                network.addLeafNode(location, connectingOnSides);
                notifyUpdate(network);
            }
        }

        // Check for new degenerated networks
        for (Map.Entry<Vector3i, Byte> leafNode : leafNodes.entries()) {
            final Vector3i leafLocation = leafNode.getKey();
            final byte leafConnectingOnSides = leafNode.getValue();
            if (SimpleNetwork.areNodesConnecting(location, connectingOnSides, leafLocation, leafConnectingOnSides)) {
                SimpleNetwork degenerateNetwork = SimpleNetwork.createDegenerateNetwork(location, connectingOnSides, leafLocation, leafConnectingOnSides);
                networks.add(degenerateNetwork);
                notifyAdd(degenerateNetwork);
            }
        }

        leafNodes.put(location, connectingOnSides);
    }

    public void updateNetworkingBlock(Vector3i location, byte connectingOnSides) {
        removeNetworkingBlock(location);
        addNetworkingBlock(location, connectingOnSides);
    }

    public void updateLeafBlock(Vector3i location, byte connectingOnSides) {
        removeLeafBlock(location, connectingOnSides);
        addLeafBlock(location, connectingOnSides);
    }

    public void removeNetworkingBlock(Vector3i location) {
        networkingNodes.remove(location);
        SimpleNetwork networkWithBlock = findNetworkWithNetworkingBlock(location);

        if (networkWithBlock == null)
            throw new IllegalStateException("Trying to remove a networking block that doesn't belong to any network");

        final Collection<SimpleNetwork> resultNetworks = networkWithBlock.removeNetworkingNode(location);
        if (resultNetworks != null) {
            if (resultNetworks.size() > 0)
                splitNetwork(networkWithBlock, resultNetworks);
            else {
                networks.remove(networkWithBlock);
                notifyRemove(networkWithBlock);
            }
        }
    }

    private void splitNetwork(SimpleNetwork splitNetwork, Collection<SimpleNetwork> splitResult) {
        networks.remove(splitNetwork);
        networks.addAll(splitResult);
        notifySplit(splitNetwork, splitResult);
    }

    public void removeLeafBlock(Vector3i location, byte connectingOnSides) {
        leafNodes.remove(location, connectingOnSides);
        final Iterator<SimpleNetwork> networkIterator = networks.iterator();
        while (networkIterator.hasNext()) {
            final SimpleNetwork network = networkIterator.next();
            if (network.hasLeafNode(location, connectingOnSides) && network.removeLeafNode(location, connectingOnSides)) {
                networkIterator.remove();
                notifyRemove(network);
            }
        }
    }

    public Collection<? extends Network> getNetworks() {
        return Collections.unmodifiableCollection(networks);
    }

    private SimpleNetwork findNetworkWithNetworkingBlock(Vector3i location) {
        for (SimpleNetwork network : networks) {
            if (network.hasNetworkingNode(location))
                return network;
        }
        return null;
    }
}
