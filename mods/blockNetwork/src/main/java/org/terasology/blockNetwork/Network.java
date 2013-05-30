package org.terasology.blockNetwork;

import org.terasology.math.Direction;

import java.util.*;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.math.Vector3i;

/**
 * Represents one network of blocks, where each block is somehow connected to another within the network.
 * <p/>
 * Network contains following node (block) types:
 * - networking nodes (blocks) - blocks that are a back-bone of a network. These allow to connect multiple nodes
 * in the network. A networking node "conducts" the "signal" of the network to nodes defined in the
 * "connectingOnSides" blocks in its vicinity.
 * - leaf nodes (blocks) - blocks that are only receiving or producing a signal, and do not themselves "conduct" it to
 * other nodes (blocks).
 * <p/>
 * A couple of non-obvious facts:
 * 1. The same block (defined as location) can be both a networking node and a leaf node of the network.
 * 2. The same leaf block can be a member of multiple disjunctive networks (different network on each side).
 * 3. A network can have no networking nodes at all, and exactly two leaf nodes (neighbouring leaf blocks).
 *
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class Network {
    private static final boolean SANITY_CHECK = true;
    private Map<Vector3i, Byte> networkingBlocks = Maps.newHashMap();
    private Map<Vector3i, Byte> leafBlocks = Maps.newHashMap();

    public static Network createDegenerateNetwork(
            Vector3i location1, Collection<Direction> connectingOnSides1,
            Vector3i location2, Collection<Direction> connectingOnSides2) {
        Network network = new Network();
        network.leafBlocks.put(location1, DirectionsUtil.getDirections(connectingOnSides1));
        network.leafBlocks.put(location2, DirectionsUtil.getDirections(connectingOnSides2));
        return network;
    }

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
                final Byte directionsForBlockOnThatSide = leafBlocks.get(possibleBlockLocation);
                if (directionsForBlockOnThatSide != null && DirectionsUtil.hasDirection(directionsForBlockOnThatSide, connectingOnSide.reverse())) {
                    final HashSet<Direction> connections = Sets.newHashSet();
                    connections.add(connectingOnSide);
                }
            }
        }
        networkingBlocks.put(new Vector3i(location), DirectionsUtil.getDirections(connectingOnSides));
    }

    /**
     * Adds a leaf block to the network.
     *
     * @param location          The location of the new block.
     * @param connectingOnSides Sides on which it can connect blocks.
     */
    public void addLeafBlock(Vector3i location, Collection<Direction> connectingOnSides) {
        if (SANITY_CHECK && (!canAddBlock(location, connectingOnSides) || isEmptyNetwork()))
            throw new IllegalStateException("Unable to add this block to network");
        leafBlocks.put(new Vector3i(location), DirectionsUtil.getDirections(connectingOnSides));
    }

    /**
     * Returns the network size - a number of blocks it spans. If a block is both a networking block and leaf block,
     * it is counted once only.
     *
     * @return
     */
    public int getNetworkSize() {
        Set<Vector3i> countedNodes = new HashSet<Vector3i>();
        countedNodes.addAll(networkingBlocks.keySet());
        countedNodes.addAll(leafBlocks.keySet());
        return countedNodes.size();
    }

    /**
     * Removes a leaf block from the network. If this was the last node in the network, <code>true</code> is returned
     * to signify that this network is now empty.
     *
     * @param location
     */
    public boolean removeLeafBlock(Vector3i location) {
        // Removal of a leaf block cannot split the network, so it's just safe to remove it
        // We just need to check, if after removal of the block, network becomes degenerated, if so - we need
        // to also clean the actual connections, as we had a network with two leaf nodes being connected
        final Byte removedDirections = leafBlocks.remove(location);
        if (removedDirections == null)
            throw new IllegalStateException("Tried to remove a block that is not in the network");

        if (isDegeneratedNetwork())
            return true;

        return isEmptyNetwork();
    }

    /**
     * Remove networking block from the network. If this removal splits the network, the returned Set will contain all
     * the Networks this Network should be replaced with. If it does not split it, this method will return <code>null</code>.
     *
     * @param location Location of the removed block.
     * @return If not <code>null</code>, current network should be replaced with the returned networks. If an empty
     *         Collection is returned, the original network should be removed.
     */
    public Collection<Network> removeNetworkingBlock(Vector3i location) {
        // Removal of a block can split the network if it is connected to a block on at least two sides, and there is no
        // other connection between the neighbouring blocks
        Byte directions = networkingBlocks.remove(location);
        if (directions == null)
            throw new IllegalStateException("Tried to remove a block that is not in the network");

        // TODO Do something smarter, at least check if the removed block is connected to at least two other nodes
        // For now, just naively rebuild the whole network after removal
        Set<Network> resultNetworks = Sets.newHashSet();

        if (networkingBlocks.isEmpty() && leafBlocks.size() == 2) {
            final Iterator<Map.Entry<Vector3i, Byte>> leafIterator = leafBlocks.entrySet().iterator();
            final Map.Entry<Vector3i, Byte> firstLeaf = leafIterator.next();
            final Map.Entry<Vector3i, Byte> secondLeaf = leafIterator.next();

            if (Network.areNodesConnecting(firstLeaf.getKey(), DirectionsUtil.getDirections(firstLeaf.getValue()),
                    secondLeaf.getKey(), DirectionsUtil.getDirections(secondLeaf.getValue())))
                resultNetworks.add(Network.createDegenerateNetwork(
                        firstLeaf.getKey(), DirectionsUtil.getDirections(firstLeaf.getValue()), secondLeaf.getKey(), DirectionsUtil.getDirections(secondLeaf.getValue())));
        }

        for (Map.Entry<Vector3i, Byte> networkingBlock : networkingBlocks.entrySet()) {
            Network networkAddedTo = null;

            final Vector3i networkingBlockLocation = networkingBlock.getKey();
            final byte networkingBlockConnectingSides = networkingBlock.getValue();
            final Collection<Direction> networkingBlockDirections = DirectionsUtil.getDirections(networkingBlockConnectingSides);
            final Iterator<Network> resultNetworksIterator = resultNetworks.iterator();
            while (resultNetworksIterator.hasNext()) {
                final Network resultNetwork = resultNetworksIterator.next();
                if (resultNetwork.canAddBlock(networkingBlockLocation, networkingBlockDirections)) {
                    if (networkAddedTo == null) {
                        resultNetwork.addNetworkingBlock(networkingBlockLocation, networkingBlockDirections);
                        networkAddedTo = resultNetwork;
                    } else {
                        networkAddedTo.mergeInNetwork(resultNetwork);
                        resultNetworksIterator.remove();
                    }
                }
            }

            if (networkAddedTo == null) {
                Network newNetwork = new Network();
                newNetwork.addNetworkingBlock(networkingBlockLocation, networkingBlockDirections);
                resultNetworks.add(newNetwork);
            }
        }

        // We have all connections for the resulting networks, now add leaves
        for (Map.Entry<Vector3i, Byte> leafBlock : leafBlocks.entrySet()) {
            final Vector3i leafBlockLocation = leafBlock.getKey();
            final Collection<Direction> leafBlockDirections = DirectionsUtil.getDirections(leafBlock.getValue());

            for (Network resultNetwork : resultNetworks) {
                if (resultNetwork.canAddBlock(leafBlockLocation, leafBlockDirections)) {
                    resultNetwork.addLeafBlock(leafBlockLocation, leafBlockDirections);
                }
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
        networkingBlocks.putAll(network.networkingBlocks);
        // Naive implementation - add all connecting blocks and rebuild all the leaves
        final Map<Vector3i, Byte> oldThisNetworkLeaves = leafBlocks;
        leafBlocks = Maps.newHashMap();
        for (Map.Entry<Vector3i, Byte> oldNetworkLeaves : oldThisNetworkLeaves.entrySet())
            addLeafBlock(oldNetworkLeaves.getKey(), DirectionsUtil.getDirections(oldNetworkLeaves.getValue()));
        for (Map.Entry<Vector3i, Byte> otherNetworkLeaves : network.leafBlocks.entrySet())
            addLeafBlock(otherNetworkLeaves.getKey(), DirectionsUtil.getDirections(otherNetworkLeaves.getValue()));
    }

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
        if (networkingBlocks.containsKey(location))
            return true;
        // If this network has no networking blocks (it's a lone leaf node), then this block can be added to the
        // network if it can connect to this (any) leaf node
        if (isDegeneratedNetwork()) {
            for (Direction connectingOnSide : connectingOnSides) {
                final Vector3i possibleBlockLocation = new Vector3i(location);
                possibleBlockLocation.add(connectingOnSide.getVector3i());
                final Byte directionsForBlockOnThatSide = leafBlocks.get(possibleBlockLocation);
                if (directionsForBlockOnThatSide != null && DirectionsUtil.hasDirection(directionsForBlockOnThatSide, connectingOnSide.reverse()))
                    return true;
            }
        } else {
            for (Direction connectingOnSide : connectingOnSides) {
                final Vector3i possibleBlockLocation = new Vector3i(location);
                possibleBlockLocation.add(connectingOnSide.getVector3i());
                final Byte directionsForBlockOnThatSide = networkingBlocks.get(possibleBlockLocation);
                if (directionsForBlockOnThatSide != null && DirectionsUtil.hasDirection(directionsForBlockOnThatSide, connectingOnSide.reverse()))
                    return true;
            }
        }
        return false;
    }

    public boolean hasNetworkingBlock(Vector3i location) {
        return networkingBlocks.containsKey(location);
    }

    public boolean hasLeafBlock(Vector3i location) {
        return leafBlocks.containsKey(location);
    }

    public Network cloneNetwork() {
        Network result = new Network();
        result.networkingBlocks.putAll(networkingBlocks);
        result.leafBlocks.putAll(leafBlocks);
        return result;
    }

    public boolean isDegeneratedNetwork() {
        return networkingBlocks.isEmpty() && leafBlocks.size() == 1;
    }

    private boolean isEmptyNetwork() {
        return networkingBlocks.isEmpty() && leafBlocks.isEmpty();
    }
}