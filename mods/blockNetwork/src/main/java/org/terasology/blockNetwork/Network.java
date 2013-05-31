package org.terasology.blockNetwork;

import org.terasology.math.Direction;

import java.util.*;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.math.Vector3i;

/**
 * Represents one network of nodes, where each nodes is somehow connected to another within the network.
 * <p/>
 * Network contains following node types:
 * - networking nodes - nodes that are a back-bone of a network. These allow to connect multiple nodes in the network.
 * A networking node "conducts" the "signal" of the network to nodes defined in the "connectingOnSides" nodes in its
 * vicinity.
 * - leaf nodes - nodes that are only receiving or producing a signal, and do not themselves "conduct" it to other nodes.
 * <p/>
 * A couple of non-obvious facts:
 * 1. The same node (defined as location) cannot be both a networking node and a leaf node in the same network.
 * 2. The same leaf node can be a member of multiple disjunctive networks (different network on each side).
 * 3. A valid network can have no networking nodes at all, and exactly two leaf nodes (neighbouring leaf nodes).
 *
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class Network {
    private static final boolean SANITY_CHECK = true;
    private Map<Vector3i, Byte> networkingNodes = Maps.newHashMap();
    private Map<Vector3i, Byte> leafNodes = Maps.newHashMap();

    public static Network createDegenerateNetwork(
            Vector3i location1, Collection<Direction> connectingOnSides1,
            Vector3i location2, Collection<Direction> connectingOnSides2) {
        Network network = new Network();
        network.leafNodes.put(location1, DirectionsUtil.getDirections(connectingOnSides1));
        network.leafNodes.put(location2, DirectionsUtil.getDirections(connectingOnSides2));
        return network;
    }

    /**
     * Adds a networking node to the network.
     *
     * @param location          The location of the new node.
     * @param connectingOnSides Sides on which it can connect nodes.
     */
    public void addNetworkingNode(Vector3i location, Collection<Direction> connectingOnSides) {
        if (SANITY_CHECK && !canAddNode(location, connectingOnSides))
            throw new IllegalStateException("Unable to add this node to network");
        networkingNodes.put(new Vector3i(location), DirectionsUtil.getDirections(connectingOnSides));
    }

    /**
     * Adds a leaf node to the network.
     *
     * @param location          The location of the new node.
     * @param connectingOnSides Sides on which it can connect nodes.
     */
    public void addLeafNode(Vector3i location, Collection<Direction> connectingOnSides) {
        if (SANITY_CHECK && (!canAddNode(location, connectingOnSides) || isEmptyNetwork()))
            throw new IllegalStateException("Unable to add this node to network");
        leafNodes.put(new Vector3i(location), DirectionsUtil.getDirections(connectingOnSides));
    }

    /**
     * Returns the network size - a number of nodes it spans.
     * it is counted once only.
     *
     * @return
     */
    public int getNetworkSize() {
        return networkingNodes.size() + leafNodes.size();
    }

    /**
     * Removes a leaf node from the network. If this was the last node in the network, <code>true</code> is returned
     * to signify that this network is now empty.
     *
     * @param location
     */
    public boolean removeLeafNode(Vector3i location) {
        // Removal of a leaf node cannot split the network, so it's just safe to remove it
        // We just need to check, if after removal of the node, network becomes degenerated, if so - we need
        // to signal that the network is no longer valid and should be removed.
        final Byte removedDirections = leafNodes.remove(location);
        if (removedDirections == null)
            throw new IllegalStateException("Tried to remove a node that is not in the network");

        if (isDegeneratedNetwork())
            return true;

        return isEmptyNetwork();
    }

    /**
     * Remove networking node from the network. If this removal splits the network, the returned Set will contain all
     * the Networks this Network should be replaced with. If it does not split it, this method will return <code>null</code>.
     *
     * @param location Location of the removed node.
     * @return If not <code>null</code>, current network should be replaced with the returned networks. If an empty
     *         Collection is returned, the original network should be removed.
     */
    public Collection<Network> removeNetworkingNode(Vector3i location) {
        // Removal of a node can split the network if it is connected to a node on at least two sides, and there is no
        // other connection between the neighbouring nodes
        Byte directions = networkingNodes.remove(location);
        if (directions == null)
            throw new IllegalStateException("Tried to remove a node that is not in the network");

        // TODO Do something smarter, at least check if the removed node is connected to at least two other nodes
        // For now, just naively rebuild the whole network after removal
        Set<Network> resultNetworks = Sets.newHashSet();

        // Setup all back-bones for networks
        for (Map.Entry<Vector3i, Byte> networkingNode : networkingNodes.entrySet()) {
            Network networkAddedTo = null;

            final Vector3i networkingNodeLocation = networkingNode.getKey();
            final byte networkingNodeConnectingSides = networkingNode.getValue();
            final Collection<Direction> networkingNodeDirections = DirectionsUtil.getDirections(networkingNodeConnectingSides);
            final Iterator<Network> resultNetworksIterator = resultNetworks.iterator();
            while (resultNetworksIterator.hasNext()) {
                final Network resultNetwork = resultNetworksIterator.next();
                if (resultNetwork.canAddNode(networkingNodeLocation, networkingNodeDirections)) {
                    if (networkAddedTo == null) {
                        resultNetwork.addNetworkingNode(networkingNodeLocation, networkingNodeDirections);
                        networkAddedTo = resultNetwork;
                    } else {
                        networkAddedTo.mergeInNetwork(resultNetwork);
                        resultNetworksIterator.remove();
                    }
                }
            }

            if (networkAddedTo == null) {
                Network newNetwork = new Network();
                newNetwork.addNetworkingNode(networkingNodeLocation, networkingNodeDirections);
                resultNetworks.add(newNetwork);
            }
        }

        // We have all connections for the resulting networks, now add leaves
        for (Map.Entry<Vector3i, Byte> leafNode : leafNodes.entrySet()) {
            final Vector3i leafNodeLocation = leafNode.getKey();
            final Collection<Direction> leafNodeDirections = DirectionsUtil.getDirections(leafNode.getValue());

            for (Network resultNetwork : resultNetworks) {
                if (resultNetwork.canAddNode(leafNodeLocation, leafNodeDirections))
                    resultNetwork.addLeafNode(leafNodeLocation, leafNodeDirections);
            }
        }

        if (resultNetworks.size() == 1)
            return null;

        return resultNetworks;
    }

    public static boolean areNodesConnecting(Vector3i location1, Collection<Direction> directions1, Vector3i location2, Collection<Direction> directions2) {
        for (Direction direction : directions1) {
            Vector3i neighbour = new Vector3i(location1);
            neighbour.add(direction.getVector3i());
            if (location2.equals(neighbour) && directions2.contains(direction.reverse()))
                return true;
        }
        return false;
    }

    /**
     * Merges the specified network into this one.
     *
     * @param network
     */
    public void mergeInNetwork(Network network) {
        networkingNodes.putAll(network.networkingNodes);
        leafNodes.putAll(network.leafNodes);
    }

    /**
     * If this network can connect to node at the location specified with the specified connecting sides.
     *
     * @param location
     * @param connectingOnSides
     * @return
     */
    public boolean canAddNode(Vector3i location, Collection<Direction> connectingOnSides) {
        if (isEmptyNetwork())
            return true;
        if (networkingNodes.containsKey(location))
            return false;
        for (Direction connectingOnSide : connectingOnSides) {
            final Vector3i possibleNodeLocation = new Vector3i(location);
            possibleNodeLocation.add(connectingOnSide.getVector3i());
            final Byte directionsForNodeOnThatSide = networkingNodes.get(possibleNodeLocation);
            if (directionsForNodeOnThatSide != null && DirectionsUtil.hasDirection(directionsForNodeOnThatSide, connectingOnSide.reverse()))
                return true;
        }
        return false;
    }

    public boolean hasNetworkingNode(Vector3i location) {
        return networkingNodes.containsKey(location);
    }

    public boolean hasLeafNode(Vector3i location) {
        return leafNodes.containsKey(location);
    }

    private boolean isDegeneratedNetwork() {
        return networkingNodes.isEmpty() && leafNodes.size() == 1;
    }

    private boolean isEmptyNetwork() {
        return networkingNodes.isEmpty() && leafNodes.isEmpty();
    }
}