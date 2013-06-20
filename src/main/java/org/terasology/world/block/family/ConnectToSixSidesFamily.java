package org.terasology.world.block.family;

import org.terasology.math.Direction;
import org.terasology.math.DirectionsUtil;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;

import java.util.List;
import java.util.Map;

public class ConnectToSixSidesFamily extends AbstractBlockFamily {
    private ConnectionCondition connectionCondition;
    private Block archetypeBlock;
    private Map<Byte, Block> blocks;

    public ConnectToSixSidesFamily(ConnectionCondition connectionCondition, BlockUri blockUri, List<String> categories, Block archetypeBlock, Map<Byte, Block> blocks) {
        super(blockUri, categories);
        this.connectionCondition = connectionCondition;
        this.archetypeBlock = archetypeBlock;
        this.blocks = blocks;
    }

    @Override
    public Block getArchetypeBlock() {
        return archetypeBlock;
    }

    @Override
    public Block getBlockFor(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i location, Side attachmentSide, Side direction) {
        byte connections = 0;
        for (Direction connectDirection : Direction.values()) {
            if (connectionCondition.isConnectingTo(location, connectDirection, worldProvider, blockEntityRegistry)) {
                connections += DirectionsUtil.getDirection(connectDirection);
            }
        }
        return blocks.get(connections);
    }

    @Override
    public Block getBlockFor(BlockUri blockUri) {
        if (getURI().equals(blockUri.getFamilyUri())) {
            try {
                byte connections = Byte.parseByte(blockUri.getIdentifier());
                return blocks.get(connections);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Iterable<Block> getBlocks() {
        return blocks.values();
    }
}
