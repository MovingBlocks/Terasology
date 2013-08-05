/*
 * Copyright 2013 Moving Blocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.block.family;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import gnu.trove.iterator.TByteObjectIterator;
import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;
import org.terasology.asset.AssetUri;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.SideBitFlag;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockDefinition;

import java.util.Collection;
import java.util.Set;

public abstract class ConnectToSixSidesFamilyFactory implements BlockFamilyFactory {
    public static final String NO_CONNECTIONS = "no_connections";
    public static final String ONE_CONNECTION = "one_connection";
    public static final String TWO_CONNECTIONS_LINE = "line_connection";
    public static final String TWO_CONNECTIONS_CORNER = "2d_corner";
    public static final String THREE_CONNECTIONS_CORNER = "3d_corner";
    public static final String THREE_CONNECTIONS_T = "2d_t";
    public static final String FOUR_CONNECTIONS_CROSS = "cross";
    public static final String FOUR_CONNECTIONS_SIDE = "3d_side";
    public static final String FIVE_CONNECTIONS = "five_connections";
    public static final String SIX_CONNECTIONS = "all";

    private ConnectionCondition connectionCondition;
    private byte connectionSides;

    protected ConnectToSixSidesFamilyFactory(ConnectionCondition connectionCondition, byte connectionSides) {
        this.connectionCondition = connectionCondition;
        this.connectionSides = connectionSides;
    }

    @Override
    public BlockFamily createBlockFamily(BlockBuilderHelper blockBuilder, AssetUri blockDefUri, BlockDefinition blockDefinition, JsonObject blockDefJson) {
        TByteObjectMap<BlockDefinition>[] basicBlocks = new TByteObjectMap[7];
        TByteObjectMap<Block> blocksForConnections = new TByteObjectHashMap<Block>();

        basicBlocks[0] = new TByteObjectHashMap<>();
        basicBlocks[0].put((byte) 0,
                getBlockDefinition(NO_CONNECTIONS, blockBuilder, blockDefJson));

        basicBlocks[1] = new TByteObjectHashMap<>();
        basicBlocks[1].put(SideBitFlag.getSides(Side.BACK),
                getBlockDefinition(ONE_CONNECTION, blockBuilder, blockDefJson));

        basicBlocks[2] = new TByteObjectHashMap<>();
        basicBlocks[2].put(SideBitFlag.getSides(Side.BACK, Side.FRONT),
                getBlockDefinition(TWO_CONNECTIONS_LINE, blockBuilder, blockDefJson));
        basicBlocks[2].put(SideBitFlag.getSides(Side.LEFT, Side.BACK),
                getBlockDefinition(TWO_CONNECTIONS_CORNER, blockBuilder, blockDefJson));

        basicBlocks[3] = new TByteObjectHashMap<>();
        basicBlocks[3].put(SideBitFlag.getSides(Side.LEFT, Side.BACK, Side.TOP),
                getBlockDefinition(THREE_CONNECTIONS_CORNER, blockBuilder, blockDefJson));
        basicBlocks[3].put(SideBitFlag.getSides(Side.LEFT, Side.BACK, Side.FRONT),
                getBlockDefinition(THREE_CONNECTIONS_T, blockBuilder, blockDefJson));

        basicBlocks[4] = new TByteObjectHashMap<>();
        basicBlocks[4].put(SideBitFlag.getSides(Side.RIGHT, Side.LEFT, Side.BACK, Side.FRONT),
                getBlockDefinition(FOUR_CONNECTIONS_CROSS, blockBuilder, blockDefJson));
        basicBlocks[4].put(SideBitFlag.getSides(Side.LEFT, Side.BACK, Side.FRONT, Side.TOP),
                getBlockDefinition(FOUR_CONNECTIONS_SIDE, blockBuilder, blockDefJson));

        basicBlocks[5] = new TByteObjectHashMap<>();
        basicBlocks[5].put(SideBitFlag.getSides(Side.LEFT, Side.BACK, Side.FRONT, Side.TOP, Side.BOTTOM),
                getBlockDefinition(FIVE_CONNECTIONS, blockBuilder, blockDefJson));

        basicBlocks[6] = new TByteObjectHashMap<>();
        basicBlocks[6].put((byte) 63,
                getBlockDefinition(SIX_CONNECTIONS, blockBuilder, blockDefJson));

        BlockUri blockUri = new BlockUri(blockDefUri.getModuleName(), blockDefUri.getAssetName());

        // Now make sure we have all combinations based on the basic set (above) and rotations
        for (byte connections = 0; connections < 64; connections++) {
            Block block = constructBlockForConnections(connections, blockBuilder, blockDefUri, basicBlocks);
            if (block == null) {
                throw new IllegalStateException("Unable to find correct block definition for connections: " + connections);
            }
            block.setUri(new BlockUri(blockUri, String.valueOf(connections)));
            blocksForConnections.put(connections, block);
        }

        final Block archetypeBlock = blocksForConnections.get(SideBitFlag.getSides(Side.RIGHT, Side.LEFT));
        return new ConnectToSixSidesFamily(connectionCondition, blockUri, blockDefinition.categories,
                archetypeBlock, blocksForConnections, connectionSides);
    }

    private Block constructBlockForConnections(final byte connections, final BlockBuilderHelper blockBuilder,
                                               final AssetUri blockDefUri, TByteObjectMap<BlockDefinition>[] basicBlocks) {
        int connectionCount = SideBitFlag.getSides(connections).size();
        TByteObjectMap<BlockDefinition> possibleBlockDefinitions = basicBlocks[connectionCount];
        final TByteObjectIterator<BlockDefinition> blockDefinitionIterator = possibleBlockDefinitions.iterator();
        while (blockDefinitionIterator.hasNext()) {
            blockDefinitionIterator.advance();
            final byte originalConnections = blockDefinitionIterator.key();
            final BlockDefinition blockDefinition = blockDefinitionIterator.value();
            Rotation rot = getRotationToAchieve(originalConnections, connections);
            if (rot != null) {
                return blockBuilder.constructTransformedBlock(blockDefUri, blockDefinition, rot);
            }
        }
        return null;
    }

    private Rotation getRotationToAchieve(byte source, byte target) {
        Collection<Side> originalSides = SideBitFlag.getSides(source);

        for (Rotation rot : Rotation.values()) {
            Set<Side> transformedSides = Sets.newHashSet();
            for (Side originalSide : originalSides) {
                transformedSides.add(rot.rotate(originalSide));
            }

            byte transformedSide = SideBitFlag.getSides(transformedSides);
            if (transformedSide == target) {
                return rot;
            }
        }
        return null;
    }

    private BlockDefinition getBlockDefinition(String definition, BlockBuilderHelper blockBuilder, JsonObject blockDefJson) {
        return blockBuilder.getBlockDefinitionForSection(blockDefJson, definition);
    }
}
