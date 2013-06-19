package org.terasology.world.block.family;

import com.google.gson.JsonObject;
import org.terasology.asset.AssetUri;
import org.terasology.world.block.loader.BlockDefinition;

public interface BlockFamilyFactory {
    /**
     * Called by the Block Loader to create a block family.
     *
     *
     * @param blockBuilder
     * @param blockDefUri
     * @param blockDefinition
     * @param blockDefJson
     * @return
     */
    public BlockFamily createBlockFamily(BlockBuilderHelper blockBuilder, AssetUri blockDefUri, BlockDefinition blockDefinition, JsonObject blockDefJson);

}
