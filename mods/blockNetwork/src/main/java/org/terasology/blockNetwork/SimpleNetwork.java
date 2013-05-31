package org.terasology.blockNetwork;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
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
public class SimpleNetwork implements Network {
    private static final boolean SANITY_CHECK = true;
    private Map<Vector3i, Byte> networkingNodes = Maps.newHashMap();
    private Multimap<Vector3i, Byte> leafNodes = HashMultimap.create();

    public static SimpleNetwork createDegenerateNetwork(
            Vector3i location1, byte connectingOnSides1,
            Vector3i location2, byte connectingOnSides2) {
        SimpleNetwork network = new SimpleNetwork();
        network.leafNodes.put(location1, connectingOnSides1);
        network.leafNodes.put(location2, connectingOnSides2);
        return network;
    }

    /**
     * Adds a networking node to the network.
     *
     * @param location          The location of the new node.
     * @param connectingOnSides Sides on which it can connect nodes.
     */
    public void addNetworkingNode(Vector3i location, byte connectingOnSides) {
        if (SANITY_CHECK && !canAddNetworkingNode(location, connectingOnSides))
            throw new IllegalStateException("Unable to add this node to network");
        networkingNodes.put(new Vector3i(location), connectingOnSides);
    }

    /**
     * Adds a leaf node to the network.
     *
     * @param location          The location of the new node.
     * @param connectingOnSides Sides on which it can connect nodes.
     */
    public void addLeafNode(Vector3i location, byte connectingOnSides) {
        if (SANITY_CHECK && (!canAddLeafNode(location, connectingOnSides) || isEmptyNetwork()))
            throw new IllegalStateException("Unable to add this node to network");
        leafNodes.put(new Vector3i(location), connectingOnSides);
    }

    /**
     * Returns the network size - a number of nodes it spans. If the same leaf node is added twice with different
     * connecting sides, it is counted twice.
     *
     * @return
     */
    @Override
    public int getNetworkSize() {
        return networkingNodes.size() + leafNodes.size();
    }

    /**
     * Removes a leaf node from the network. If this was the last node in the network, <code>true</code> is returned
     * to signify that this network is now empty.
     *
     * @param location
     * @param connectingOnSides
     */
    public boolean removeLeafNode(Vector3i location, byte connectingOnSides) {
        // Removal of a leaf node cannot split the network, so it's just safe to remove it
        // We just need to check, if after removal of the node, network becomes degenerated, if so - we need
        // to signal that the network is no longer valid and should be removed.
        final boolean changed = leafNodes.remove(location, connectingOnSides);
        if (!changed)
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
    public Collection<SimpleNetwork> removeNetworkingNode(Vector3i location) {
        // Removal of a node can split the network if it is connected to a node on at least two sides, and there is no
        // other connection between the neighbouring nodes
        Byte directions = networkingNodes.remove(location);
        if (directions == null)
            throw new IllegalStateException("Tried to remove a node that is not in the network");

        // TODO Do something smarter, at least check if the removed node is connected to at least two other nodes
        // For now, just naively rebuild the whole network after removal
        Set<SimpleNetwork> resultNetworks = Sets.newHashSet();

        // Setup all back-bones for networks
        for (Map.Entry<Vector3i, Byte> networkingNode : networkingNodes.entrySet()) {
            SimpleNetwork networkAddedTo = null;

            final Vector3i networkingNodeLocation = networkingNode.getKey();
            final byte networkingNodeConnectingSides = networkingNode.getValue();
            final Iterator<SimpleNetwork> resultNetworksIterator = resultNetworks.iterator();
            while (resultNetworksIterator.hasNext()) {
                final SimpleNetwork resultNetwork = resultNetworksIterator.next();
                if (resultNetwork.canAddNetworkingNode(networkingNodeLocation, networkingNodeConnectingSides)) {
                    if (networkAddedTo == null) {
                        resultNetwork.addNetworkingNode(networkingNodeLocation, networkingNodeConnectingSides);
                        networkAddedTo = resultNetwork;
                    } else {
                        networkAddedTo.mergeInNetwork(resultNetwork);
                        resultNetworksIterator.remove();
                    }
                }
            }

            if (networkAddedTo == null) {
                SimpleNetwork newNetwork = new SimpleNetwork();
                newNetwork.addNetworkingNode(networkingNodeLocation, networkingNodeConnectingSides);
                resultNetworks.add(newNetwork);
            }
        }

        // We have all connections for the resulting networks, now add leaves
        for (Map.Entry<Vector3i, Byte> leafNode : leafNodes.entries()) {
            final Vector3i leafNodeLocation = leafNode.getKey();
            final byte leafNodeConnectingSides = leafNode.getValue();

            for (SimpleNetwork resultNetwork : resultNetworks) {
                if (resultNetwork.canAddLeafNode(leafNodeLocation, leafNodeConnectingSides))
                    resultNetwork.addLeafNode(leafNodeLocation, leafNodeConnectingSides);
            }
        }

        if (resultNetworks.size() == 1)
            return null;

        return resultNetworks;
    }

    public static boolean areNodesConnecting(Vector3i location1, byte directions1, Vector3i location2, byte directions2) {
        for (Direction direction : DirectionsUtil.getDirections(directions1)) {
            Vector3i neighbour = new Vector3i(location1);
            neighbour.add(direction.getVector3i());
            if (location2.equals(neighbour) && DirectionsUtil.hasDirection(directions2, direction.reverse()))
                return true;
        }
        return false;
    }

    /**
     * Merges the specified network into this one.
     *
     * @param network
     */
    public void mergeInNetwork(SimpleNetwork network) {
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
    public boolean canAddNetworkingNode(Vector3i location, byte connectingOnSides) {
        if (isEmptyNetwork())
            return true;
        if (networkingNodes.containsKey(location) || leafNodes.containsKey(location))
            return false;
        for (Direction connectingOnSide : DirectionsUtil.getDirections(connectingOnSides)) {
            final Vector3i possibleNodeLocation = new Vector3i(location);
            possibleNodeLocation.add(connectingOnSide.getVector3i());
            final Byte directionsForNodeOnThatSide = networkingNodes.get(possibleNodeLocation);
            if (directionsForNodeOnThatSide != null && DirectionsUtil.hasDirection(directionsForNodeOnThatSide, connectingOnSide.reverse()))
                return true;
        }
        return false;
    }

    public boolean canAddLeafNode(Vector3i location, byte connectingOnSides) {
        if (isEmptyNetwork())
            return false;
        if (networkingNodes.containsKey(location) || leafNodes.containsEntry(location, connectingOnSides))
            return false;
        
        for (Direction connectingOnSide : DirectionsUtil.getDirections(connectingOnSides)) {
            final Vector3i possibleNodeLocation = new Vector3i(location);
            possibleNodeLocation.add(connectingOnSide.getVector3i());
            final Byte directionsForNodeOnThatSide = networkingNodes.get(possibleNodeLocation);
            if (directionsForNodeOnThatSide != null && DirectionsUtil.hasDirection(directionsForNodeOnThatSide, connectingOnSide.reverse()))
                return true;
        }
        return false;
    }

    @Override
    public boolean hasNetworkingNode(Vector3i location) {
        return networkingNodes.containsKey(location);
    }

    @Override
    public boolean hasLeafNode(Vector3i location, byte connectingOnSides) {
        return leafNodes.containsEntry(location, connectingOnSides);
    }

    private boolean isDegeneratedNetwork() {
        return networkingNodes.isEmpty() && leafNodes.size() == 1;
    }

    private boolean isEmptyNetwork() {
        return networkingNodes.isEmpty() && leafNodes.isEmpty();
    }
}