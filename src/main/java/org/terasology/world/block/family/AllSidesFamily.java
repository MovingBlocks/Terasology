package org.terasology.world.block.family;

import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;

import java.util.Locale;
import java.util.Map;

public class AllSidesFamily extends AbstractBlockFamily implements SideDefinedBlockFamily {
    private Block archetypeBlock;
    private Map<Side, Block> sideBlocks;

    public AllSidesFamily(BlockUri uri, Iterable<String> categories, Block archetypeBlock, Map<Side, Block> sideBlocks) {
        super(uri, categories);

        for (Map.Entry<Side, Block> blockBySide : sideBlocks.entrySet()){
            final Side side = blockBySide.getKey();
            final Block block = blockBySide.getValue();
            if (block == null) {
                throw new IllegalArgumentException("Missing block for side: " + side.toString());
            }
            block.setBlockFamily(this);
            block.setUri(new BlockUri(uri, side.name()));
        }

        this.archetypeBlock = archetypeBlock;
        this.sideBlocks = sideBlocks;
    }

    @Override
    public Block getArchetypeBlock() {
        return archetypeBlock;
    }

    @Override
    public Block getBlockForPlacement(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i location, Side attachmentSide, Side direction) {
        return sideBlocks.get(Side.FRONT);
    }

    @Override
    public Block getBlockForNeighborUpdate(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i location, Block oldBlock) {
        return oldBlock;
    }

    @Override
    public Side getBlockSide(Block block) {
        for (Map.Entry<Side, Block> blockBySide : sideBlocks.entrySet()){
            if (blockBySide.getValue() == block)
                return blockBySide.getKey();
        }
        return null;
    }

    @Override
    public Block getBlockForSide(Side side) {
        return sideBlocks.get(side);
    }

    @Override
    public Block getBlockFor(BlockUri blockUri) {
        if (getURI().equals(blockUri.getFamilyUri())) {
            try {
                Side side = Side.valueOf(blockUri.getIdentifier().toUpperCase(Locale.ENGLISH));
                return sideBlocks.get(side);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Iterable<Block> getBlocks() {
        return sideBlocks.values();
    }
}
