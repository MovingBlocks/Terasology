package org.terasology.world.block.family;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import org.terasology.asset.AssetUri;
import org.terasology.math.Side;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockDefinition;

import java.util.Map;

@RegisterBlockFamilyFactory("horizontal")
public class HorizontalBlockFamilyFactory implements BlockFamilyFactory {
    @Override
    public BlockFamily createBlockFamily(BlockBuilderHelper blockBuilder, AssetUri blockDefUri, BlockDefinition blockDef, JsonObject blockDefJson) {
        Map<Side, Block> blockMap = Maps.newHashMap();
        blockMap.putAll(blockBuilder.constructHorizontalRotatedBlocks(blockDefUri, blockDef));

        return new HorizontalBlockFamily(new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName()), blockMap, blockDef.categories);
    }
}
