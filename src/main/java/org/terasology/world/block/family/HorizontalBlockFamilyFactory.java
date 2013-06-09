package org.terasology.world.block.family;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import org.terasology.asset.AssetUri;
import org.terasology.math.Side;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockDefinition;
import org.terasology.world.block.loader.BlockLoader;

import java.util.Map;

@RegisterBlockFamilyFactory(id="Horizontal")
public class HorizontalBlockFamilyFactory implements BlockFamilyFactory {
    @Override
    public BlockFamily createBlockFamily(BlockLoader blockLoader, AssetUri blockDefUri, BlockDefinition blockDefinition, JsonObject blockDefJson) {
        Map<Side, Block> blockMap = Maps.newEnumMap(Side.class);
        blockLoader.constructHorizontalBlocks(blockDefUri, blockDefinition, blockMap);

        final String[] categories = blockLoader.getCategories(blockDefinition);
        return new HorizontalBlockFamily(new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName()), blockMap, categories);
    }
}
