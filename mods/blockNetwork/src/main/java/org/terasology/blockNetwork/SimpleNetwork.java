package org.terasology.blockNetwork;

import org.terasology.math.Direction;

import java.util.*;

import com.google.common.collect.*;
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
    private static final boolean SANITY_CHECK = false;
    private Map<Vector3i, Byte> networkingNodes = Maps.newHashMap();
    private Multimap<Vector3i, Byte> leafNodes = HashMultimap.create();

    // Distance cache
    private Map<TwoNetworkNodes, Integer> distanceCache = Maps.newHashMap();

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
        distanceCache.clear();
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
        distanceCache.clear();
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
     * Removes a leaf node from the network. If this removal made the network degenerate, it will return <code>true</code>.
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

        distanceCache.clear();

        if (isDegeneratedNetwork())
            return true;

        return isEmptyNetwork();
    }

    public void removeAllLeafNodes() {
        leafNodes.clear();
        distanceCache.clear();
    }

    public void removeAllNetworkingNodes() {
        networkingNodes.clear();
        distanceCache.clear();
    }

    public void removeNetworkingNode(Vector3i location, byte connectingOnSides) {
        if (networkingNodes.remove(location) == null)
            throw new IllegalStateException("Tried to remove a node that is not in the network");
        distanceCache.clear();
    }

    public Map<Vector3i, Byte> getNetworkingNodes() {
        return Collections.unmodifiableMap(networkingNodes);
    }

    public Multimap<Vector3i, Byte> getLeafNodes() {
        return Multimaps.unmodifiableMultimap(leafNodes);
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

    @Override
    public int getDistance(Vector3i from, byte fromConnectionSides, Vector3i to, byte toConnectionSides) {
        if ((!hasNetworkingNode(from) && !hasLeafNode(from, fromConnectionSides))
                || (!hasNetworkingNode(to) && !hasLeafNode(to, toConnectionSides)))
            throw new IllegalArgumentException("Cannot test nodes not in network");

        if (from.equals(to) && fromConnectionSides == toConnectionSides)
            return 0;

        if (SimpleNetwork.areNodesConnecting(from, fromConnectionSides, to, toConnectionSides))
            return 1;

        // Breadth-first search of the network
        Set<Vector3i> visitedNodes = Sets.newHashSet();
        visitedNodes.add(from);

        Map<Vector3i, Byte> networkingNodesToTest = Maps.newHashMap();
        listConnectedNotVisitedNetworkingNodes(visitedNodes, from, fromConnectionSides, networkingNodesToTest);
        int distanceSearched = 1;
        while (networkingNodesToTest.size()>0) {
            distanceSearched++;

            for (Map.Entry<Vector3i, Byte> nodeToTest : networkingNodesToTest.entrySet()) {
                if (SimpleNetwork.areNodesConnecting(nodeToTest.getKey(), nodeToTest.getValue(), to, toConnectionSides)) {
                    distanceCache.put(
                            new TwoNetworkNodes(
                                    new NetworkNode(from, fromConnectionSides), new NetworkNode(to, toConnectionSides)),
                            distanceSearched);
                    return distanceSearched;
                }
                visitedNodes.add(nodeToTest.getKey());
            }

            Map<Vector3i, Byte> nextNetworkingNodesToTest = Maps.newHashMap();
            for (Map.Entry<Vector3i, Byte> nodeToTest : networkingNodesToTest.entrySet())
                listConnectedNotVisitedNetworkingNodes(visitedNodes, nodeToTest.getKey(), nodeToTest.getValue(), nextNetworkingNodesToTest);

            networkingNodesToTest = nextNetworkingNodesToTest;
        }
        return -1;
    }

    @Override
    public boolean isInDistance(int distance, Vector3i from, byte fromConnectionSides, Vector3i to, byte toConnectionSides) {
        if (distance < 0)
            throw new IllegalArgumentException("distance must be >= 0");
        if ((!hasNetworkingNode(from) && !hasLeafNode(from, fromConnectionSides))
                || (!hasNetworkingNode(to) && !hasLeafNode(to, toConnectionSides)))
            throw new IllegalArgumentException("Cannot test nodes not in network");

        if (from.equals(to) && fromConnectionSides == toConnectionSides)
            return true;

        if (distance == 0)
            return false;

        if (SimpleNetwork.areNodesConnecting(from, fromConnectionSides, to, toConnectionSides))
            return true;

        // Breadth-first search of the network
        Set<Vector3i> visitedNodes = Sets.newHashSet();
        visitedNodes.add(from);

        Map<Vector3i, Byte> networkingNodesToTest = Maps.newHashMap();
        listConnectedNotVisitedNetworkingNodes(visitedNodes, from, fromConnectionSides, networkingNodesToTest);
        int distanceSearched = 1;
        while (distanceSearched < distance) {
            distanceSearched++;

            for (Map.Entry<Vector3i, Byte> nodeToTest : networkingNodesToTest.entrySet()) {
                if (SimpleNetwork.areNodesConnecting(nodeToTest.getKey(), nodeToTest.getValue(), to, toConnectionSides)) {
                    distanceCache.put(
                            new TwoNetworkNodes(
                                    new NetworkNode(from, fromConnectionSides), new NetworkNode(to, toConnectionSides)),
                            distanceSearched);
                    return true;
                }
                visitedNodes.add(nodeToTest.getKey());
            }

            Map<Vector3i, Byte> nextNetworkingNodesToTest = Maps.newHashMap();
            for (Map.Entry<Vector3i, Byte> nodeToTest : networkingNodesToTest.entrySet())
                listConnectedNotVisitedNetworkingNodes(visitedNodes, nodeToTest.getKey(), nodeToTest.getValue(), nextNetworkingNodesToTest);

            networkingNodesToTest = nextNetworkingNodesToTest;
        }

        return false;
    }

    public Map<Vector3i, Byte> getConnectedNodes(Vector3i location, byte connectionSides) {
        Map<Vector3i, Byte> result = Maps.newHashMap();
        for (Direction connectingOnSide : DirectionsUtil.getDirections(connectionSides)) {
            final Vector3i possibleNodeLocation = new Vector3i(location);
            possibleNodeLocation.add(connectingOnSide.getVector3i());

            final Byte directionsForNodeOnThatSide = networkingNodes.get(possibleNodeLocation);
            if (directionsForNodeOnThatSide != null && DirectionsUtil.hasDirection(directionsForNodeOnThatSide, connectingOnSide.reverse()))
                result.put(possibleNodeLocation, directionsForNodeOnThatSide);
            
            for (byte directionsForLeafNodeOnThatSide : leafNodes.get(possibleNodeLocation)) {
                if (DirectionsUtil.hasDirection(directionsForLeafNodeOnThatSide, connectingOnSide.reverse()))
                    result.put(possibleNodeLocation, directionsForLeafNodeOnThatSide);
            }
        }
        return result;
    }

    private void listConnectedNotVisitedNetworkingNodes(Set<Vector3i> visitedNodes, Vector3i location, byte connectionSides, Map<Vector3i, Byte> result) {
        for (Direction connectingOnSide : DirectionsUtil.getDirections(connectionSides)) {
            final Vector3i possibleNodeLocation = new Vector3i(location);
            possibleNodeLocation.add(connectingOnSide.getVector3i());
            final Byte directionsForNodeOnThatSide = networkingNodes.get(possibleNodeLocation);
            if (directionsForNodeOnThatSide != null && !visitedNodes.contains(possibleNodeLocation)
                    && DirectionsUtil.hasDirection(directionsForNodeOnThatSide, connectingOnSide.reverse()))
                result.put(possibleNodeLocation, directionsForNodeOnThatSide);
        }
    }

    private boolean isDegeneratedNetwork() {
        return networkingNodes.isEmpty() && leafNodes.size() == 1;
    }

    private boolean isEmptyNetwork() {
        return networkingNodes.isEmpty() && leafNodes.isEmpty();
    }
}