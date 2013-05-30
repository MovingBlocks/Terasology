package org.terasology.blockNetwork;

import org.terasology.math.Direction;

import javax.vecmath.Vector3f;
import java.util.*;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.math.Vector3i;

/**
 * Represents one network of blocks, where each block is somehow connected to another within the network.
 *
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class Network {
    private static final boolean SANITY_CHECK = true;
    private Map<Vector3i, Byte> networkingBlocks = Maps.newHashMap();
    private Map<Vector3i, Byte> leafPossibleConnections = Maps.newHashMap();
    private Map<Vector3i, Byte> leafActualConnections = Maps.newHashMap();

    /**
     * Adds block to the network.
     *
     * @param location          The location of the new block.
     * @param connectingOnSides Sides on which it can connect blocks.
     */
    public void addNetworkingBlock(Vector3i location, Collection<Direction> connectingOnSides) {
        if (SANITY_CHECK && !canAddBlock(location, connectingOnSides))
            throw new IllegalStateException("Unable to add this block to network");
        if (isDegeneratedNetwork()) {
            for (Direction connectingOnSide : connectingOnSides) {
                final Vector3i possibleBlockLocation = new Vector3i(location);
                possibleBlockLocation.add(connectingOnSide.getVector3i());
                final Byte directionsForBlockOnThatSide = leafPossibleConnections.get(possibleBlockLocation);
                if (directionsForBlockOnThatSide != null && DirectionsUtil.hasDirection(directionsForBlockOnThatSide, connectingOnSide.reverse())) {
                    final HashSet<Direction> connections = Sets.newHashSet();
                    connections.add(connectingOnSide);
                    addLeafActualConnection(location, connectingOnSide);
                }
            }
        }
        networkingBlocks.put(new Vector3i(location), DirectionsUtil.getDirections(connectingOnSides));
    }

    public void addLeafBlock(Vector3i location, Collection<Direction> connectingOnSides) {
        if (SANITY_CHECK && !canAddBlock(location, connectingOnSides))
            throw new IllegalStateException("Unable to add this block to network");
        leafPossibleConnections.put(new Vector3i(location), DirectionsUtil.getDirections(connectingOnSides));
        if (isDegeneratedNetwork()) {
            for (Direction connectingOnSide : connectingOnSides) {
                final Vector3i possibleBlockLocation = new Vector3i(location);
                possibleBlockLocation.add(connectingOnSide.getVector3i());
                final Byte directionsForBlockOnThatSide = leafPossibleConnections.get(possibleBlockLocation);
                if (directionsForBlockOnThatSide != null && DirectionsUtil.hasDirection(directionsForBlockOnThatSide, connectingOnSide.reverse())) {
                    final HashSet<Direction> connections = Sets.newHashSet();
                    connections.add(connectingOnSide);
                    final HashSet<Direction> reverseConnections = Sets.newHashSet();
                    reverseConnections.add(connectingOnSide.reverse());
                    addLeafActualConnection(location, connectingOnSide);
                    addLeafActualConnection(possibleBlockLocation, connectingOnSide.reverse());
                }
            }
        } else {
            for (Direction connectingOnSide : connectingOnSides) {
                final Vector3i possibleBlockLocation = new Vector3i(location);
                possibleBlockLocation.add(connectingOnSide.getVector3i());
                final Byte directionsForBlockOnThatSide = networkingBlocks.get(possibleBlockLocation);
                if (directionsForBlockOnThatSide != null && DirectionsUtil.hasDirection(directionsForBlockOnThatSide, connectingOnSide.reverse()))
                    addLeafActualConnection(location, connectingOnSide);
            }
        }
    }

    private void addLeafActualConnection(Vector3i location, Direction direction) {
        Byte actualConnections = leafActualConnections.get(location);
        if (actualConnections == null)
            actualConnections = 0;
        leafActualConnections.put(location, DirectionsUtil.addDirection(actualConnections, direction));
    }

    private boolean isDegeneratedNetwork() {
        return networkingBlocks.isEmpty() && leafPossibleConnections.size() == 1;
    }

    private boolean isEmptyNetwork() {
        return networkingBlocks.isEmpty() && leafPossibleConnections.isEmpty();
    }

    /**
     * Remove block from the network. If this removal splits the network, the returned Set will contain all the
     * Networks this Network should be replaced with.
     *
     * @param location Location of the removed block.
     * @return
     */
    public Set<Network> removeNetworkingBlock(Vector3i location) {
        // TODO Review and finish
        // Removal of a block splits the network, if it is connected to a block on at least two sides, and there is no
        // other connection between the neighbouring blocks
//        Set<Direction> directions = networkingBlocks.remove(location);
//        int connectedOnSidesCount = 0;
//        for (Direction direction : directions) {
//            final List<Direction> directionList = Arrays.asList(direction);
//            if (canAddBlock(location, directionList))
//                connectedOnSidesCount++;
////            else if (canConnectToLeafBlocks(location, directionList))
//        }
//        if (connectedOnSidesCount < 2)
//            return Collections.emptySet();
//
//        List<Vector3i> nodesBeingDisconnected = new ArrayList<Vector3i>();
//        for (Direction direction : directions) {
//            final Vector3i connectedBlockLocation = new Vector3i(location);
//            connectedBlockLocation.add(direction.getVector3i());
//            if (networkingBlocks.containsKey(connectedBlockLocation))
//                nodesBeingDisconnected.add(connectedBlockLocation);
//        }

        // Naive implementation depth-first search with a set of visited blocks
        return null;
    }

    /**
     * Merges the specified network into this one.
     *
     * @param network
     */
    public void mergeWithNetwork(Network network) {
        networkingBlocks.putAll(network.networkingBlocks);
        leafPossibleConnections.putAll(network.leafPossibleConnections);
        // TODO merge possible connections
    }

//    /**
//     * If this network contains this block
//     *
//     * @param location
//     * @return
//     */
//    public boolean hasBlockAt(Vector3i location) {
//        return networkingBlocks.containsKey(location) || leafNodes.containsKey(location);
//    }

    /**
     * If this network can connect to block at the location specified with the specified connecting sides.
     *
     * @param location
     * @param connectingOnSides
     * @return
     */
    public boolean canAddBlock(Vector3i location, Collection<Direction> connectingOnSides) {
        if (isEmptyNetwork())
            return true;
        // If this network has no networking blocks (it's a lone leaf node), then this block can be added to the
        // network if it can connect to this (any) leaf node
        if (isDegeneratedNetwork()) {
            for (Direction connectingOnSide : connectingOnSides) {
                final Vector3i possibleBlockLocation = new Vector3i(location);
                possibleBlockLocation.add(connectingOnSide.getVector3i());
                final Byte directionsForBlockOnThatSide = leafPossibleConnections.get(possibleBlockLocation);
                if (directionsForBlockOnThatSide!= null && DirectionsUtil.hasDirection(directionsForBlockOnThatSide, connectingOnSide.reverse()))
                    return true;
            }
        } else {
            for (Direction connectingOnSide : connectingOnSides) {
                final Vector3i possibleBlockLocation = new Vector3i(location);
                possibleBlockLocation.add(connectingOnSide.getVector3i());
                final Byte directionsForBlockOnThatSide = networkingBlocks.get(possibleBlockLocation);
                if (directionsForBlockOnThatSide!= null && DirectionsUtil.hasDirection(directionsForBlockOnThatSide, connectingOnSide.reverse()))
                        return true;
            }
        }
        return false;
    }

//    public boolean canConnectToLeafBlocks(Vector3i location, Collection<Direction> connectingOnSides) {
//        for (Direction connectingOnSide : connectingOnSides) {
//            final Vector3i possibleBlockLocation = new Vector3i(location);
//            possibleBlockLocation.add(connectingOnSide.getVector3i());
//            Set<Direction> directionsForBlockOnThatSide = leafNodes.get(possibleBlockLocation);
//            if (directionsForBlockOnThatSide != null) {
//                if (directionsForBlockOnThatSide.contains(connectingOnSide.reverse()))
//                    return true;
//            }
//        }
//        return false;
//    }

//    private int getDistance(Vector3i from, Vector3i to) {
//        Set<Vector3i> visitedNodes = new HashSet<Vector3i>();
//        int distance = 0;
//        Set<Vector3i> searchLeafNodes = new HashSet<Vector3i>();
//
//        networkingBlocks
//    }

//    private Map<Vector3i, Set<Direction>> getAllBlocksConnectedToBlockExcluding(Vector3i location, Set<Direction> connectingOnSides, Set<Vector3i> exclusions) {
//        Map<Vector3i, Set<Direction>> result = Maps.newHashMap();
//        for (Direction connectingOnSide : connectingOnSides) {
//            final Vector3i possibleBlockLocation = new Vector3i(location);
//            possibleBlockLocation.add(connectingOnSide.getVector3i());
//            if (!exclusions.contains(possibleBlockLocation)) {
//                Set<Direction> directionsForBlockOnThatSide = networkingBlocks.get(possibleBlockLocation);
//                if (directionsForBlockOnThatSide != null)
//                    result.put(possibleBlockLocation, directionsForBlockOnThatSide);
//            }
//        }
//        return result;
//    }
}