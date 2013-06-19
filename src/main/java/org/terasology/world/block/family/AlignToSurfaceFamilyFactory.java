package org.terasology.world.block.family;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import org.terasology.asset.AssetUri;
import org.terasology.math.Side;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockDefinition;

import java.util.List;
import java.util.Map;

@RegisterBlockFamilyFactory("alignToSurface")
public class AlignToSurfaceFamilyFactory implements BlockFamilyFactory {

    private static final String TOP = "top";
    private static final String SIDES = "sides";
    private static final String BOTTOM = "bottom";

    @Override
    public BlockFamily createBlockFamily(BlockBuilderHelper blockBuilder, AssetUri blockDefUri, BlockDefinition blockDefinition, JsonObject blockDefJson) {
        Map<Side, Block> blockMap = Maps.newEnumMap(Side.class);
        BlockDefinition topDef = blockBuilder.getBlockDefinitionForSection(blockDefJson, TOP);
        if (topDef != null) {
            Block block = blockBuilder.constructSimpleBlock(blockDefUri, topDef);
            block.setDirection(Side.TOP);
            blockMap.put(Side.TOP, block);
        }
        BlockDefinition sideDef = blockBuilder.getBlockDefinitionForSection(blockDefJson, SIDES);
        if (sideDef != null) {
            blockMap.putAll(blockBuilder.constructHorizontalRotatedBlocks(blockDefUri, sideDef));
        }
        BlockDefinition bottomDef = blockBuilder.getBlockDefinitionForSection(blockDefJson, BOTTOM);
        if (bottomDef != null) {
            Block block = blockBuilder.constructSimpleBlock(blockDefUri, bottomDef);
            block.setDirection(Side.BOTTOM);
            blockMap.put(Side.BOTTOM, block);
        }
        return new AlignToSurfaceFamily(new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName()), blockMap, blockDefinition.categories);
    }

}
