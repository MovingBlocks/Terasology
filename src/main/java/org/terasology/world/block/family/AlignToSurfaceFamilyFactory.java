package org.terasology.world.block.family;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import org.terasology.asset.AssetUri;
import org.terasology.math.Side;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockDefinition;

import java.util.List;
import java.util.Map;

@RegisterBlockFamilyFactory("alignToSurface")
public class AlignToSurfaceFamilyFactory extends AbstractBlockFamilyFactory {

    private static final String TOP = "top";
    private static final String SIDES = "sides";
    private static final String BOTTOM = "bottom";

    private static final List<String> SECTIONS = ImmutableList.of(TOP, SIDES, BOTTOM);

    @Override
    public BlockFamily createBlockFamily(BlockBuilderHelper blockBuilder, AssetUri blockDefUri, BlockDefinition mainDefinition, Map<String, BlockDefinition> extraDefinitions, JsonObject blockDefJson) {
        Map<Side, Block> blockMap = Maps.newEnumMap(Side.class);
        if (extraDefinitions.containsKey(TOP)) {
            BlockDefinition topDef = extraDefinitions.get(TOP);
            Block block = blockBuilder.constructSimpleBlock(blockDefUri, topDef);
            block.setDirection(Side.TOP);
            blockMap.put(Side.TOP, block);
        }
        if (extraDefinitions.containsKey(SIDES)) {
            BlockDefinition sideDef = extraDefinitions.get(SIDES);
            blockMap.putAll(blockBuilder.constructHorizontalRotatedBlocks(blockDefUri, sideDef));
        }
        if (extraDefinitions.containsKey(BOTTOM)) {
            BlockDefinition bottomDef = extraDefinitions.get(BOTTOM);
            Block block = blockBuilder.constructSimpleBlock(blockDefUri, bottomDef);
            block.setDirection(Side.BOTTOM);
            blockMap.put(Side.BOTTOM, block);
        }
        return new AlignToSurfaceFamily(new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName()), blockMap, mainDefinition.categories);
    }

    @Override
    public Iterable<String> supportedExtraBlockDefinitionSections() {
        return SECTIONS;
    }
}
