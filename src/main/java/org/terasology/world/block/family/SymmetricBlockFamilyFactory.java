package org.terasology.world.block.family;

import com.google.gson.JsonObject;
import org.terasology.asset.AssetUri;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockDefinition;

@RegisterBlockFamilyFactory("symmetric")
public class SymmetricBlockFamilyFactory implements BlockFamilyFactory {
    @Override
    public BlockFamily createBlockFamily(BlockBuilderHelper blockBuilder, AssetUri blockDefUri, BlockDefinition blockDef, JsonObject blockDefJson) {
        Block block = blockBuilder.constructSimpleBlock(blockDefUri, blockDef);
        return new SymmetricFamily(new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName()), block, blockDef.categories);
    }
}
