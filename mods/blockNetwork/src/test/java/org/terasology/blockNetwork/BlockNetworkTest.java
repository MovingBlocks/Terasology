package org.terasology.blockNetwork;

import org.junit.Before;
import org.junit.Test;
import org.terasology.math.Direction;
import org.terasology.math.Vector3i;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BlockNetworkTest {
    private BlockNetwork blockNetwork;
    private TestListener listener;
    private byte allDirections;

    @Before
    public void setup() {
        blockNetwork = new BlockNetwork();
        listener = new TestListener();
        blockNetwork.addTopologyListener(listener);
        allDirections = DirectionsUtil.addDirection((byte) 0, Direction.UP, Direction.LEFT, Direction.FORWARD, Direction.DOWN, Direction.RIGHT, Direction.BACKWARD);
    }

    @Test
    public void addAndRemoveNetworkingBlock() {
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(1, blockNetwork.getNetworks().size());
        assertEquals(1, listener.networksAdded);

        blockNetwork.removeNetworkingBlock(new Vector3i(0, 0, 0));
        assertEquals(0, blockNetwork.getNetworks().size());
        assertEquals(1, listener.networksRemoved);
    }

    @Test
    public void addAndRemoveLeafBlock() {
        blockNetwork.addLeafBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(0, blockNetwork.getNetworks().size());

        blockNetwork.removeLeafBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(0, blockNetwork.getNetworks().size());

        assertEquals(0, listener.networksAdded);
        assertEquals(0, listener.networksRemoved);
    }

    @Test
    public void addTwoNeighbouringLeafBlocks() {
        blockNetwork.addLeafBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(0, listener.networksAdded);
        
        blockNetwork.addLeafBlock(new Vector3i(0, 0, 1), allDirections);
        assertEquals(1, blockNetwork.getNetworks().size());
        assertEquals(1, listener.networksAdded);
    }

    @Test
    public void addLeafNodeThenNetworkingNode() {
        blockNetwork.addLeafBlock(new Vector3i(0, 0, 1), allDirections);
        assertEquals(0, listener.networksAdded);
        
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(1, blockNetwork.getNetworks().size());
        Network network = blockNetwork.getNetworks().iterator().next();
        assertTrue(network.hasLeafNode(new Vector3i(0, 0, 1), allDirections));
        assertTrue(network.hasNetworkingNode(new Vector3i(0, 0, 0)));

        assertEquals(1, listener.networksAdded);
    }

    @Test
    public void addTwoNeighbouringNetworkingBlocks() {
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(1, listener.networksAdded);
        
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 1), allDirections);
        assertEquals(1, blockNetwork.getNetworks().size());

        assertEquals(1, listener.networksUpdated);
    }

    @Test
    public void newNetworkingNodeJoinsLeafNodeIntoExistingNetwork() {
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 1), allDirections);
        blockNetwork.addLeafBlock(new Vector3i(1, 0, 0), allDirections);
        assertEquals(1, blockNetwork.getNetworks().size());
        Network network = blockNetwork.getNetworks().iterator().next();
        assertFalse(network.hasLeafNode(new Vector3i(1, 0, 0), allDirections));
        assertTrue(network.hasNetworkingNode(new Vector3i(0, 0, 1)));
        assertEquals(1, listener.networksAdded);

        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(1, blockNetwork.getNetworks().size());
        assertTrue(network.hasNetworkingNode(new Vector3i(0, 0, 0)));
        assertTrue(network.hasNetworkingNode(new Vector3i(0, 0, 1)));
        assertTrue(network.hasLeafNode(new Vector3i(1, 0, 0), allDirections));
        assertEquals(1, listener.networksUpdated);
    }

    @Test
    public void removingNetworkingNodeSplitsNetworkInTwo() {
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 1), allDirections);
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, -1), allDirections);
        assertEquals(1, blockNetwork.getNetworks().size());
        assertEquals(1, listener.networksAdded);

        blockNetwork.removeNetworkingBlock(new Vector3i(0, 0, 0));
        assertEquals(2, blockNetwork.getNetworks().size());
        assertEquals(1, listener.networkSplits);
    }

    @Test
    public void addingNetworkingNodeJoinsExistingNetworks() {
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 1), allDirections);
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, -1), allDirections);
        assertEquals(2, blockNetwork.getNetworks().size());
        assertEquals(2, listener.networksAdded);

        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(1, blockNetwork.getNetworks().size());
        Network network = blockNetwork.getNetworks().iterator().next();
        assertTrue(network.hasNetworkingNode(new Vector3i(0, 0, -1)));
        assertTrue(network.hasNetworkingNode(new Vector3i(0, 0, 0)));
        assertTrue(network.hasNetworkingNode(new Vector3i(0, 0, 1)));
        assertEquals(1, listener.networkMerges);
    }

    private class TestListener implements BlockNetworkTopologyListener {
        public int networksAdded;
        public int networksUpdated;
        public int networksRemoved;
        public int networkSplits;
        public int networkMerges;

        public void reset() {
            networksAdded = 0;
            networksUpdated = 0;
            networksRemoved = 0;
            networkSplits = 0;
            networkMerges = 0;
        }

        @Override
        public void networkAdded(Network newNetwork) {
            networksAdded++;
        }

        @Override
        public void networkUpdated(Network network) {
            networksUpdated++;
        }

        @Override
        public void networkRemoved(Network network) {
            networksRemoved++;
        }

        @Override
        public void networkSplit(Network sourceNetwork, Collection<? extends Network> resultNetwork) {
            networkSplits++;
        }

        @Override
        public void networksMerged(Network mainNetwork, Network mergedNetwork) {
            networkMerges++;
        }
    }
}
