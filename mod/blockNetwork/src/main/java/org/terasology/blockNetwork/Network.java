package org.terasology.blockNetwork;
import org.terasology.math.Side;
import org.terasology.math.Direction;

import javax.vecmath.Vector3f;
import java.util.*;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.math.Vector3i;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class Network {
    private Map<Vector3i, Set<Direction>> networkedBlocks = Maps.newHashMap();

    /**
     * Adds block to the network.
     * @param location The location of the new block.
     * @param connectingOnSides Sides on which it can connect blocks.
     */
    public void addBlock(Vector3f location, Collection<Direction> connectingOnSides) {
        networkedBlocks.put(new Vector3i(location), Sets.newHashSet(connectingOnSides));
    }

    /**
     * Remove block from the network. If this removal splits the network, the returned Set will contain all the
     * new Networks that were created by the split.
     * @param location Location of the removed block.
     * @return
     */
    public Set<Network> removeBlock(Vector3i location) {
        // Removal of a block splits the network, if it is connected to a block on at least two sides, and there is no
        // other connection between the neighbouring blocks
        Set<Direction> directions = networkedBlocks.remove(location);
        int connectedOnSidesCount = 0;
        for (Direction direction : directions) {
            if (canConnectTo(location, Arrays.asList(direction)))
                connectedOnSidesCount++;
        }
        if (connectedOnSidesCount<2)
            return Collections.emptySet();

        // Naive implementation depth-first search with a set of visited blocks
        for (Direction direction : directions) {
            // TODO Finish network split detection
        }
        return null;
    }

    /**
     * Merges the specified network into this one.
     * @param network
     */
    public void mergeWithNetwork(Network network) {
        networkedBlocks.putAll(network.networkedBlocks);
    }

    /**
     * If this network contains this block
     * @param location
     * @return
     */
    public boolean hasBlockAt(Vector3i location) {
        return networkedBlocks.containsKey(location);
    }

    /**
     * If this network can connect to block at the location specified with the specified connecting sides.
     * @param location
     * @param connectingOnSides
     * @return
     */
    public boolean canConnectTo(Vector3i location, Collection<Direction> connectingOnSides) {
        for (Direction connectingOnSide : connectingOnSides) {
            Vector3f direction = connectingOnSide.getVector3f();
            Set<Direction> directionsForBlockOnThatSide = networkedBlocks.get(new Vector3f(location.x + direction.x, location.y + direction.y, location.z + direction.z));
            if (directionsForBlockOnThatSide!= null) {
                if (directionsForBlockOnThatSide.contains(connectingOnSide.reverse()))
                    return true;
            }
        }
        return false;
    }
}