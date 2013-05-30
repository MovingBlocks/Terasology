package org.terasology.blockNetwork;

import org.junit.Before;
import org.junit.Test;
import org.terasology.math.Direction;
import org.terasology.math.Vector3fUtil;
import org.terasology.math.Vector3i;

import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NetworkTest {
    private Network network;
    private Collection<Direction> allDirections;
    private Collection<Direction> upOnly;

    @Before
    public void setup() {
        network = new Network();
        allDirections = new HashSet<Direction>();
        allDirections.add(Direction.UP);
        allDirections.add(Direction.LEFT);
        allDirections.add(Direction.FORWARD);
        allDirections.add(Direction.DOWN);
        allDirections.add(Direction.RIGHT);
        allDirections.add(Direction.BACKWARD);
        upOnly = new HashSet<Direction>();
        upOnly.add(Direction.UP);
    }

    @Test
    public void addNetworkingNodeToEmptyNetwork() {
        assertTrue(network.canAddBlock(new Vector3i(0, 0, 0), allDirections));
        network.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
    }

    @Test
    public void addLeafNodeToEmptyNetwork() {
        assertTrue(network.canAddBlock(new Vector3i(0, 0, 0), allDirections));
        network.addLeafBlock(new Vector3i(0, 0, 0), allDirections);
    }

    @Test
    public void addingLeafNodeToNetworkingNode() {
        network.addNetworkingBlock(new Vector3i(0, 0, 1), allDirections);

        assertTrue(network.canAddBlock(new Vector3i(0, 0, 0), allDirections));
        network.addLeafBlock(new Vector3i(0, 0, 0), allDirections);
    }

    @Test
    public void addingLeafNodeToLeafNode() {
        network.addLeafBlock(new Vector3i(0, 0, 1), allDirections);

        assertTrue(network.canAddBlock(new Vector3i(0, 0, 0), allDirections));
        network.addLeafBlock(new Vector3i(0, 0, 0), allDirections);
    }

    @Test
    public void addingNetworkingNodeToNetworkingNode() {
        network.addNetworkingBlock(new Vector3i(0, 0, 1), allDirections);

        assertTrue(network.canAddBlock(new Vector3i(0, 0, 0), allDirections));
        network.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
    }

    @Test
    public void addingNetworkingNodeToLeafNode() {
        network.addLeafBlock(new Vector3i(0, 0, 1), allDirections);

        assertTrue(network.canAddBlock(new Vector3i(0, 0, 0), allDirections));
        network.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
    }

    @Test
    public void cantAddNodeToNetworkingNodeTooFar() {
        network.addNetworkingBlock(new Vector3i(0, 0, 2), allDirections);

        assertFalse(network.canAddBlock(new Vector3i(0, 0, 0), allDirections));
    }

    @Test
    public void cantAddNodeToLeafNodeTooFar() {
        network.addLeafBlock(new Vector3i(0, 0, 2), allDirections);

        assertFalse(network.canAddBlock(new Vector3i(0, 0, 0), allDirections));
    }

    @Test
    public void cantAddNodeToNetworkingNodeWrongDirection() {
        network.addNetworkingBlock(new Vector3i(0, 0, 1), upOnly);

        assertFalse(network.canAddBlock(new Vector3i(0, 0, 0), allDirections));
    }

    @Test
    public void cantAddNodeToLeafNodeWrongDirection() {
        network.addLeafBlock(new Vector3i(0, 0, 1), upOnly);

        assertFalse(network.canAddBlock(new Vector3i(0, 0, 0), allDirections));
    }

    @Test
    public void cantAddNodeToNetworkOnTheSideOfConnectedLeaf() {
        network.addLeafBlock(new Vector3i(0, 0, 1), allDirections);
        network.addNetworkingBlock(new Vector3i(0, 0, 2), allDirections);

        assertFalse(network.canAddBlock(new Vector3i(0, 0, 0), allDirections));
    }

    @Test
    public void canAddLeafNodeOnTheSideOfConnectedNetworkingNode() {
        network.addLeafBlock(new Vector3i(0, 0, 2), allDirections);
        network.addNetworkingBlock(new Vector3i(0, 0, 1), allDirections);

        assertTrue(network.canAddBlock(new Vector3i(0, 0, 0), allDirections));
        network.addLeafBlock(new Vector3i(0, 0, 0), allDirections);
    }
    
    @Test
    public void canAddNetworkingNodeOnTheSideOfConnectedNetworkingNode() {
        network.addLeafBlock(new Vector3i(0, 0, 2), allDirections);
        network.addNetworkingBlock(new Vector3i(0, 0, 1), allDirections);

        assertTrue(network.canAddBlock(new Vector3i(0, 0, 0), allDirections));
        network.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
    }
}
