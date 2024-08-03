// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.family;

import com.google.common.collect.Sets;
import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.math.Rotation;
import org.terasology.engine.math.Side;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockBuilderHelper;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.block.loader.BlockFamilyDefinition;
import org.terasology.engine.world.block.shapes.BlockShape;
import org.terasology.gestalt.naming.Name;

import java.util.Locale;
import java.util.Set;

/**
 * Multi-Connect family describes a block family that will connect to other neighboring blocks.
 * <p>
 * examples: - Rail Segments - Cables - Fence
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
     */
    public MultiConnectFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        super(definition, blockBuilder);
    }

    /**
     * A condition to return true if the block should have a connection on the given side
     *
     * @param blockLocation The position of the block in question
     * @param connectSide The side to determine connection for
     * @return A boolean indicating if the block should connect on the given side
     */
    protected abstract boolean connectionCondition(Vector3ic blockLocation, Side connectSide);

    /**
     * The sides of the block that can be connected to. Example: In a family like RomanColumn, this method only returns
     * SideBitFlag.getSides(Side.TOP, Side.BOTTOM) because a column should only connect on the top and bottom. Example 2: In the signalling
     * module, this returns all of the possible sides because a cable can connect in any direction.
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
     * @param root The root block URI of the family
     * @param definition The definition of the block family as passed down from the engine
     * @param blockBuilder The block builder to make the blocks in the family
     * @param name The name of the section of the block to be registered, ex: "no_connections"
     * @param sides A byte representing the sides which should be connected for this block
     * @param rotations All of the ways the block should be rotated
     * @return All of the rotations possible for the block with the given sides
     */
    public Set<Block> registerBlock(BlockUri root, BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder, String name,
                                    byte sides, Iterable<Rotation> rotations) {
        Set<Block> result = Sets.newLinkedHashSet();
        for (Rotation rotation : rotations) {
            byte sideBits = 0;
            for (Side side : Side.getSides(sides)) {
                sideBits |= rotation.rotate(side).getFlag();
            }
            Block block = blockBuilder.constructTransformedBlock(definition, name,
                    rotation, new BlockUri(root, new Name(String.valueOf(sideBits))), this);

            blocks.put(sideBits, block);
            result.add(block);
        }
        return result;
    }


    /**
     * @param root The root block URI of the family
     * @param definition The definition of the block family as passed down from the engine
     * @param blockBuilder The block builder to make the blocks in the family
     * @param sides A byte representing the sides which should be connected for this block
     * @param rotations All of the ways the block should be rotated
     * @return All of the rotations possible for the block with the given sides
     */
    public Set<Block> registerBlock(BlockUri root, BlockFamilyDefinition definition, final BlockBuilderHelper blockBuilder,
                                    byte sides, Iterable<Rotation> rotations) {
        Set<Block> result = Sets.newLinkedHashSet();
        for (Rotation rotation : rotations) {
            byte sideBits = 0;
            for (Side side : Side.getSides(sides)) {
                sideBits |= rotation.rotate(side).getFlag();
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
        for (Side connectSide : Side.getSides(getConnectionSides())) {
            if (this.connectionCondition(data.blockPosition, connectSide)) {
                connections |= connectSide.getFlag();
            }
        }
        return blocks.get(connections);
    }

    @Override
    public Block getBlockForNeighborUpdate(Vector3ic location, Block oldBlock) {
        byte connections = 0;
        for (Side connectSide : Side.getSides(getConnectionSides())) {
            if (this.connectionCondition(location, connectSide)) {
                connections |= connectSide.getFlag();
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
