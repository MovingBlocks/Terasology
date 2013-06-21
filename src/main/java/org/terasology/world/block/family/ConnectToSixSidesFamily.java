package org.terasology.world.block.family;

import gnu.trove.map.TByteObjectMap;
import org.terasology.math.Sides;
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
    private TByteObjectMap<Block> blocks;

    public ConnectToSixSidesFamily(ConnectionCondition connectionCondition, BlockUri blockUri, List<String> categories, Block archetypeBlock, TByteObjectMap<Block> blocks) {
        super(blockUri, categories);
        this.connectionCondition = connectionCondition;
        this.archetypeBlock = archetypeBlock;
        this.blocks = blocks;

        for (Block block : blocks.valueCollection()) {
            block.setBlockFamily(this);
        }
    }

    @Override
    public Block getArchetypeBlock() {
        return archetypeBlock;
    }

    @Override
    public Block getBlockUponPlacement(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i location, Side attachmentSide, Side direction) {
        byte connections = 0;
        for (Side connectSide : Side.values()) {
            if (connectionCondition.isConnectingTo(location, connectSide, worldProvider, blockEntityRegistry)) {
                connections += Sides.getSide(connectSide);
            }
        }
        return blocks.get(connections);
    }

    @Override
    public Block getBlockUponNeighborUpdate(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i location, Block oldBlock) {
        byte connections = 0;
        for (Side connectSide : Side.values()) {
            if (connectionCondition.isConnectingTo(location, connectSide, worldProvider, blockEntityRegistry)) {
                connections += Sides.getSide(connectSide);
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
        return blocks.valueCollection();
    }
}
