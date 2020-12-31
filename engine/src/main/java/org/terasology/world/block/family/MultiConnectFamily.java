/*
 * Copyright 2018 MovingBlocks
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
import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;
import org.joml.Vector3f;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.JomlUtil;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.SideBitFlag;
import org.terasology.math.geom.Vector3i;
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

import java.util.Locale;
import java.util.Set;

/**
 * Multi-Connect family describes a block family that will connect to other neighboring blocks.
 *
 * examples:
 * - Rail Segments
 * - Cables
 * - Fence
 */
public abstract class MultiConnectFamily extends AbstractBlockFamily implements UpdatesWithNeighboursFamily {
    private static final Logger logger = LoggerFactory.getLogger(MultiConnectFamily.class);

    @In
    protected WorldProvider worldProvider;

    @In
    protected BlockEntityRegistry blockEntityRegistry;

    protected TByteObjectMap<Block> blocks = new TByteObjectHashMap<>();

    /**
     * Constructor for a block with a specified shape
     *
     * @param definition Family definition
     * @param shape The shape of the block
     * @param blockBuilder The builder to make the blocks for the family
     */
    public MultiConnectFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        super(definition, shape, blockBuilder);
    }

    /**
     * Constructor for a regular block
     *
     * @param definition Family definition
     * @param blockBuilder The builder to make the blocks for the family
     */
    public MultiConnectFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        super(definition, blockBuilder);
    }

    /**
     * A condition to return true if the block should have a connection on the given side
     *
     * @param blockLocation The position of the block in question
     * @param connectSide The side to determine connection for
     *
     * @return A boolean indicating if the block should connect on the given side
     * @deprecated This method is scheduled for removal in an upcoming version.
     *             Use the JOML implementation instead: {@link #connectionCondition(Vector3ic, Side)}.
     */
    @Deprecated
    protected abstract boolean connectionCondition(Vector3i blockLocation, Side connectSide);

    /**
     * A condition to return true if the block should have a connection on the given side
     *
     * @param blockLocation The position of the block in question
     * @param connectSide The side to determine connection for
     *
     * @return A boolean indicating if the block should connect on the given side
     */
    protected abstract boolean connectionCondition(Vector3ic blockLocation, Side connectSide);


    /**
     * The sides of the block that can be connected to.
     * Example: In a family like RomanColumn, this method only returns SideBitFlag.getSides(Side.TOP, Side.BOTTOM)
     * because a column should only connect on the top and bottom.
     * Example 2: In the signalling module, this returns all of the possible sides because a cable can connect in any direction.
     *
     * @return The sides of the block that can be connected to
     */
    public abstract byte getConnectionSides();

    /**
     * @return Which block should be shown in the player's inventory. The "default" block.
     */
    @Override
    public abstract Block getArchetypeBlock();

    /**
     *
     * @param root The root block URI of the family
     * @param definition The definition of the block family as passed down from the engine
     * @param blockBuilder The block builder to make the blocks in the family
     * @param name The name of the section of the block to be registered, ex: "no_connections"
     * @param sides A byte representing the sides which should be connected for this block
     * @param rotations All of the ways the block should be rotated
     * @return All of the rotations possible for the block with the given sides
     */
    public Set<Block> registerBlock(BlockUri root, BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder, String name, byte sides, Iterable<Rotation> rotations) {
        Set<Block> result = Sets.newLinkedHashSet();
        for (Rotation rotation: rotations) {
            byte sideBits = 0;
            for (Side side : SideBitFlag.getSides(sides)) {
                sideBits += SideBitFlag.getSide(rotation.rotate(side));
            }
            Block block = blockBuilder.constructTransformedBlock(definition, name, rotation, new BlockUri(root, new Name(String.valueOf(sideBits))), this);

            blocks.put(sideBits, block);
            result.add(block);
        }
        return result;
    }


    /**
     *
     * @param root The root block URI of the family
     * @param definition The definition of the block family as passed down from the engine
     * @param blockBuilder The block builder to make the blocks in the family
     * @param sides A byte representing the sides which should be connected for this block
     * @param rotations All of the ways the block should be rotated
     * @return All of the rotations possible for the block with the given sides
     */
    public Set<Block> registerBlock(BlockUri root, BlockFamilyDefinition definition, final BlockBuilderHelper blockBuilder, byte sides, Iterable<Rotation> rotations) {
        Set<Block> result = Sets.newLinkedHashSet();
        for (Rotation rotation: rotations) {
            byte sideBits = 0;
            for (Side side : SideBitFlag.getSides(sides)) {
                sideBits += SideBitFlag.getSide(rotation.rotate(side));
            }
            BlockUri uri = new BlockUri(root, new Name(String.valueOf(sideBits)));
            Block block = blockBuilder.constructTransformedBlock(definition, rotation, uri, this);
            block.setUri(uri);

            blocks.put(sideBits, block);
            result.add(block);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Block getBlockForPlacement(BlockPlacementData data) {
        byte connections = 0;
        for (Side connectSide : SideBitFlag.getSides(getConnectionSides())) {
            if (this.connectionCondition(JomlUtil.from(data.blockPosition), connectSide)) {
                connections += SideBitFlag.getSide(connectSide);
            }
        }
        return blocks.get(connections);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Block getBlockForPlacement(Vector3i location, Side attachmentSide, Side direction) {
        BlockPlacementData data = new BlockPlacementData(JomlUtil.from(location), attachmentSide, new Vector3f());
        return getBlockForPlacement(data);
    }

    /**
     * Update the block then a neighbor changes
     *
     * @param location The location of the block
     * @param oldBlock What the block was before the neighbor updated
     *
     * @return The block from the family to be placed
     */
    @Override
    public Block getBlockForNeighborUpdate(Vector3i location, Block oldBlock) {
        byte connections = 0;
        for (Side connectSide : SideBitFlag.getSides(getConnectionSides())) {
            if (this.connectionCondition(location, connectSide)) {
                connections += SideBitFlag.getSide(connectSide);
            }
        }
        return blocks.get(connections);
    }

    @Override
    public Block getBlockForNeighborUpdate(Vector3ic location, Block oldBlock) {
        byte connections = 0;
        for (Side connectSide : SideBitFlag.getSides(getConnectionSides())) {
            if (this.connectionCondition(location, connectSide)) {
                connections += SideBitFlag.getSide(connectSide);
            }
        }
        return blocks.get(connections);
    }

    /**
     * @return A block from the family for a given URI
     */
    @Override
    public Block getBlockFor(BlockUri blockUri) {
        if (getURI().equals(blockUri.getFamilyUri())) {
            try {
                byte connections = Byte.parseByte(blockUri.getIdentifier().toString().toLowerCase(Locale.ENGLISH));
                return blocks.get(connections);
            } catch (IllegalArgumentException e) {
                logger.error("can't find block with URI: {}", blockUri, e);
                return null;
            }
        }
        return null;
    }

    public byte getConnections(BlockUri blockUri) {
        if (getURI().equals(blockUri.getFamilyUri())) {
            try {
                return Byte.parseByte(blockUri.getIdentifier().toString().toLowerCase(Locale.ENGLISH));
            } catch (NumberFormatException e) {
                logger.error("can't find block with URI: {}", blockUri, e);
            }
        }
        return 0;
    }

    /**
     * @return An iterable of the registered blocks
     */
    @Override
    public Iterable<Block> getBlocks() {
        return blocks.valueCollection();
    }
}
