package org.terasology.blockNetwork;

import java.util.*;

import com.google.common.collect.*;
import org.terasology.math.Side;
import org.terasology.math.SideBitFlag;
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
        if (!areNodesConnecting(networkNode1, networkNode2))
            throw new IllegalArgumentException("These two nodes are not connected");

        SimpleNetwork network = new SimpleNetwork();
        network.leafNodes.put(networkNode1.location, networkNode1);
        network.leafNodes.put(networkNode2.location, networkNode2);
        return network;
    }

    /**
     * Adds a networking node to the network.
     *
     * @param networkNode Definition of the networking node position and connecting sides.
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
     * @param networkNode Definition of the leaf node position and connecting sides.
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
     * @return The sum of networking nodes and leaf nodes (count).
     */
    @Override
    public int getNetworkSize() {
        return networkingNodes.size() + leafNodes.size();
    }

    /**
     * Removes a leaf node from the network. If this removal made the network degenerate, it will return <code>true</code>.
     *
     * @param networkingNode  Definition of the leaf node position and connecting sides.
     * @return <code>true</code> if the network after the removal is degenerated or empty (no longer valid).
     */
    public boolean removeLeafNode(NetworkNode networkingNode) {
        // Removal of a leaf node cannot split the network, so it's just safe to remove it
        // We just need to check, if after removal of the node, network becomes degenerated, if so - we need
        // to signal that the network is no longer valid and should be removed.
        final boolean changed = leafNodes.remove(networkingNode.location, networkingNode);
        if (!changed)
            throw new IllegalStateException("Tried to remove a node that is not in the network");

        distanceCache.clear();

        return isDegeneratedNetwork() || isEmptyNetwork();
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
        for (Side side : SideBitFlag.getSides(node1.connectionSides)) {
            final ImmutableBlockLocation possibleConnectedLocation = node1.location.move(side);
            if (node2.location.equals(possibleConnectedLocation) && SideBitFlag.hasSide(node2.connectionSides, side.reverse()))
                return true;
        }
        return false;
    }

    /**
     * If this network can connect to node at the location specified with the specified connecting sides.
     *
     * @param networkNode Definition of the networking node position and connecting sides.
     * @return If the networking node can be added to the network (connects to it).
     */
    public boolean canAddNetworkingNode(NetworkNode networkNode) {
        if (isEmptyNetwork())
            return true;
        if (networkingNodes.containsValue(networkNode) || leafNodes.containsValue(networkNode))
            return false;
        return canConnectToNetworkingNode(networkNode);
    }

    public boolean canAddLeafNode(NetworkNode networkNode) {
        if (isEmptyNetwork())
            return false;
        if (networkingNodes.containsValue(networkNode) || leafNodes.containsValue(networkNode))
            return false;

        return canConnectToNetworkingNode(networkNode);
    }

    private boolean canConnectToNetworkingNode(NetworkNode networkNode) {
        for (Side connectingOnSide : SideBitFlag.getSides(networkNode.connectionSides)) {
            final ImmutableBlockLocation possibleConnectionLocation = networkNode.location.move(connectingOnSide);
            for (NetworkNode possibleConnectedNode : networkingNodes.get(possibleConnectionLocation)) {
                if (SideBitFlag.hasSide(possibleConnectedNode.connectionSides, connectingOnSide.reverse()))
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
        TwoNetworkNodes nodePair = new TwoNetworkNodes(from, to);
        final Integer cachedDistance = distanceCache.get(nodePair);
        if (cachedDistance != null)
            return cachedDistance;

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

        TwoNetworkNodes nodePair = new TwoNetworkNodes(from, to);
        final Integer cachedDistance = distanceCache.get(nodePair);
        if (cachedDistance != null)
            return cachedDistance<=distance;

        if ((!hasNetworkingNode(from) && !hasLeafNode(from))
                || (!hasNetworkingNode(to) && !hasLeafNode(to)))
            throw new IllegalArgumentException("Cannot test nodes not in network");

        return isInDistanceInternal(distance, from, to, nodePair);
    }

    private boolean isInDistanceInternal(int distance, NetworkNode from, NetworkNode to, TwoNetworkNodes cachePairKey) {
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
                    distanceCache.put(cachePairKey, distanceSearched);
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

    @Override
    public boolean isInDistanceWithSide(int distance, NetworkNode from, NetworkNode to, Side toSide) {
        to = new NetworkNode(to.location.toVector3i(), toSide);
        TwoNetworkNodes nodePair = new TwoNetworkNodes(from, to);
        return isInDistanceInternal(distance, from, to, nodePair);
    }

    @Override
    public byte getLeafSidesInNetwork(NetworkNode networkNode) {
        if (!hasLeafNode(networkNode))
            throw new IllegalArgumentException("Cannot test nodes not in network");

        if (networkingNodes.size() == 0) {
            // Degenerated network
            for (Side connectingOnSide : SideBitFlag.getSides(networkNode.connectionSides)) {
                Vector3i possibleLocation = networkNode.location.toVector3i();
                possibleLocation.add(connectingOnSide.getVector3i());
                for (NetworkNode node : leafNodes.get(new ImmutableBlockLocation(possibleLocation))) {
                    if (SideBitFlag.hasSide(node.connectionSides, connectingOnSide.reverse())) {
                        return SideBitFlag.getSide(connectingOnSide);
                    }
                }
            }

            return 0;
        } else {
            byte result = 0;
            for (Side connectingOnSide : SideBitFlag.getSides(networkNode.connectionSides)) {
                Vector3i possibleLocation = networkNode.location.toVector3i();
                possibleLocation.add(connectingOnSide.getVector3i());
                for (NetworkNode node : networkingNodes.get(new ImmutableBlockLocation(possibleLocation))) {
                    if (SideBitFlag.hasSide(node.connectionSides, connectingOnSide.reverse())) {
                        result+=SideBitFlag.getSide(connectingOnSide);
                    }
                }
            }

            return result;
        }
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
        for (Side connectingOnSide : SideBitFlag.getSides(location.connectionSides)) {
            final ImmutableBlockLocation possibleConnectionLocation = location.location.move(connectingOnSide);
            for (NetworkNode possibleConnection : networkingNodes.get(possibleConnectionLocation)) {
                if (!visitedNodes.contains(possibleConnection) && SideBitFlag.hasSide(possibleConnection.connectionSides, connectingOnSide.reverse()))
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