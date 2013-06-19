package org.terasology.world.block.family;

import com.google.gson.JsonObject;
import org.terasology.asset.AssetUri;
import org.terasology.world.block.loader.BlockDefinition;

import java.util.Map;

public interface BlockFamilyFactory {
    /**
     * Called by the Block Loader to create a block family.
     *
     * @param blockBuilder
     * @param blockDefUri
     * @param mainDefinition
     * @param blockDefJson
     * @return
     */
    public BlockFamily createBlockFamily(BlockBuilderHelper blockBuilder, AssetUri blockDefUri, BlockDefinition mainDefinition, Map<String, BlockDefinition> extraDefinitions, JsonObject blockDefJson);

    /**
     * @return A list of the names of sections that should be merged with the main block definition to produce extra block definitions
     */
    public Iterable<String> supportedExtraBlockDefinitionSections();

}
