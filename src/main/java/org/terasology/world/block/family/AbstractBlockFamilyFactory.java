package org.terasology.world.block.family;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import org.terasology.asset.AssetUri;
import org.terasology.entitySystem.common.NullIterator;
import org.terasology.world.block.loader.BlockDefinition;

import java.util.Map;

/**
 * @author Immortius
 */
public abstract class AbstractBlockFamilyFactory implements BlockFamilyFactory {

    @Override
    public abstract BlockFamily createBlockFamily(BlockBuilderHelper blockBuilder, AssetUri blockDefUri, BlockDefinition mainDefinition, Map<String, BlockDefinition> extraDefinitions, JsonObject blockDefJson);


    @Override
    public Iterable<String> supportedExtraBlockDefinitionSections() {
        return NullIterator.newInstance();
    }
}
