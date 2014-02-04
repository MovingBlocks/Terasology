/*
 * Copyright 2013 MovingBlocks
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class UpdatesWithNeighboursFamilyFactory implements BlockFamilyFactory {
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

    private static final Map<String, Byte> DEFAULT_SHAPE_MAPPING =
            new HashMap<String, Byte>() {
                {
                    put(NO_CONNECTIONS, (byte) 0);
                    put(ONE_CONNECTION, SideBitFlag.getSides(Side.BACK));

                    put(TWO_CONNECTIONS_LINE, SideBitFlag.getSides(Side.BACK, Side.FRONT));
                    put(TWO_CONNECTIONS_CORNER, SideBitFlag.getSides(Side.LEFT, Side.BACK));

                    put(THREE_CONNECTIONS_CORNER, SideBitFlag.getSides(Side.LEFT, Side.BACK, Side.TOP));
                    put(THREE_CONNECTIONS_T, SideBitFlag.getSides(Side.LEFT, Side.BACK, Side.FRONT));

                    put(FOUR_CONNECTIONS_CROSS, SideBitFlag.getSides(Side.RIGHT, Side.LEFT, Side.BACK, Side.FRONT));
                    put(FOUR_CONNECTIONS_SIDE, SideBitFlag.getSides(Side.LEFT, Side.BACK, Side.FRONT, Side.TOP));

                    put(FIVE_CONNECTIONS, SideBitFlag.getSides(Side.LEFT, Side.BACK, Side.FRONT, Side.TOP, Side.BOTTOM));
                    put(SIX_CONNECTIONS, (byte) 63);
                }
            };

    private ConnectionCondition connectionCondition;
    private byte connectionSides;
    private boolean horizontalOnly;
    private Map<String, Byte> shapeMapping;


    protected UpdatesWithNeighboursFamilyFactory(ConnectionCondition connectionCondition, byte connectionSides) {
        this(connectionCondition, connectionSides, DEFAULT_SHAPE_MAPPING, false);
    }

    protected UpdatesWithNeighboursFamilyFactory(ConnectionCondition connectionCondition, byte connectionSides,
                                                 Map<String, Byte> shapeMapping, boolean horizontalOnly) {
        this.connectionCondition = connectionCondition;
        this.connectionSides = connectionSides;
        this.shapeMapping = shapeMapping;
        this.horizontalOnly = horizontalOnly;
    }

    @Override
    public BlockFamily createBlockFamily(BlockBuilderHelper blockBuilder, AssetUri blockDefUri, BlockDefinition blockDefinition, JsonObject blockDefJson) {
        TByteObjectMap<BlockDefinition>[] basicBlocks = new TByteObjectMap[7];
        TByteObjectMap<Block> blocksForConnections = new TByteObjectHashMap<>();

        basicBlocks[0] = new TByteObjectHashMap<>();
        putBlockDefinition(basicBlocks[0], blockBuilder, blockDefJson, NO_CONNECTIONS);

        basicBlocks[1] = new TByteObjectHashMap<>();
        putBlockDefinition(basicBlocks[1], blockBuilder, blockDefJson, ONE_CONNECTION);

        basicBlocks[2] = new TByteObjectHashMap<>();
        putBlockDefinition(basicBlocks[2], blockBuilder, blockDefJson, TWO_CONNECTIONS_LINE);
        putBlockDefinition(basicBlocks[2], blockBuilder, blockDefJson, TWO_CONNECTIONS_CORNER);

        basicBlocks[3] = new TByteObjectHashMap<>();
        putBlockDefinition(basicBlocks[3], blockBuilder, blockDefJson, THREE_CONNECTIONS_CORNER);
        putBlockDefinition(basicBlocks[3], blockBuilder, blockDefJson, THREE_CONNECTIONS_T);

        basicBlocks[4] = new TByteObjectHashMap<>();
        putBlockDefinition(basicBlocks[4], blockBuilder, blockDefJson, FOUR_CONNECTIONS_CROSS);
        putBlockDefinition(basicBlocks[4], blockBuilder, blockDefJson, FOUR_CONNECTIONS_SIDE);

        basicBlocks[5] = new TByteObjectHashMap<>();
        putBlockDefinition(basicBlocks[5], blockBuilder, blockDefJson, FIVE_CONNECTIONS);

        basicBlocks[6] = new TByteObjectHashMap<>();
        putBlockDefinition(basicBlocks[6], blockBuilder, blockDefJson, SIX_CONNECTIONS);

        BlockUri blockUri = new BlockUri(blockDefUri.getModuleName(), blockDefUri.getAssetName());

        // Now make sure we have all combinations based on the basic set (above) and rotations
        for (byte connections = 0; connections < 64; connections++) {
            // Only the allowed connections should be created
            if ((connections & connectionSides) == connections) {
                Block block = constructBlockForConnections(connections, blockBuilder, blockDefUri, basicBlocks);
                if (block == null) {
                    throw new IllegalStateException("Unable to find correct block definition for connections: " + connections);
                }
                block.setUri(new BlockUri(blockUri, String.valueOf(connections)));
                blocksForConnections.put(connections, block);
            }
        }

        final Block archetypeBlock = blocksForConnections.get(SideBitFlag.getSides(Side.RIGHT, Side.LEFT));
        return new UpdatesWithNeighboursFamily(connectionCondition, blockUri, blockDefinition.categories,
                archetypeBlock, blocksForConnections, connectionSides);
    }

    private void putBlockDefinition(TByteObjectMap<BlockDefinition> blockDefinitions, BlockBuilderHelper blockBuilder, JsonObject blockDefJson,
                                    String connections) {
        Byte value = shapeMapping.get(connections);
        if (value != null) {
            blockDefinitions.put(value, getBlockDefinition(connections, blockBuilder, blockDefJson));
        }
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

        Iterable<Rotation> rotations = horizontalOnly ? Rotation.horizontalRotations() : Rotation.values();
        for (Rotation rot : rotations) {
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
