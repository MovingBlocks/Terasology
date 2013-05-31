package org.terasology.blockNetwork;

import org.junit.Before;
import org.junit.Test;
import org.terasology.math.Direction;
import org.terasology.math.Vector3i;

import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BlockNetworkTest {
    private BlockNetwork blockNetwork;
    private byte allDirections;

    @Before
    public void setup() {
        blockNetwork = new BlockNetwork();
        allDirections = DirectionsUtil.addDirection((byte) 0, Direction.UP, Direction.LEFT, Direction.FORWARD, Direction.DOWN, Direction.RIGHT, Direction.BACKWARD);
    }

    @Test
    public void addAndRemoveNetworkingBlock() {
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(1, blockNetwork.getNetworks().size());
        blockNetwork.removeNetworkingBlock(new Vector3i(0, 0, 0));
        assertEquals(0, blockNetwork.getNetworks().size());
    }
    
    @Test
    public void addAndRemoveLeafBlock() {
        blockNetwork.addLeafBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(0, blockNetwork.getNetworks().size());
        blockNetwork.removeLeafBlock(new Vector3i(0, 0, 0));
        assertEquals(0, blockNetwork.getNetworks().size());
    }
    
    @Test
    public void addTwoNeighbouringLeafBlocks() {
        blockNetwork.addLeafBlock(new Vector3i(0, 0, 0), allDirections);
        blockNetwork.addLeafBlock(new Vector3i(0, 0, 1), allDirections);
        assertEquals(1, blockNetwork.getNetworks().size());
    }

    @Test
    public void addLeafNodeThenNetworkingNode() {
        blockNetwork.addLeafBlock(new Vector3i(0, 0, 1), allDirections);
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(1, blockNetwork.getNetworks().size());
        Network network = blockNetwork.getNetworks().iterator().next();
        assertTrue(network.hasLeafNode(new Vector3i(0, 0, 1)));
        assertTrue(network.hasNetworkingNode(new Vector3i(0, 0, 0)));
    }

    @Test
    public void newNetworkingNodeJoinsLeafNodeIntoExistingNetwork() {
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 1), allDirections);
        blockNetwork.addLeafBlock(new Vector3i(1, 0, 0), allDirections);
        assertEquals(1, blockNetwork.getNetworks().size());
        Network network = blockNetwork.getNetworks().iterator().next();
        assertFalse(network.hasLeafNode(new Vector3i(1, 0, 0)));
        assertTrue(network.hasNetworkingNode(new Vector3i(0, 0, 1)));

        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(1, blockNetwork.getNetworks().size());
        assertTrue(network.hasNetworkingNode(new Vector3i(0, 0, 0)));
        assertTrue(network.hasNetworkingNode(new Vector3i(0, 0, 1)));
        assertTrue(network.hasLeafNode(new Vector3i(1, 0, 0)));
    }
}
