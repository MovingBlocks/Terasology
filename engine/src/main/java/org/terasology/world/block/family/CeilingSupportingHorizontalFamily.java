// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.block.family;

import com.google.common.collect.Maps;
import org.terasology.math.Pitch;
import org.terasology.math.Roll;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.Yaw;
import org.terasology.naming.Name;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

import java.util.Map;

/**
 * Rotates a block to either be upright or upside down and always face the player.
 * <p>
 * The block will be placed upside down, if it's either being attached to a BOTTOM side (i.e. the ceiling),
 * or if it's being attached to the upper half of any horizontal side (LEFT, RIGHT, FRONT, BACK).
 */
@RegisterBlockFamily("ceilingSupportingHorizontal")
public class CeilingSupportingHorizontalFamily extends AbstractBlockFamily {
    private final Map<ExtendedSide, Block> blocks = Maps.newEnumMap(ExtendedSide.class);

    public CeilingSupportingHorizontalFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        this(definition, blockBuilder);

        //TODO: maybe remove this later
        BlockUri uri;
        if (CUBE_SHAPE_URN.equals(shape.getUrn())) {
            uri = new BlockUri(definition.getUrn());
        } else {
            uri = new BlockUri(definition.getUrn(), shape.getUrn());
        }
        populateBlockMaps(blockBuilder, shape, definition, uri);
        setBlockUri(uri);
    }

    public CeilingSupportingHorizontalFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        super(definition, blockBuilder);

        BlockUri uri = new BlockUri(definition.getUrn());
        populateBlockMaps(blockBuilder, null, definition, uri);
    }

    /**
     * Populates the map with all 8 rotations of the block that are possible.
     * <p>
     * These are all four 90 degree rotations about the Y-axis (YAW) for each case where the TOP side is
     * - facing upwards
     * - facing downwards
     *
     * @param blockBuilder The block builder to use to produce blocks
     * @param shape        The shape the block should be made in
     * @param definition   The definition for the family
     * @param uri          The base URI for the block
     */
    private void populateBlockMaps(BlockBuilderHelper blockBuilder, BlockShape shape, BlockFamilyDefinition definition, BlockUri uri) {
        for (Rotation rotation : Rotation.horizontalRotations()) {
            Side horizontalSide = rotation.rotate(Side.FRONT);
            ExtendedSide extendedSideTop = ExtendedSide.getExtendedSideFor(Side.TOP, horizontalSide);
            blocks.put(extendedSideTop, transformBlock(
                    blockBuilder, shape, definition, new BlockUri(uri, new Name(extendedSideTop.name())),
                    rotation, extendedSideTop
            ));

            ExtendedSide extendedSideBottom = ExtendedSide.getExtendedSideFor(Side.BOTTOM, horizontalSide);
            Yaw yaw = Rotation.horizontalRotations().get((rotation.getYaw().getIndex() + 2) % 4).getYaw();
            blocks.put(extendedSideBottom, transformBlock(
                    blockBuilder, shape, definition, new BlockUri(uri, new Name(extendedSideBottom.name())),
                    Rotation.rotate(yaw, Pitch.CLOCKWISE_180, Roll.NONE), extendedSideBottom
            ));
        }
    }

    /**
     * Build and rotate the block according to the specified rotation.
     *
     * @param blockBuilder The builder instance to use
     * @param shape        The shape specified, or null if none was
     * @param definition   The definition instance of the family to pass to the builder
     * @param uri          The URI of the block being build
     * @param rotation     The rotation to apply to the block
     * @param side         The subsection of the block that this is.
     */
    private Block transformBlock(
            BlockBuilderHelper blockBuilder, BlockShape shape, BlockFamilyDefinition definition,
            BlockUri uri, Rotation rotation, ExtendedSide side
    ) {
        Block block;
        if (shape == null) {
            block = blockBuilder.constructTransformedBlock(definition, side.name(), rotation, uri, this);
        } else {
            block = blockBuilder.constructTransformedBlock(definition, shape, side.name(), rotation, uri, this);
        }

        if (block != null) {
            return block;
        } else {
            throw new IllegalArgumentException("Missing block for side: " + side.toString());
        }
    }

    @Override
    public Block getBlockForPlacement(BlockPlacementData data) {
        boolean upsideDownPlacement = data.attachmentSide == Side.BOTTOM
                || data.attachmentSide != Side.TOP && data.relativeAttachmentPosition.y() > 0.5;
        final Side mainSide = upsideDownPlacement ? Side.BOTTOM : Side.TOP;

        Side blockDirection = Side.inDirection(-data.viewingDirection.x(), 0, -data.viewingDirection.z());
        return blocks.get(ExtendedSide.getExtendedSideFor(mainSide, blockDirection));
    }

    @Override
    public Block getArchetypeBlock() {
        return blocks.get(ExtendedSide.getExtendedSideFor(Side.TOP, Side.FRONT));
    }

    @Override
    public Block getBlockFor(BlockUri blockUri) {
        if (getURI().equals(blockUri.getFamilyUri())) {
            try {
                ExtendedSide side = ExtendedSide.valueOf(blockUri.getIdentifier().toString());
                return blocks.get(side);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Iterable<Block> getBlocks() {
        return blocks.values();
    }

    /**
     * This enum encapsulates all rotations for upright and upside down positioning.
     * The primary is either TOP or BOTTOM and represents upright, or upside down positioning respectively.
     * The rotation represent the horizontal direction the block is facing.
     */
    private enum ExtendedSide {
        TOP_FRONT, TOP_BACK, TOP_LEFT, TOP_RIGHT,
        BOTTOM_FRONT, BOTTOM_BACK, BOTTOM_LEFT, BOTTOM_RIGHT;

        /**
         * Given a primary side and a rotation, get the ExtendedSide encapsulating these.
         *
         * @param side     The primary side
         * @param rotation The rotation of that side
         * @return The ExtendedSide that represents this
         */
        public static ExtendedSide getExtendedSideFor(Side side, Side rotation) {
            return ExtendedSide.valueOf(side.name() + "_" + rotation.name());
        }
    }
}
