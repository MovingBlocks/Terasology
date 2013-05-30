package org.terasology.blockNetwork;

import org.junit.Before;
import org.junit.Test;
import org.terasology.math.Direction;
import org.terasology.math.Vector3i;

import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class BlockNetworkTest {
    private BlockNetwork blockNetwork;
    private Collection<Direction> allDirections;

    @Before
    public void setup() {
        blockNetwork = new BlockNetwork();
        allDirections = new HashSet<Direction>();
        allDirections.add(Direction.UP);
        allDirections.add(Direction.LEFT);
        allDirections.add(Direction.FORWARD);
        allDirections.add(Direction.DOWN);
        allDirections.add(Direction.RIGHT);
        allDirections.add(Direction.BACKWARD);
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
}
