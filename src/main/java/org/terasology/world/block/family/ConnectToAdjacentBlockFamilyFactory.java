package org.terasology.world.block.family;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.terasology.asset.AssetUri;
import org.terasology.math.Side;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockAdjacentType;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockDefinition;
import org.terasology.world.block.loader.BlockLoader;

import java.util.EnumMap;
import java.util.Map;

@RegisterBlockFamilyFactory(id="ConnectToAdjacent")
public class ConnectToAdjacentBlockFamilyFactory implements BlockFamilyFactory {
    @Override
    public BlockFamily createBlockFamily(BlockLoader blockLoader, AssetUri blockDefUri, BlockDefinition blockDefinition, JsonObject blockDefJson) {
        Map<BlockAdjacentType, EnumMap<Side, Block> > blockMap = Maps.newEnumMap(BlockAdjacentType.class);
        String[] categories = new String[0];

        if ( blockDefJson.has("types") ){

            JsonArray blockTypes = blockDefJson.getAsJsonArray("types");

            blockDefJson.remove("types");

            for ( JsonElement element : blockTypes.getAsJsonArray() ){
                JsonObject typeDefJson = element.getAsJsonObject();

                if ( !typeDefJson.has("type") ){
                    throw new IllegalArgumentException("Block type is empty");
                }
                BlockAdjacentType type = blockLoader.fromJson(typeDefJson.get("type"), BlockAdjacentType.class);

                if ( type == null ){
                    throw new IllegalArgumentException("Invalid type block: " + blockLoader.fromJson(typeDefJson.get("type"), String.class));
                }

                if ( !blockMap.containsKey(type) ){
                    blockMap.put( type, Maps.<Side, Block>newEnumMap(Side.class));
                }

                typeDefJson.remove("type");
                blockLoader.mergeJsonInto(blockDefJson, typeDefJson);
                BlockDefinition typeDef = blockLoader.loadBlockDefinition(typeDefJson);
                blockLoader.constructHorizontalBlocks( blockDefUri, typeDef, blockMap.get(type) );
            }
        }
        return  new ConnectToAdjacentBlockFamily(new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName()), blockMap,  categories);

    }
}
