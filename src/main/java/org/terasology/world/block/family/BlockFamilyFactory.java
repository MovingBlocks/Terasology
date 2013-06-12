package org.terasology.world.block.family;

import com.google.gson.JsonObject;
import org.terasology.asset.AssetUri;
import org.terasology.world.block.loader.BlockDefinition;
import org.terasology.world.block.loader.BlockLoader;

public interface BlockFamilyFactory {
    public BlockFamily createBlockFamily(BlockLoader blockLoader, AssetUri blockDefUri, BlockDefinition blockDefinition, JsonObject blockDefJson);
}
