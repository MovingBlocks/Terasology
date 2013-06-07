package org.terasology.world.block.family;

import com.google.gson.JsonObject;
import org.terasology.asset.AssetUri;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockDefinition;
import org.terasology.world.block.loader.BlockLoader;

@RegisterBlockFamilyFactory(id="Symmetric")
public class SymmetricFamilyFactory implements BlockFamilyFactory {
    @Override
    public BlockFamily createBlockFamily(BlockLoader blockLoader, AssetUri blockDefUri, BlockDefinition blockDefinition, JsonObject blockDefJson) {
        Block block = blockLoader.constructSingleBlock(blockDefUri, blockDefinition);

        final BlockUri blockUri = new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName());
        final String[] categories = blockLoader.getCategories(blockDefinition);
        return new SymmetricFamily(blockUri, block, categories);

    }
}
