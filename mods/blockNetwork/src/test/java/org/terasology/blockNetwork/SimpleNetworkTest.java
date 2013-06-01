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
        assertTrue(network.canAddNetworkingNode(new Vector3i(0, 0, 0), allDirections));
        network.addNetworkingNode(new Vector3i(0, 0, 0), allDirections);
        assertEquals(1, network.getNetworkSize());
    }

    @Test
    public void cantAddLeafNodeToEmptyNetwork() {
        assertFalse(network.canAddLeafNode(new Vector3i(0, 0, 0), allDirections));
    }

    @Test
    public void addingLeafNodeToNetworkingNode() {
        network.addNetworkingNode(new Vector3i(0, 0, 1), allDirections);

        assertTrue(network.canAddLeafNode(new Vector3i(0, 0, 0), allDirections));
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

        assertTrue(network.canAddNetworkingNode(new Vector3i(0, 0, 0), allDirections));
        network.addNetworkingNode(new Vector3i(0, 0, 0), allDirections);
        assertEquals(2, network.getNetworkSize());
    }

    @Test
    public void cantAddNodeToNetworkingNodeTooFar() {
        network.addNetworkingNode(new Vector3i(0, 0, 2), allDirections);

        assertFalse(network.canAddNetworkingNode(new Vector3i(0, 0, 0), allDirections));
    }

    @Test
    public void cantAddNodeToNetworkingNodeWrongDirection() {
        network.addNetworkingNode(new Vector3i(0, 0, 1), upOnly);

        assertFalse(network.canAddNetworkingNode(new Vector3i(0, 0, 0), allDirections));
    }

    @Test
    public void cantAddNodeToNetworkOnTheSideOfConnectedLeaf() {
        network.addNetworkingNode(new Vector3i(0, 0, 2), allDirections);
        network.addLeafNode(new Vector3i(0, 0, 1), allDirections);

        assertFalse(network.canAddNetworkingNode(new Vector3i(0, 0, 0), allDirections));
    }

    @Test
    public void canAddLeafNodeOnTheSideOfConnectedNetworkingNode() {
        network.addNetworkingNode(new Vector3i(0, 0, 1), allDirections);
        network.addLeafNode(new Vector3i(0, 0, 2), allDirections);

        assertTrue(network.canAddLeafNode(new Vector3i(0, 0, 0), allDirections));
        network.addLeafNode(new Vector3i(0, 0, 0), allDirections);
    }

    @Test
    public void canAddNetworkingNodeOnTheSideOfConnectedNetworkingNode() {
        network.addNetworkingNode(new Vector3i(0, 0, 1), allDirections);
        network.addLeafNode(new Vector3i(0, 0, 2), allDirections);

        assertTrue(network.canAddNetworkingNode(new Vector3i(0, 0, 0), allDirections));
        network.addNetworkingNode(new Vector3i(0, 0, 0), allDirections);
        assertEquals(3, network.getNetworkSize());
    }

    @Test
    public void cantAddNodeToNetworkWithTwoLeafNodes() {
        network = SimpleNetwork.createDegenerateNetwork(new Vector3i(0, 0, 2), allDirections, new Vector3i(0, 0, 1), allDirections);

        assertFalse(network.canAddNetworkingNode(new Vector3i(0, 0, 0), allDirections));
    }

    @Test
    public void removeLeafNodeFromConnectedNetworkWithNetworkingNode() {
        network.addNetworkingNode(new Vector3i(0, 0, 1), allDirections);
        network.addLeafNode(new Vector3i(0, 0, 0), allDirections);

        network.removeLeafNode(new Vector3i(0, 0, 0), allDirections);
        assertEquals(1, network.getNetworkSize());
    }

    @Test
    public void removeLeafNodeFromConnectedNetworkWithOnlyLeafNodes() {
        network = SimpleNetwork.createDegenerateNetwork(new Vector3i(0, 0, 0), allDirections, new Vector3i(0, 0, 1), allDirections);

        assertTrue(network.removeLeafNode(new Vector3i(0, 0, 0), allDirections));
        assertEquals(1, network.getNetworkSize());
    }

    @Test
    public void distanceForSameLeafNode() {
        network.addNetworkingNode(new Vector3i(0, 0, 1), allDirections);
        network.addLeafNode(new Vector3i(0, 0, 0), allDirections);

        assertTrue(network.isInDistance(0, new Vector3i(0, 0, 0), allDirections, new Vector3i(0, 0, 0), allDirections));
        assertEquals(0, network.getDistance(new Vector3i(0, 0, 0), allDirections, new Vector3i(0, 0, 0), allDirections));
    }
    
    @Test
    public void distanceForDegeneratedNetwork() {
        network = SimpleNetwork.createDegenerateNetwork(new Vector3i(0, 0, 0), allDirections, new Vector3i(0, 0, 1), allDirections);

        assertTrue(network.isInDistance(1, new Vector3i(0, 0, 0), allDirections, new Vector3i(0, 0, 1), allDirections));
        assertEquals(1, network.getDistance(new Vector3i(0, 0, 0), allDirections, new Vector3i(0, 0, 1), allDirections));
    }

    @Test
    public void distanceForTwoLeafNodesOnNetwork() {
        network.addNetworkingNode(new Vector3i(0, 0, 1), allDirections);
        network.addLeafNode(new Vector3i(0, 0, 2), allDirections);
        network.addLeafNode(new Vector3i(0, 0, 0), allDirections);

        assertTrue(network.isInDistance(2, new Vector3i(0, 0, 0), allDirections, new Vector3i(0, 0, 2), allDirections));
        assertFalse(network.isInDistance(1, new Vector3i(0, 0, 0), allDirections, new Vector3i(0, 0, 2), allDirections));
        assertEquals(2, network.getDistance(new Vector3i(0, 0, 0), allDirections, new Vector3i(0, 0, 2), allDirections));
    }

    @Test
    public void distanceForLongNetwork() {
        for (int i=0; i<10; i++)
            network.addNetworkingNode(new Vector3i(0, 0, i), allDirections);

        assertTrue(network.isInDistance(9, new Vector3i(0, 0, 0), allDirections, new Vector3i(0, 0, 9), allDirections));
        assertFalse(network.isInDistance(8, new Vector3i(0, 0, 0), allDirections, new Vector3i(0, 0, 9), allDirections));
        assertEquals(9, network.getDistance(new Vector3i(0, 0, 0), allDirections, new Vector3i(0, 0, 9), allDirections));
    }

    @Test
    public void distanceForBranchedNetwork() {
        for (int i=0; i<10; i++)
            network.addNetworkingNode(new Vector3i(0, 0, i), allDirections);

        for (int i=1; i<=5; i++)
            network.addNetworkingNode(new Vector3i(i, 0, 5), allDirections);

        assertTrue(network.isInDistance(10, new Vector3i(0, 0, 0), allDirections, new Vector3i(5, 0, 5), allDirections));
        assertFalse(network.isInDistance(9, new Vector3i(0, 0, 0), allDirections, new Vector3i(5, 0, 5), allDirections));
        assertEquals(10, network.getDistance(new Vector3i(0, 0, 0), allDirections, new Vector3i(5, 0, 5), allDirections));

    }
}
