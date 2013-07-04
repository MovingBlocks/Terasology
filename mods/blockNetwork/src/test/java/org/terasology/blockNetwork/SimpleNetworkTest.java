package org.terasology.blockNetwork;

import org.junit.Before;
import org.junit.Test;
import org.terasology.math.Side;
import org.terasology.math.SideBitFlag;
import org.terasology.math.Vector3i;

import static org.junit.Assert.*;

public class SimpleNetworkTest {
    private SimpleNetwork network;
    private byte allDirections;
    private byte upOnly;

    @Before
    public void setup() {
        network = new SimpleNetwork();
        allDirections = 63;
        upOnly = SideBitFlag.addSide((byte) 0, Side.TOP);
    }

    private NetworkNode toNode(Vector3i location, byte sides) {
        return new NetworkNode(location, sides);
    }

    @Test
    public void addNetworkingNodeToEmptyNetwork() {
        assertTrue(network.canAddNetworkingNode(toNode(new Vector3i(0, 0, 0), allDirections)));
        network.addNetworkingNode(toNode(new Vector3i(0, 0, 0), allDirections));
        assertEquals(1, network.getNetworkSize());
    }

    @Test
    public void cantAddLeafNodeToEmptyNetwork() {
        assertFalse(network.canAddLeafNode(toNode(new Vector3i(0, 0, 0), allDirections)));
    }

    @Test
    public void addingLeafNodeToNetworkingNode() {
        network.addNetworkingNode(toNode(new Vector3i(0, 0, 1), allDirections));

        assertTrue(network.canAddLeafNode(toNode(new Vector3i(0, 0, 0), allDirections)));
        network.addLeafNode(toNode(new Vector3i(0, 0, 0), allDirections));
        assertEquals(2, network.getNetworkSize());
    }

    @Test
    public void creatingDegenerateNetwork() {
        network = SimpleNetwork.createDegenerateNetwork(toNode(new Vector3i(0, 0, 1), allDirections), toNode(new Vector3i(0, 0, 0), allDirections));
        assertEquals(2, network.getNetworkSize());
    }

    @Test
    public void cantAddNetworkingNodeToDegeneratedNetwork() {
        network = SimpleNetwork.createDegenerateNetwork(toNode(new Vector3i(0, 0, 1), allDirections), toNode(new Vector3i(0, 0, 0), allDirections));
        assertFalse(network.canAddNetworkingNode(toNode(new Vector3i(0, 0, 2), allDirections)));
    }

    @Test
    public void cantAddLeafNodeToDegeneratedNetwork() {
        network = SimpleNetwork.createDegenerateNetwork(toNode(new Vector3i(0, 0, 1), allDirections), toNode(new Vector3i(0, 0, 0), allDirections));
        assertFalse(network.canAddLeafNode(toNode(new Vector3i(0, 0, 2), allDirections)));
    }

    @Test
    public void addingNetworkingNodeToNetworkingNode() {
        network.addNetworkingNode(toNode(new Vector3i(0, 0, 1), allDirections));

        assertTrue(network.canAddNetworkingNode(toNode(new Vector3i(0, 0, 0), allDirections)));
        network.addNetworkingNode(toNode(new Vector3i(0, 0, 0), allDirections));
        assertEquals(2, network.getNetworkSize());
    }

    @Test
    public void cantAddNodeToNetworkingNodeTooFar() {
        network.addNetworkingNode(toNode(new Vector3i(0, 0, 2), allDirections));

        assertFalse(network.canAddNetworkingNode(toNode(new Vector3i(0, 0, 0), allDirections)));
    }

    @Test
    public void cantAddNodeToNetworkingNodeWrongDirection() {
        network.addNetworkingNode(toNode(new Vector3i(0, 0, 1), upOnly));

        assertFalse(network.canAddNetworkingNode(toNode(new Vector3i(0, 0, 0), allDirections)));
    }

    @Test
    public void cantAddNodeToNetworkOnTheSideOfConnectedLeaf() {
        network.addNetworkingNode(toNode(new Vector3i(0, 0, 2), allDirections));
        network.addLeafNode(toNode(new Vector3i(0, 0, 1), allDirections));

        assertFalse(network.canAddNetworkingNode(toNode(new Vector3i(0, 0, 0), allDirections)));
    }

    @Test
    public void canAddLeafNodeOnTheSideOfConnectedNetworkingNode() {
        network.addNetworkingNode(toNode(new Vector3i(0, 0, 1), allDirections));
        network.addLeafNode(toNode(new Vector3i(0, 0, 2), allDirections));

        assertTrue(network.canAddLeafNode(toNode(new Vector3i(0, 0, 0), allDirections)));
        network.addLeafNode(toNode(new Vector3i(0, 0, 0), allDirections));
    }

    @Test
    public void canAddNetworkingNodeOnTheSideOfConnectedNetworkingNode() {
        network.addNetworkingNode(toNode(new Vector3i(0, 0, 1), allDirections));
        network.addLeafNode(toNode(new Vector3i(0, 0, 2), allDirections));

        assertTrue(network.canAddNetworkingNode(toNode(new Vector3i(0, 0, 0), allDirections)));
        network.addNetworkingNode(toNode(new Vector3i(0, 0, 0), allDirections));
        assertEquals(3, network.getNetworkSize());
    }

    @Test
    public void cantAddNodeToNetworkWithTwoLeafNodes() {
        network = SimpleNetwork.createDegenerateNetwork(toNode(new Vector3i(0, 0, 2), allDirections), toNode(new Vector3i(0, 0, 1), allDirections));

        assertFalse(network.canAddNetworkingNode(toNode(new Vector3i(0, 0, 0), allDirections)));
    }

    @Test
    public void removeLeafNodeFromConnectedNetworkWithNetworkingNode() {
        network.addNetworkingNode(toNode(new Vector3i(0, 0, 1), allDirections));
        network.addLeafNode(toNode(new Vector3i(0, 0, 0), allDirections));

        network.removeLeafNode(toNode(new Vector3i(0, 0, 0), allDirections));
        assertEquals(1, network.getNetworkSize());
    }

    @Test
    public void removeLeafNodeFromConnectedNetworkWithOnlyLeafNodes() {
        network = SimpleNetwork.createDegenerateNetwork(toNode(new Vector3i(0, 0, 0), allDirections), toNode(new Vector3i(0, 0, 1), allDirections));

        assertTrue(network.removeLeafNode(toNode(new Vector3i(0, 0, 0), allDirections)));
        assertEquals(1, network.getNetworkSize());
    }

    @Test
    public void distanceForSameLeafNode() {
        network.addNetworkingNode(toNode(new Vector3i(0, 0, 1), allDirections));
        network.addLeafNode(toNode(new Vector3i(0, 0, 0), allDirections));

        assertTrue(network.isInDistance(0, toNode(new Vector3i(0, 0, 0), allDirections), toNode(new Vector3i(0, 0, 0), allDirections)));
        assertEquals(0, network.getDistance(toNode(new Vector3i(0, 0, 0), allDirections), toNode(new Vector3i(0, 0, 0), allDirections)));
    }
    
    @Test
    public void distanceForDegeneratedNetwork() {
        network = SimpleNetwork.createDegenerateNetwork(toNode(new Vector3i(0, 0, 0), allDirections), toNode(new Vector3i(0, 0, 1), allDirections));

        assertTrue(network.isInDistance(1, toNode(new Vector3i(0, 0, 0), allDirections), toNode(new Vector3i(0, 0, 1), allDirections)));
        assertEquals(1, network.getDistance(toNode(new Vector3i(0, 0, 0), allDirections), toNode(new Vector3i(0, 0, 1), allDirections)));
    }

    @Test
    public void distanceForTwoLeafNodesOnNetwork() {
        NetworkNode firstLeaf = toNode(new Vector3i(0, 0, 0), allDirections);
        NetworkNode secondLeaf = toNode(new Vector3i(0, 0, 2), allDirections);
        network.addNetworkingNode(toNode(new Vector3i(0, 0, 1), allDirections));
        network.addLeafNode(secondLeaf);
        network.addLeafNode(firstLeaf);

        assertTrue(network.isInDistance(2, firstLeaf, secondLeaf));
        assertFalse(network.isInDistance(1, firstLeaf, secondLeaf));
        assertEquals(2, network.getDistance(firstLeaf, secondLeaf));
    }

    @Test
    public void distanceFromDifferentSides() {
        NetworkNode firstLeaf = toNode(new Vector3i(0, 0, 0), allDirections);
        NetworkNode secondLeaf = toNode(new Vector3i(0, 0, 2), allDirections);
        network.addNetworkingNode(toNode(new Vector3i(0, 0, 1), allDirections));
        network.addNetworkingNode(toNode(new Vector3i(0, 1, 1), allDirections));
        network.addNetworkingNode(toNode(new Vector3i(0, 1, 2), allDirections));
        network.addLeafNode(secondLeaf);
        network.addLeafNode(firstLeaf);

        assertTrue(network.isInDistanceWithSide(2, firstLeaf, secondLeaf, Side.FRONT));
        assertFalse(network.isInDistanceWithSide(2, firstLeaf, secondLeaf, Side.TOP));
        assertFalse(network.isInDistanceWithSide(3, firstLeaf, secondLeaf, Side.TOP));
        assertTrue(network.isInDistanceWithSide(4, firstLeaf, secondLeaf, Side.TOP));
    }

    @Test
    public void distanceForLongNetwork() {
        for (int i=0; i<10; i++)
            network.addNetworkingNode(toNode(new Vector3i(0, 0, i), allDirections));

        assertTrue(network.isInDistance(9, toNode(new Vector3i(0, 0, 0), allDirections), toNode(new Vector3i(0, 0, 9), allDirections)));
        assertFalse(network.isInDistance(8, toNode(new Vector3i(0, 0, 0), allDirections), toNode(new Vector3i(0, 0, 9), allDirections)));
        assertEquals(9, network.getDistance(toNode(new Vector3i(0, 0, 0), allDirections), toNode(new Vector3i(0, 0, 9), allDirections)));
    }

    @Test
    public void distanceForBranchedNetwork() {
        for (int i=0; i<10; i++)
            network.addNetworkingNode(toNode(new Vector3i(0, 0, i), allDirections));

        for (int i=1; i<=5; i++)
            network.addNetworkingNode(toNode(new Vector3i(i, 0, 5), allDirections));

        assertTrue(network.isInDistance(10, toNode(new Vector3i(0, 0, 0), allDirections), toNode(new Vector3i(5, 0, 5), allDirections)));
        assertFalse(network.isInDistance(9, toNode(new Vector3i(0, 0, 0), allDirections), toNode(new Vector3i(5, 0, 5), allDirections)));
        assertEquals(10, network.getDistance(toNode(new Vector3i(0, 0, 0), allDirections), toNode(new Vector3i(5, 0, 5), allDirections)));

    }
}
