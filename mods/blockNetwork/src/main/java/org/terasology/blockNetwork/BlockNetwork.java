package org.terasology.blockNetwork;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.math.Direction;
import org.terasology.math.Vector3i;

import java.util.*;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BlockNetwork {
    private Set<Network> networks = Sets.newHashSet();
    private Map<Vector3i, Collection<Direction>> leafNodes = Maps.newHashMap();
    private Set<Vector3i> networkingNodes = Sets.newHashSet();

    public void addNetworkingBlock(Vector3i location, Collection<Direction> connectingOnSides) {
        networkingNodes.add(location);
        Network addedToNetwork = null;

        // Try adding to existing networks
        final Iterator<Network> networkIterator = networks.iterator();
        while (networkIterator.hasNext()) {
            final Network network = networkIterator.next();
            if (network.canAddNode(location, connectingOnSides)) {
                if (addedToNetwork == null) {
                    network.addNetworkingNode(location, connectingOnSides);
                    addedToNetwork = network;
                } else {
                    addedToNetwork.mergeInNetwork(network);
                    networkIterator.remove();
                }
            }
        }

        // If it's not in any networks, create a new one
        if (addedToNetwork == null) {
            Network newNetwork = new Network();
            newNetwork.addNetworkingNode(location, connectingOnSides);
            networks.add(newNetwork);
            addedToNetwork = newNetwork;
        }

        // Find all leaf nodes that it joins to its network
        for (Map.Entry<Vector3i, Collection<Direction>> leafNode: leafNodes.entrySet()){
            final Vector3i leafNodeLocation = leafNode.getKey();
            final Collection<Direction> leafNodeConnectingOnSides = leafNode.getValue();
            if (addedToNetwork.canAddNode(leafNodeLocation, leafNodeConnectingOnSides))
                addedToNetwork.addLeafNode(leafNodeLocation, leafNodeConnectingOnSides);
        }
    }

    public void addLeafBlock(Vector3i location, Collection<Direction> connectingOnSides) {
        for (Network network : networks) {
            if (network.canAddNode(location, connectingOnSides))
                network.addLeafNode(location, connectingOnSides);
        }

        // Check for new degenerated networks
        for (Map.Entry<Vector3i, Collection<Direction>> leafNode : leafNodes.entrySet()){
            final Vector3i leafLocation = leafNode.getKey();
            final Collection<Direction> leafConnectingOnSides = leafNode.getValue();
            if (Network.areNodesConnecting(location, connectingOnSides, leafLocation, leafConnectingOnSides)) {
                Network degenerateNetwork = Network.createDegenerateNetwork(location, connectingOnSides, leafLocation, leafConnectingOnSides);
                networks.add(degenerateNetwork);
            }
        }

        leafNodes.put(location, connectingOnSides);
    }

    public void updateNetworkingBlock(Vector3i location, Collection<Direction> connectingOnSides) {
        removeNetworkingBlock(location);
        addNetworkingBlock(location, connectingOnSides);
    }

    public void updateLeafBlock(Vector3i location, Collection<Direction> connectingOnSides) {
        removeLeafBlock(location);
        addLeafBlock(location, connectingOnSides);
    }

    public void removeNetworkingBlock(Vector3i location) {
        networkingNodes.remove(location);
        Network networkWithBlock = findNetworkWithNetworkingBlock(location);

        if (networkWithBlock == null)
            throw new IllegalStateException("Trying to remove a networking block that doesn't belong to any network");

        final Collection<Network> resultNetworks = networkWithBlock.removeNetworkingNode(location);
        if (resultNetworks != null) {
            networks.remove(networkWithBlock);
            networks.addAll(resultNetworks);
        }
    }

    public void removeLeafBlock(Vector3i location) {
        leafNodes.remove(location);
        final Iterator<Network> networkIterator = networks.iterator();
        while (networkIterator.hasNext()) {
            final Network network = networkIterator.next();
            if (network.hasLeafNode(location) && network.removeLeafNode(location))
                networkIterator.remove();
        }
    }

    public Collection<Network> getNetworks() {
        return Collections.unmodifiableCollection(networks);
    }

    private Network findNetworkWithNetworkingBlock(Vector3i location) {
        for (Network network : networks) {
            if (network.hasNetworkingNode(location))
                return network;
        }
        return null;
    }
}
