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

@RegisterBlockFamilyFactory(id="AlignToSurface")
public class AlignToSurfaceFamilyFactory implements BlockFamilyFactory {
    @Override
    public BlockFamily createBlockFamily(BlockLoader blockLoader, AssetUri blockDefUri, BlockDefinition blockDefinition, JsonObject blockDefJson) {
        Map<Side, Block> blockMap = Maps.newEnumMap(Side.class);
        String[] categories = new String[0];
        if (blockDefJson.has("top")) {
            JsonObject topDefJson = blockDefJson.getAsJsonObject("top");
            blockDefJson.remove("top");
            blockLoader.mergeJsonInto(blockDefJson, topDefJson);
            BlockDefinition topDef = blockLoader.loadBlockDefinition(topDefJson);
            Block block = blockLoader.constructSingleBlock(blockDefUri, topDef);
            block.setDirection(Side.TOP);
            blockMap.put(Side.TOP, block);
            categories = blockLoader.getCategories(topDef);
        }
        if (blockDefJson.has("sides")) {
            JsonObject sideDefJson = blockDefJson.getAsJsonObject("sides");
            blockDefJson.remove("sides");
            blockLoader.mergeJsonInto(blockDefJson, sideDefJson);
            BlockDefinition sideDef = blockLoader.loadBlockDefinition(sideDefJson);
            blockLoader.constructHorizontalBlocks(blockDefUri, sideDef, blockMap);
            categories = blockLoader.getCategories(sideDef);
        }
        if (blockDefJson.has("bottom")) {
            JsonObject bottomDefJson = blockDefJson.getAsJsonObject("bottom");
            blockDefJson.remove("bottom");
            blockLoader.mergeJsonInto(blockDefJson, bottomDefJson);
            BlockDefinition bottomDef = blockLoader.loadBlockDefinition(bottomDefJson);
            Block block = blockLoader.constructSingleBlock(blockDefUri, bottomDef);
            block.setDirection(Side.BOTTOM);
            blockMap.put(Side.BOTTOM, block);
            categories = blockLoader.getCategories(bottomDef);
        }
        return new AlignToSurfaceFamily(new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName()), blockMap, categories);
    }
}
