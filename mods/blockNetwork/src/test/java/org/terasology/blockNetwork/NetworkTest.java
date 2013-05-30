package org.terasology.blockNetwork;

import org.junit.Before;
import org.junit.Test;
import org.terasology.math.Direction;
import org.terasology.math.Vector3fUtil;
import org.terasology.math.Vector3i;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

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
        assertEquals(1, network.getNetworkSize());
    }

    @Test
    public void addLeafNodeToEmptyNetwork() {
        assertTrue(network.canAddBlock(new Vector3i(0, 0, 0), allDirections));
        network.addLeafBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(1, network.getNetworkSize());
    }

    @Test
    public void addingLeafNodeToNetworkingNode() {
        network.addNetworkingBlock(new Vector3i(0, 0, 1), allDirections);

        assertTrue(network.canAddBlock(new Vector3i(0, 0, 0), allDirections));
        network.addLeafBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(2, network.getNetworkSize());
    }

    @Test
    public void addingLeafNodeToLeafNode() {
        network.addLeafBlock(new Vector3i(0, 0, 1), allDirections);

        assertTrue(network.canAddBlock(new Vector3i(0, 0, 0), allDirections));
        network.addLeafBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(2, network.getNetworkSize());
    }

    @Test
    public void addingNetworkingNodeToNetworkingNode() {
        network.addNetworkingBlock(new Vector3i(0, 0, 1), allDirections);

        assertTrue(network.canAddBlock(new Vector3i(0, 0, 0), allDirections));
        network.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(2, network.getNetworkSize());
    }

    @Test
    public void addingNetworkingNodeToLeafNode() {
        network.addLeafBlock(new Vector3i(0, 0, 1), allDirections);

        assertTrue(network.canAddBlock(new Vector3i(0, 0, 0), allDirections));
        network.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(2, network.getNetworkSize());
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
        assertEquals(3, network.getNetworkSize());
    }

    @Test
    public void cantAddNodeToNetworkWithTwoLeafNodes() {
        network.addLeafBlock(new Vector3i(0, 0, 2), allDirections);
        network.addLeafBlock(new Vector3i(0, 0, 1), allDirections);

        assertFalse(network.canAddBlock(new Vector3i(0, 0, 0), allDirections));
    }

    @Test
    public void removeNetworkingNodeFromConnectedNetworkWithLeaf() {
        network.addLeafBlock(new Vector3i(0, 0, 1), allDirections);
        network.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);

        assertNull(network.removeNetworkingBlock(new Vector3i(0, 0, 0)));
        assertEquals(1, network.getNetworkSize());
    }

    @Test
    public void removeNetworkingNodeFromConnectedNetworkWithNetworkingNode() {
        network.addNetworkingBlock(new Vector3i(0, 0, 1), allDirections);
        network.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);

        assertNull(network.removeNetworkingBlock(new Vector3i(0, 0, 0)));
        assertEquals(1, network.getNetworkSize());
    }

    @Test
    public void removeLeafNodeFromConnectedNetworkWithNetworkingNode() {
        network.addLeafBlock(new Vector3i(0, 0, 0), allDirections);
        network.addNetworkingBlock(new Vector3i(0, 0, 1), allDirections);

        network.removeLeafBlock(new Vector3i(0, 0, 0));
        assertEquals(1, network.getNetworkSize());
    }

    @Test
    public void removeLeafNodeFromConnectedNetworkWithOnlyLeafNodes() {
        network.addLeafBlock(new Vector3i(0, 0, 0), allDirections);
        network.addLeafBlock(new Vector3i(0, 0, 1), allDirections);

        network.removeLeafBlock(new Vector3i(0, 0, 0));
        assertEquals(1, network.getNetworkSize());
    }

    @Test
    public void removingNetworkingNodeSplitsNetwork() {
        network.addLeafBlock(new Vector3i(0, 0, -1), allDirections);
        network.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
        network.addLeafBlock(new Vector3i(0, 0, 1), allDirections);

        final Collection<Network> resultNetworks = network.removeNetworkingBlock(new Vector3i(0, 0, 0));
        assertEquals(2, resultNetworks.size());
        for (Network resultNetwork : resultNetworks)
            assertEquals(1, resultNetwork.getNetworkSize());
    }

    @Test
    public void removingNetworkingNodeDoesNotSplitNetworkIfThereIsAlternativePath() {
        network.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
        network.addNetworkingBlock(new Vector3i(1, 0, 0), allDirections);
        network.addNetworkingBlock(new Vector3i(1, 0, 1), allDirections);
        network.addNetworkingBlock(new Vector3i(0, 0, 1), allDirections);

        assertNull(network.removeNetworkingBlock(new Vector3i(0, 0, 0)));
        assertEquals(3, network.getNetworkSize());
    }

    @Test
    public void removeTheOnlyNetworkingNode() {
        network.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
        final Collection<Network> resultNetworks = network.removeNetworkingBlock(new Vector3i(0, 0, 0));
        assertNotNull(resultNetworks);
        assertEquals(0, resultNetworks.size());
    }

    @Test
    public void removeTheOnlyLeafNode() {
        network.addLeafBlock(new Vector3i(0, 0, 0), allDirections);
        assertTrue(network.removeLeafBlock(new Vector3i(0, 0, 0)));
    }
}
