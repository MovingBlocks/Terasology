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
        final Iterator<Network> networkIterator = networks.iterator();
        while (networkIterator.hasNext()) {
            final Network network = networkIterator.next();
            if (network.canAddBlock(location, connectingOnSides)) {
                if (addedToNetwork == null) {
                    network.addNetworkingBlock(location, connectingOnSides);
                    addedToNetwork = network;
                } else {
                    addedToNetwork.mergeInNetwork(network);
                    networkIterator.remove();
                }
            }
        }

        if (addedToNetwork == null) {
            Network newNetwork = new Network();
            newNetwork.addNetworkingBlock(location, connectingOnSides);
            networks.add(newNetwork);
        }
    }

    public void addLeafBlock(Vector3i location, Collection<Direction> connectingOnSides) {
        for (Network network : networks) {
            if (network.canAddBlock(location, connectingOnSides))
                network.addLeafBlock(location, connectingOnSides);
        }

        // Check for new degenerate networks
        for (Map.Entry<Vector3i, Collection<Direction>> leafNode : leafNodes.entrySet()){
            final Vector3i leafLocation = leafNode.getKey();
            final Collection<Direction> leafConnectingOnSides = leafNode.getValue();
            if (!networkingNodes.contains(leafLocation) && Network.areNodesConnecting(location, connectingOnSides, leafLocation, leafConnectingOnSides)) {
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

        final Collection<Network> resultNetworks = networkWithBlock.removeNetworkingBlock(location);
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
            if (network.hasLeafBlock(location) && network.removeLeafBlock(location))
                networkIterator.remove();
        }
    }

    public Collection<Network> getNetworks() {
        return Collections.unmodifiableCollection(networks);
    }

    private Network findNetworkWithNetworkingBlock(Vector3i location) {
        for (Network network : networks) {
            if (network.hasNetworkingBlock(location))
                return network;
        }
        return null;
    }
}
