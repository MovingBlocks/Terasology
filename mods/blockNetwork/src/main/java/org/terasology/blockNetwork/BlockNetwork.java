package org.terasology.blockNetwork;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.math.Vector3i;

import java.util.*;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BlockNetwork {
    private Set<Network> networks = Sets.newHashSet();
    private Map<Vector3i, Byte> leafNodes = Maps.newHashMap();
    private Set<Vector3i> networkingNodes = Sets.newHashSet();

    private Map<Network, Collection<Network>> networkSplits = Maps.newHashMap();
    private Map<Network, Collection<Network>> networkMerges = Maps.newHashMap();
    private Set<Network> networkDeletes = Sets.newHashSet();
    private Set<Network> networkAdds = Sets.newHashSet();
    private Set<Network> networkUpdates = Sets.newHashSet();

    public void addNetworkingBlock(Vector3i location, byte connectingOnSides) {
        networkingNodes.add(location);
        Network addedToNetwork = null;

        // Try adding to existing networks
        final Iterator<Network> networkIterator = networks.iterator();
        while (networkIterator.hasNext()) {
            final Network network = networkIterator.next();
            if (network.canAddNode(location, connectingOnSides)) {
                if (addedToNetwork == null) {
                    network.addNetworkingNode(location, connectingOnSides);
                    networkUpdates.add(network);
                    addedToNetwork = network;
                } else {
                    mergeNetwork(addedToNetwork, network);
                    networkIterator.remove();
                }
            }
        }

        // If it's not in any networks, create a new one
        if (addedToNetwork == null) {
            Network newNetwork = new Network();
            newNetwork.addNetworkingNode(location, connectingOnSides);
            networks.add(newNetwork);
            networkAdds.add(newNetwork);
            addedToNetwork = newNetwork;
        }

        // Find all leaf nodes that it joins to its network
        for (Map.Entry<Vector3i, Byte> leafNode : leafNodes.entrySet()) {
            final Vector3i leafNodeLocation = leafNode.getKey();
            final byte leafNodeConnectingOnSides = leafNode.getValue();
            if (addedToNetwork.canAddNode(leafNodeLocation, leafNodeConnectingOnSides))
                addedToNetwork.addLeafNode(leafNodeLocation, leafNodeConnectingOnSides);
        }
    }

    private void mergeNetwork(Network mainNetwork, Network mergedNetwork) {
        mainNetwork.mergeInNetwork(mergedNetwork);
        Collection<Network> mainNetworkMerges = networkMerges.get(mainNetwork);
        if (mainNetworkMerges == null) {
            mainNetworkMerges = Sets.newHashSet();
            networkMerges.put(mainNetwork, mainNetworkMerges);
        }
        mainNetworkMerges.add(mergedNetwork);
    }

    public void addLeafBlock(Vector3i location, byte connectingOnSides) {
        for (Network network : networks) {
            if (network.canAddNode(location, connectingOnSides)) {
                network.addLeafNode(location, connectingOnSides);
                networkUpdates.add(network);
            }
        }

        // Check for new degenerated networks
        for (Map.Entry<Vector3i, Byte> leafNode : leafNodes.entrySet()) {
            final Vector3i leafLocation = leafNode.getKey();
            final byte leafConnectingOnSides = leafNode.getValue();
            if (Network.areNodesConnecting(location, connectingOnSides, leafLocation, leafConnectingOnSides)) {
                Network degenerateNetwork = Network.createDegenerateNetwork(location, connectingOnSides, leafLocation, leafConnectingOnSides);
                networks.add(degenerateNetwork);
                networkAdds.add(degenerateNetwork);
            }
        }

        leafNodes.put(location, connectingOnSides);
    }

    public void updateNetworkingBlock(Vector3i location, byte connectingOnSides) {
        removeNetworkingBlock(location);
        addNetworkingBlock(location, connectingOnSides);
    }

    public void updateLeafBlock(Vector3i location, byte connectingOnSides) {
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
            if (resultNetworks.size() > 0)
                splitNetwork(networkWithBlock, resultNetworks);
            else {
                networks.remove(networkWithBlock);
                networkDeletes.add(networkWithBlock);
            }
        }
    }

    private void splitNetwork(Network splitNetwork, Collection<Network> splitResult) {
        networks.remove(splitNetwork);
        networks.addAll(splitResult);
        Collection<Network> networkSplitNetworks = networkSplits.get(splitNetwork);
        if (networkSplitNetworks == null) {
            networkSplitNetworks = Sets.newHashSet();
            networkSplits.put(splitNetwork, networkSplitNetworks);
        }
        networkSplitNetworks.addAll(splitResult);
    }

    public void removeLeafBlock(Vector3i location) {
        leafNodes.remove(location);
        final Iterator<Network> networkIterator = networks.iterator();
        while (networkIterator.hasNext()) {
            final Network network = networkIterator.next();
            if (network.hasLeafNode(location) && network.removeLeafNode(location)) {
                networkIterator.remove();
                networkDeletes.add(network);
            }
        }
    }

    public Collection<Network> getNetworks() {
        return Collections.unmodifiableCollection(networks);
    }

    public BlockNetworkTopologyChanges consumeNetworksTopologyChanges() {
        if (networkAdds.isEmpty() && networkUpdates.isEmpty() && networkDeletes.isEmpty()
                && networkSplits.isEmpty() && networkMerges.isEmpty())
            return null;

        BlockNetworkTopologyChanges changes = new BlockNetworkTopologyChanges(networkAdds, networkUpdates, networkDeletes,
                networkMerges, networkSplits);

        networkAdds = Sets.newHashSet();
        networkUpdates = Sets.newHashSet();
        networkDeletes = Sets.newHashSet();
        networkMerges = Maps.newHashMap();
        networkSplits = Maps.newHashMap();

        return changes;
    }

    private Network findNetworkWithNetworkingBlock(Vector3i location) {
        for (Network network : networks) {
            if (network.hasNetworkingNode(location))
                return network;
        }
        return null;
    }
}
