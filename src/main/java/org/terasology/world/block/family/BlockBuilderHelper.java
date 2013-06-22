package org.terasology.world.block.family;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.terasology.asset.AssetUri;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.world.block.Block;
import org.terasology.world.block.loader.BlockDefinition;

import javax.vecmath.Matrix3d;
import java.util.Map;

public interface BlockBuilderHelper {

    public Block constructSimpleBlock(AssetUri blockDefUri, BlockDefinition blockDefinition);

    public Map<Side, Block> constructHorizontalRotatedBlocks(AssetUri blockDefUri, BlockDefinition blockDefinition);

    public Block constructTransformedBlock(AssetUri blockDefUri, BlockDefinition blockDefinition, Rotation rotation);

    public BlockDefinition getBlockDefinitionForSection(JsonObject json, String sectionName);

}
