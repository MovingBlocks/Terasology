package org.terasology.world.block.family;

import com.google.gson.JsonObject;
import org.terasology.asset.AssetUri;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockDefinition;

import java.util.Map;

@RegisterBlockFamilyFactory("symmetric")
public class SymmetricBlockFamilyFactory extends AbstractBlockFamilyFactory {

    @Override
    public BlockFamily createBlockFamily(BlockBuilderHelper blockBuilder, AssetUri blockDefUri, BlockDefinition mainDefinition, Map<String, BlockDefinition> extraDefinitions, JsonObject blockDefJson) {
        Block block = blockBuilder.constructSimpleBlock(blockDefUri, mainDefinition);
        return new SymmetricFamily(new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName()), block, mainDefinition.categories);
    }
}
