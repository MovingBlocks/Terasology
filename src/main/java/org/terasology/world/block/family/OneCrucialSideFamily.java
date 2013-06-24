package org.terasology.world.block.family;

import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;

import java.util.Locale;
import java.util.Map;

public class OneCrucialSideFamily extends AbstractBlockFamily {
    private Block archetypeBlock;
    private Map<Side, Block> crucialSideBlocks;

    public OneCrucialSideFamily(BlockUri uri, Iterable<String> categories, Block archetypeBlock, Map<Side, Block> crucialSideBlocks) {
        super(uri, categories);

        for (Map.Entry<Side, Block> blockBySide : crucialSideBlocks.entrySet()){
            final Side side = blockBySide.getKey();
            final Block block = blockBySide.getValue();
            if (block == null) {
                throw new IllegalArgumentException("Missing block for side: " + side.toString());
            }
            block.setBlockFamily(this);
            block.setUri(new BlockUri(uri, side.name()));
        }

        this.archetypeBlock = archetypeBlock;
        this.crucialSideBlocks = crucialSideBlocks;
    }

    @Override
    public Block getArchetypeBlock() {
        return archetypeBlock;
    }

    @Override
    public Block getBlockForPlacement(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i location, Side attachmentSide, Side direction) {
        return crucialSideBlocks.get(attachmentSide);
    }

    @Override
    public Block getBlockForNeighborUpdate(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i location, Block oldBlock) {
        return oldBlock;
    }

    public Side getBlockSide(Block block) {
        for (Map.Entry<Side, Block> blockBySide : crucialSideBlocks.entrySet()){
            if (blockBySide.getValue() == block)
                return blockBySide.getKey();
        }
        return null;
    }

    public Block getBlockForSide(Side side) {
        return crucialSideBlocks.get(side);
    }

    @Override
    public Block getBlockFor(BlockUri blockUri) {
        if (getURI().equals(blockUri.getFamilyUri())) {
            try {
                Side side = Side.valueOf(blockUri.getIdentifier().toUpperCase(Locale.ENGLISH));
                return crucialSideBlocks.get(side);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Iterable<Block> getBlocks() {
        return crucialSideBlocks.values();
    }
}
