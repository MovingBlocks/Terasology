package org.terasology.blockNetwork;

import org.junit.Before;
import org.junit.Test;
import org.terasology.math.Direction;
import org.terasology.math.Vector3i;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.HashMultimap;

public class BlockNetworkTest {
    private BlockNetwork blockNetwork;
    private TestListener listener;
    private byte allDirections;

    @Before
    public void setup() {
        blockNetwork = new BlockNetwork();
        listener = new TestListener();
        blockNetwork.addTopologyListener(listener);
        blockNetwork.addTopologyListener(new ValidatingListener());
        allDirections = DirectionsUtil.addDirection((byte) 0, Direction.UP, Direction.LEFT, Direction.FORWARD, Direction.DOWN, Direction.RIGHT, Direction.BACKWARD);
    }

    private NetworkNode toNode(Vector3i location, byte directions) {
        return new NetworkNode(location, directions);
    }

    @Test
    public void addAndRemoveNetworkingBlock() {
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(1, blockNetwork.getNetworks().size());
        assertEquals(1, listener.networksAdded);

        blockNetwork.removeNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
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
        assertTrue(network.hasLeafNode(toNode(new Vector3i(0, 0, 1), allDirections)));
        assertTrue(network.hasNetworkingNode(toNode(new Vector3i(0, 0, 0), allDirections)));

        assertEquals(1, listener.networksAdded);
    }

    @Test
    public void addTwoNeighbouringNetworkingBlocks() {
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(1, listener.networksAdded);
        
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 1), allDirections);
        assertEquals(1, blockNetwork.getNetworks().size());

        assertEquals(1, listener.networksAdded);
        assertEquals(2, listener.networkingNodesAdded);
    }

    @Test
    public void newNetworkingNodeJoinsLeafNodeIntoExistingNetwork() {
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 1), allDirections);
        blockNetwork.addLeafBlock(new Vector3i(1, 0, 0), allDirections);
        assertEquals(1, blockNetwork.getNetworks().size());
        Network network = blockNetwork.getNetworks().iterator().next();
        assertFalse(network.hasLeafNode(toNode(new Vector3i(1, 0, 0), allDirections)));
        assertTrue(network.hasNetworkingNode(toNode(new Vector3i(0, 0, 1), allDirections)));
        assertEquals(1, listener.networksAdded);

        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(1, blockNetwork.getNetworks().size());
        assertTrue(network.hasNetworkingNode(toNode(new Vector3i(0, 0, 0), allDirections)));
        assertTrue(network.hasNetworkingNode(toNode(new Vector3i(0, 0, 1), allDirections)));
        assertTrue(network.hasLeafNode(toNode(new Vector3i(1, 0, 0), allDirections)));
        assertEquals(1, listener.networksAdded);
        assertEquals(2, listener.networkingNodesAdded);
        assertEquals(1, listener.leafNodesAdded);
    }

    @Test
    public void removingNetworkingNodeSplitsNetworkInTwo() {
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 1), allDirections);
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, -1), allDirections);
        assertEquals(1, blockNetwork.getNetworks().size());
        assertEquals(1, listener.networksAdded);

        blockNetwork.removeNetworkingBlock(new Vector3i(0, 0, 0), allDirections);
        assertEquals(2, blockNetwork.getNetworks().size());
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
        assertTrue(network.hasNetworkingNode(toNode(new Vector3i(0, 0, -1), allDirections)));
        assertTrue(network.hasNetworkingNode(toNode(new Vector3i(0, 0, 0), allDirections)));
        assertTrue(network.hasNetworkingNode(toNode(new Vector3i(0, 0, 1), allDirections)));
        assertEquals(1, listener.networksRemoved);
    }

    @Test
    public void addLeafNetworkingLeaf() {
        blockNetwork.addLeafBlock(new Vector3i(0, 0, 2), allDirections);
        blockNetwork.addNetworkingBlock(new Vector3i(0, 0, 1), allDirections);
        blockNetwork.addLeafBlock(new Vector3i(0, 0, 0), allDirections);

        blockNetwork.removeLeafBlock(new Vector3i(0, 0, 0), allDirections);
        blockNetwork.addLeafBlock(new Vector3i(0, 0, 0), allDirections);

        assertEquals(1, blockNetwork.getNetworks().size());

        Network network = blockNetwork.getNetworks().iterator().next();
        assertTrue(network.hasLeafNode(toNode(new Vector3i(0, 0, 0), allDirections)));
        assertTrue(network.hasNetworkingNode(toNode(new Vector3i(0, 0, 1), allDirections)));
        assertTrue(network.hasLeafNode(toNode(new Vector3i(0, 0, 2), allDirections)));
    }

    private class TestListener implements NetworkTopologyListener {
        public int networksAdded;
        public int networksRemoved;
        public int networkingNodesAdded;
        public int networkingNodesRemoved;
        public int leafNodesAdded;
        public int leafNodesRemoved;

        public void reset() {
            networksAdded = 0;
            networksRemoved = 0;
            networkingNodesAdded=0;
            networkingNodesRemoved=0;
            leafNodesAdded=0;
            leafNodesRemoved=0;
        }

        @Override
        public void networkAdded(Network newNetwork) {
            networksAdded++;
        }

        @Override
        public void networkRemoved(Network network) {
            networksRemoved++;
        }

        @Override
        public void networkingNodesAdded(Network network, Set<NetworkNode> networkingNodes) {
            networkingNodesAdded++;
        }

        @Override
        public void networkingNodesRemoved(Network network, Set<NetworkNode> networkingNodes) {
            networkingNodesRemoved++;
        }

        @Override
        public void leafNodesAdded(Network network, Set<NetworkNode> leafNodes) {
            leafNodesAdded++;
        }

        @Override
        public void leafNodesRemoved(Network network, Set<NetworkNode> leafNodes) {
            leafNodesRemoved++;
        }
    }

    private class ValidatingListener implements NetworkTopologyListener {
        private Set<Network> networks = Sets.newHashSet();
        private Multimap<Network, NetworkNode> networkingNodes = HashMultimap.create();
        private Multimap<Network, NetworkNode> leafNodes = HashMultimap.create();

        @Override
        public void networkAdded(Network network) {
            assertTrue(networks.add(network));
        }

        @Override
        public void networkingNodesAdded(Network network, Set<NetworkNode> networkingNodes) {
            assertTrue(networks.contains(network));
            for (NetworkNode networkingNode : networkingNodes)
                assertTrue(this.networkingNodes.put(network, networkingNode));
        }

        @Override
        public void networkingNodesRemoved(Network network, Set<NetworkNode> networkingNodes) {
            assertTrue(networks.contains(network));
            for (NetworkNode networkingNode : networkingNodes)
                assertTrue(this.networkingNodes.remove(network, networkingNode));
        }

        @Override
        public void leafNodesAdded(Network network, Set<NetworkNode> leafNodes) {
            assertTrue(networks.contains(network));
            for (NetworkNode leafNode : leafNodes)
                assertTrue(this.leafNodes.put(network, leafNode));
        }

        @Override
        public void leafNodesRemoved(Network network, Set<NetworkNode> leafNodes) {
            assertTrue(networks.contains(network));
            for (NetworkNode leafNode : leafNodes)
                assertTrue(this.leafNodes.remove(network, leafNode));
        }

        @Override
        public void networkRemoved(Network network) {
            assertFalse(networkingNodes.containsKey(network));
            assertFalse(leafNodes.containsKey(network));
            assertTrue(networks.remove(network));
        }
    }
}
