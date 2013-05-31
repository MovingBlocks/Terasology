package org.terasology.blockNetwork;

import org.junit.Before;
import org.junit.Test;
import org.terasology.math.Direction;
import org.terasology.math.Vector3i;

import java.util.Collection;

import static org.junit.Assert.*;

public class SimpleNetworkTest {
    private SimpleNetwork network;
    private byte allDirections;
    private byte upOnly;

    @Before
    public void setup() {
        network = new SimpleNetwork();
        allDirections = DirectionsUtil.addDirection((byte) 0, Direction.UP, Direction.LEFT, Direction.FORWARD, Direction.DOWN, Direction.RIGHT, Direction.BACKWARD);
        upOnly = DirectionsUtil.addDirection((byte) 0, Direction.UP);
    }

    @Test
    public void addNetworkingNodeToEmptyNetwork() {
        assertTrue(network.canAddNode(new Vector3i(0, 0, 0), allDirections));
        network.addNetworkingNode(new Vector3i(0, 0, 0), allDirections);
        assertEquals(1, network.getNetworkSize());
    }

    @Test(expected = IllegalStateException.class)
    public void addLeafNodeToEmptyNetwork() {
        assertTrue(network.canAddNode(new Vector3i(0, 0, 0), allDirections));
        network.addLeafNode(new Vector3i(0, 0, 0), allDirections);
    }

    @Test
    public void addingLeafNodeToNetworkingNode() {
        network.addNetworkingNode(new Vector3i(0, 0, 1), allDirections);

        assertTrue(network.canAddNode(new Vector3i(0, 0, 0), allDirections));
        network.addLeafNode(new Vector3i(0, 0, 0), allDirections);
        assertEquals(2, network.getNetworkSize());
    }

    @Test
    public void creatingDegenerateNetwork() {
        network = SimpleNetwork.createDegenerateNetwork(new Vector3i(0, 0, 1), allDirections, new Vector3i(0, 0, 0), allDirections);
        assertEquals(2, network.getNetworkSize());
    }

    @Test
    public void addingNetworkingNodeToNetworkingNode() {
        network.addNetworkingNode(new Vector3i(0, 0, 1), allDirections);

        assertTrue(network.canAddNode(new Vector3i(0, 0, 0), allDirections));
        network.addNetworkingNode(new Vector3i(0, 0, 0), allDirections);
        assertEquals(2, network.getNetworkSize());
    }

    @Test
    public void cantAddNodeToNetworkingNodeTooFar() {
        network.addNetworkingNode(new Vector3i(0, 0, 2), allDirections);

        assertFalse(network.canAddNode(new Vector3i(0, 0, 0), allDirections));
    }

    @Test
    public void cantAddNodeToNetworkingNodeWrongDirection() {
        network.addNetworkingNode(new Vector3i(0, 0, 1), upOnly);

        assertFalse(network.canAddNode(new Vector3i(0, 0, 0), allDirections));
    }

    @Test
    public void cantAddNodeToNetworkOnTheSideOfConnectedLeaf() {
        network.addNetworkingNode(new Vector3i(0, 0, 2), allDirections);
        network.addLeafNode(new Vector3i(0, 0, 1), allDirections);

        assertFalse(network.canAddNode(new Vector3i(0, 0, 0), allDirections));
    }

    @Test
    public void canAddLeafNodeOnTheSideOfConnectedNetworkingNode() {
        network.addNetworkingNode(new Vector3i(0, 0, 1), allDirections);
        network.addLeafNode(new Vector3i(0, 0, 2), allDirections);

        assertTrue(network.canAddNode(new Vector3i(0, 0, 0), allDirections));
        network.addLeafNode(new Vector3i(0, 0, 0), allDirections);
    }

    @Test
    public void canAddNetworkingNodeOnTheSideOfConnectedNetworkingNode() {
        network.addNetworkingNode(new Vector3i(0, 0, 1), allDirections);
        network.addLeafNode(new Vector3i(0, 0, 2), allDirections);

        assertTrue(network.canAddNode(new Vector3i(0, 0, 0), allDirections));
        network.addNetworkingNode(new Vector3i(0, 0, 0), allDirections);
        assertEquals(3, network.getNetworkSize());
    }

    @Test
    public void cantAddNodeToNetworkWithTwoLeafNodes() {
        network = SimpleNetwork.createDegenerateNetwork(new Vector3i(0, 0, 2), allDirections, new Vector3i(0, 0, 1), allDirections);

        assertFalse(network.canAddNode(new Vector3i(0, 0, 0), allDirections));
    }

    @Test
    public void removeNetworkingNodeFromConnectedNetworkWithLeaf() {
        network.addNetworkingNode(new Vector3i(0, 0, 0), allDirections);
        network.addLeafNode(new Vector3i(0, 0, 1), allDirections);

        final Collection<SimpleNetwork> resultNetworks = network.removeNetworkingNode(new Vector3i(0, 0, 0));
        assertNotNull(resultNetworks);
        assertEquals(0, resultNetworks.size());
    }

    @Test
    public void removeNetworkingNodeFromConnectedNetworkWithNetworkingNode() {
        network.addNetworkingNode(new Vector3i(0, 0, 1), allDirections);
        network.addNetworkingNode(new Vector3i(0, 0, 0), allDirections);

        assertNull(network.removeNetworkingNode(new Vector3i(0, 0, 0)));
        assertEquals(1, network.getNetworkSize());
    }

    @Test
    public void removeLeafNodeFromConnectedNetworkWithNetworkingNode() {
        network.addNetworkingNode(new Vector3i(0, 0, 1), allDirections);
        network.addLeafNode(new Vector3i(0, 0, 0), allDirections);

        network.removeLeafNode(new Vector3i(0, 0, 0));
        assertEquals(1, network.getNetworkSize());
    }

    @Test
    public void removeLeafNodeFromConnectedNetworkWithOnlyLeafNodes() {
        network = SimpleNetwork.createDegenerateNetwork(new Vector3i(0, 0, 0), allDirections, new Vector3i(0, 0, 1), allDirections);

        assertTrue(network.removeLeafNode(new Vector3i(0, 0, 0)));
        assertEquals(1, network.getNetworkSize());
    }

    @Test
    public void removingNetworkingNodeRemovesNetworkWithLeavesOnly() {
        network.addNetworkingNode(new Vector3i(0, 0, 0), allDirections);
        network.addLeafNode(new Vector3i(0, 0, -1), allDirections);
        network.addLeafNode(new Vector3i(0, 0, 1), allDirections);

        final Collection<SimpleNetwork> resultNetworks = network.removeNetworkingNode(new Vector3i(0, 0, 0));
        assertNotNull(resultNetworks);
        assertEquals(0, resultNetworks.size());
    }

    @Test
    public void removingNetworkingNodeSplitsNetwork() {
        network.addNetworkingNode(new Vector3i(0, 0, -1), allDirections);
        network.addNetworkingNode(new Vector3i(0, 0, 0), allDirections);
        network.addNetworkingNode(new Vector3i(0, 0, 1), allDirections);

        final Collection<SimpleNetwork> resultNetworks = network.removeNetworkingNode(new Vector3i(0, 0, 0));
        assertEquals(2, resultNetworks.size());
        for (SimpleNetwork resultNetwork : resultNetworks)
            assertEquals(1, resultNetwork.getNetworkSize());
    }

    @Test
    public void removingNetworkingNodeDoesNotSplitNetworkIfThereIsAlternativePath() {
        network.addNetworkingNode(new Vector3i(0, 0, 0), allDirections);
        network.addNetworkingNode(new Vector3i(1, 0, 0), allDirections);
        network.addNetworkingNode(new Vector3i(1, 0, 1), allDirections);
        network.addNetworkingNode(new Vector3i(0, 0, 1), allDirections);

        assertNull(network.removeNetworkingNode(new Vector3i(0, 0, 0)));
        assertEquals(3, network.getNetworkSize());
    }

    @Test
    public void removeTheOnlyNetworkingNode() {
        network.addNetworkingNode(new Vector3i(0, 0, 0), allDirections);
        final Collection<SimpleNetwork> resultNetworks = network.removeNetworkingNode(new Vector3i(0, 0, 0));
        assertNotNull(resultNetworks);
        assertEquals(0, resultNetworks.size());
    }
}
