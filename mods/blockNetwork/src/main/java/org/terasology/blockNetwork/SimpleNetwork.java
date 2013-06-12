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
    private SetMultimap<ImmutableBlockLocation, NetworkNode> networkingNodes = HashMultimap.create();
    private SetMultimap<ImmutableBlockLocation, NetworkNode> leafNodes = HashMultimap.create();

    // Distance cache
    private Map<TwoNetworkNodes, Integer> distanceCache = Maps.newHashMap();

    public static SimpleNetwork createDegenerateNetwork(
            NetworkNode networkNode1,
            NetworkNode networkNode2) {
        SimpleNetwork network = new SimpleNetwork();
        network.leafNodes.put(networkNode1.location, networkNode1);
        network.leafNodes.put(networkNode2.location, networkNode2);
        return network;
    }

    /**
     * Adds a networking node to the network.
     *
     * @param location          The location of the new node.
     * @param connectingOnSides Sides on which it can connect nodes.
     */
    public void addNetworkingNode(NetworkNode networkNode) {
        if (SANITY_CHECK && !canAddNetworkingNode(networkNode))
            throw new IllegalStateException("Unable to add this node to network");
        networkingNodes.put(networkNode.location, networkNode);
        distanceCache.clear();
    }

    /**
     * Adds a leaf node to the network.
     *
     * @param location          The location of the new node.
     * @param connectingOnSides Sides on which it can connect nodes.
     */
    public void addLeafNode(NetworkNode networkNode) {
        if (SANITY_CHECK && (!canAddLeafNode(networkNode) || isEmptyNetwork()))
            throw new IllegalStateException("Unable to add this node to network");
        leafNodes.put(networkNode.location, networkNode);
        distanceCache.clear();
    }

    /**
     * Returns the network size - a number of nodes it spans. If the same node is added twice with different
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
    public boolean removeLeafNode(NetworkNode networkingNode) {
        // Removal of a leaf node cannot split the network, so it's just safe to remove it
        // We just need to check, if after removal of the node, network becomes degenerated, if so - we need
        // to signal that the network is no longer valid and should be removed.
        final boolean changed = leafNodes.remove(networkingNode.location, networkingNode);
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

    public void removeNetworkingNode(NetworkNode networkNode) {
        if (!networkingNodes.remove(networkNode.location, networkNode))
            throw new IllegalStateException("Tried to remove a node that is not in the network");
        distanceCache.clear();
    }

    public Collection<NetworkNode> getNetworkingNodes() {
        return Collections.unmodifiableCollection(networkingNodes.values());
    }

    public Collection<NetworkNode> getLeafNodes() {
        return Collections.unmodifiableCollection(leafNodes.values());
    }

    public static boolean areNodesConnecting(NetworkNode node1, NetworkNode node2) {
        for (Direction direction : DirectionsUtil.getDirections(node1.connectionSides)) {
            final ImmutableBlockLocation possibleConnectedLocation = node1.location.move(direction);
            if (node2.location.equals(possibleConnectedLocation) && DirectionsUtil.hasDirection(node2.connectionSides, direction.reverse()))
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
    public boolean canAddNetworkingNode(NetworkNode networkNode) {
        if (isEmptyNetwork())
            return true;
        if (networkingNodes.containsValue(networkNode) || leafNodes.containsValue(networkNode))
            return false;
        if (canConnectToNetworkingNode(networkNode)) return true;
        return false;
    }

    public boolean canAddLeafNode(NetworkNode networkNode) {
        if (isEmptyNetwork())
            return false;
        if (networkingNodes.containsValue(networkNode) || leafNodes.containsValue(networkNode))
            return false;

        if (canConnectToNetworkingNode(networkNode)) return true;
        return false;
    }

    private boolean canConnectToNetworkingNode(NetworkNode networkNode) {
        for (Direction connectingOnSide : DirectionsUtil.getDirections(networkNode.connectionSides)) {
            final ImmutableBlockLocation possibleConnectionLocation = networkNode.location.move(connectingOnSide);
            for (NetworkNode possibleConnectedNode : networkingNodes.get(possibleConnectionLocation)) {
                if (DirectionsUtil.hasDirection(possibleConnectedNode.connectionSides, connectingOnSide.reverse()))
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasNetworkingNode(NetworkNode networkNode) {
        return networkingNodes.containsValue(networkNode);
    }

    @Override
    public boolean hasLeafNode(NetworkNode networkNode) {
        return leafNodes.containsValue(networkNode);
    }

    @Override
    public int getDistance(NetworkNode from, NetworkNode to) {
        if ((!hasNetworkingNode(from) && !hasLeafNode(from))
                || (!hasNetworkingNode(to) && !hasLeafNode(to)))
            throw new IllegalArgumentException("Cannot test nodes not in network");

        if (from.equals(to))
            return 0;

        if (SimpleNetwork.areNodesConnecting(from, to))
            return 1;

        // Breadth-first search of the network
        Set<NetworkNode> visitedNodes = Sets.newHashSet();
        visitedNodes.add(from);

        Set<NetworkNode> networkingNodesToTest = Sets.newHashSet();
        listConnectedNotVisitedNetworkingNodes(visitedNodes, from, networkingNodesToTest);
        int distanceSearched = 1;
        while (networkingNodesToTest.size()>0) {
            distanceSearched++;

            for (NetworkNode nodeToTest : networkingNodesToTest) {
                if (SimpleNetwork.areNodesConnecting(nodeToTest, to)) {
                    distanceCache.put(
                            new TwoNetworkNodes(from, to),
                            distanceSearched);
                    return distanceSearched;
                }
                visitedNodes.add(nodeToTest);
            }

            Set<NetworkNode> nextNetworkingNodesToTest = Sets.newHashSet();
            for (NetworkNode nodeToTest : networkingNodesToTest)
                listConnectedNotVisitedNetworkingNodes(visitedNodes, nodeToTest, nextNetworkingNodesToTest);

            networkingNodesToTest = nextNetworkingNodesToTest;
        }
        return -1;
    }

    @Override
    public boolean isInDistance(int distance, NetworkNode from, NetworkNode to) {
        if (distance < 0)
            throw new IllegalArgumentException("distance must be >= 0");
        if ((!hasNetworkingNode(from) && !hasLeafNode(from))
                || (!hasNetworkingNode(to) && !hasLeafNode(to)))
            throw new IllegalArgumentException("Cannot test nodes not in network");

        if (from.equals(to))
            return true;

        if (distance == 0)
            return false;

        if (SimpleNetwork.areNodesConnecting(from, to))
            return true;

        // Breadth-first search of the network
        Set<NetworkNode> visitedNodes = Sets.newHashSet();
        visitedNodes.add(from);

        Set<NetworkNode> networkingNodesToTest = Sets.newHashSet();
        listConnectedNotVisitedNetworkingNodes(visitedNodes, from, networkingNodesToTest);
        int distanceSearched = 1;
        while (distanceSearched < distance) {
            distanceSearched++;

            for (NetworkNode nodeToTest : networkingNodesToTest) {
                if (SimpleNetwork.areNodesConnecting(nodeToTest, to)) {
                    distanceCache.put(
                            new TwoNetworkNodes(from, to),
                            distanceSearched);
                    return true;
                }
                visitedNodes.add(nodeToTest);
            }

            Set<NetworkNode> nextNetworkingNodesToTest = Sets.newHashSet();
            for (NetworkNode nodeToTest : networkingNodesToTest)
                listConnectedNotVisitedNetworkingNodes(visitedNodes, nodeToTest, nextNetworkingNodesToTest);

            networkingNodesToTest = nextNetworkingNodesToTest;
        }

        return false;
    }

//    public Map<Vector3i, Byte> getConnectedNodes(Vector3i location, byte connectionSides) {
//        Map<Vector3i, Byte> result = Maps.newHashMap();
//        for (Direction connectingOnSide : DirectionsUtil.getDirections(connectionSides)) {
//            final Vector3i possibleNodeLocation = new Vector3i(location);
//            possibleNodeLocation.add(connectingOnSide.getVector3i());
//
//            final Byte directionsForNodeOnThatSide = networkingNodes.get(possibleNodeLocation);
//            if (directionsForNodeOnThatSide != null && DirectionsUtil.hasDirection(directionsForNodeOnThatSide, connectingOnSide.reverse()))
//                result.put(possibleNodeLocation, directionsForNodeOnThatSide);
//
//            for (byte directionsForLeafNodeOnThatSide : leafNodes.get(possibleNodeLocation)) {
//                if (DirectionsUtil.hasDirection(directionsForLeafNodeOnThatSide, connectingOnSide.reverse()))
//                    result.put(possibleNodeLocation, directionsForLeafNodeOnThatSide);
//            }
//        }
//        return result;
//    }

    private void listConnectedNotVisitedNetworkingNodes(Set<NetworkNode> visitedNodes, NetworkNode location, Collection<NetworkNode> result) {
        for (Direction connectingOnSide : DirectionsUtil.getDirections(location.connectionSides)) {
            final ImmutableBlockLocation possibleConnectionLocation = location.location.move(connectingOnSide);
            for (NetworkNode possibleConnection : networkingNodes.get(possibleConnectionLocation)) {
                if (!visitedNodes.contains(possibleConnection) && DirectionsUtil.hasDirection(possibleConnection.connectionSides, connectingOnSide.reverse()))
                    result.add(possibleConnection);
            }
        }
    }

    private boolean isDegeneratedNetwork() {
        return networkingNodes.isEmpty() && leafNodes.size() == 1;
    }

    private boolean isEmptyNetwork() {
        return networkingNodes.isEmpty() && leafNodes.isEmpty();
    }
}