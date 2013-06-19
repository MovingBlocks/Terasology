package org.terasology.world.block.family;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.terasology.asset.AssetUri;
import org.terasology.math.Side;
import org.terasology.utilities.gson.JsonMergeUtil;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockDefinition;

import java.util.Map;

@RegisterBlockFamilyFactory("alignToSurface")
public class AlignToSurfaceFamilyFactory implements BlockFamilyFactory {
    @Override
    public BlockFamily createBlockFamily(BlockBuilderHelper blockBuilder, AssetUri blockDefUri, BlockDefinition blockDef, JsonObject blockDefJson) {
        Map<Side, Block> blockMap = Maps.newEnumMap(Side.class);
        if (blockDefJson.has("top")) {
            JsonObject topDefJson = blockDefJson.getAsJsonObject("top");
            blockDefJson.remove("top");
            JsonMergeUtil.mergeOnto(blockDefJson, topDefJson);
            BlockDefinition topDef = blockBuilder.createBlockDefinition(topDefJson);
            Block block = blockBuilder.constructSimpleBlock(blockDefUri, topDef);
            block.setDirection(Side.TOP);
            blockMap.put(Side.TOP, block);
        }
        if (blockDefJson.has("sides")) {
            JsonObject sideDefJson = blockDefJson.getAsJsonObject("sides");
            blockDefJson.remove("sides");
            JsonMergeUtil.mergeOnto(blockDefJson, sideDefJson);
            BlockDefinition sideDef = blockBuilder.createBlockDefinition(sideDefJson);
            blockMap.putAll(blockBuilder.constructHorizontalRotatedBlocks(blockDefUri, sideDef));
        }
        if (blockDefJson.has("bottom")) {
            JsonObject bottomDefJson = blockDefJson.getAsJsonObject("bottom");
            blockDefJson.remove("bottom");
            JsonMergeUtil.mergeOnto(blockDefJson, bottomDefJson);
            BlockDefinition bottomDef = blockBuilder.createBlockDefinition(bottomDefJson);
            Block block = blockBuilder.constructSimpleBlock(blockDefUri, bottomDef);
            block.setDirection(Side.BOTTOM);
            blockMap.put(Side.BOTTOM, block);
        }
        return new AlignToSurfaceFamily(new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName()), blockMap, blockDef.categories);
    }

}
