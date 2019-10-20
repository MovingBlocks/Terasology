/*
 * Copyright 2019 MovingBlocks
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

import com.google.common.collect.Maps;
import org.terasology.math.Pitch;
import org.terasology.math.Roll;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.naming.Name;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

import java.util.Map;

/**
 * Rotates a block to always face the player, with a few specific rules.
 * <p>
 * If the block is placed onto a TOP  side (eg, the ground), then the FRONT side of the block will face upwards.
 * If the block is placed onto a BOTTOM side (eg, the underside of a roof), then the FRONT side of the block will face downwards
 * <p>
 * Regardless of the side the block is placed onto, it shall rotate about it's Y axis (ie YAW) to orient it's FRONT side to face the player.
 */
@RegisterBlockFamily("fullRotation")
public class FullRotationFamily extends AbstractBlockFamily {
    private Map<ExtendedSide, Block> blocks = Maps.newEnumMap(ExtendedSide.class);

    public FullRotationFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
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

    public FullRotationFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        super(definition, blockBuilder);

        BlockUri uri = new BlockUri(definition.getUrn());
        populateBlockMaps(blockBuilder, null, definition, uri);

    }

    /**
     * Populates the map with all 12 rotations of the block that are possible.
     * <p>
     * These are all four 90 degree rotations about the Y-axis (YAW) for each case where the FRONT side is
     * - facing upwards
     * - facing horizontal
     * - facing downwards
     *
     * @param blockBuilder The block builder to use to produce blocks
     * @param shape        The shape the block should be made in
     * @param definition   The definition for the family
     * @param uri          The base URI for the block
     */
    private void populateBlockMaps(BlockBuilderHelper blockBuilder, BlockShape shape, BlockFamilyDefinition definition, BlockUri uri) {
        for (Rotation rot : Rotation.horizontalRotations()) {
            Side primarySide = rot.rotate(Side.FRONT);
            Side sideRotation = Side.TOP;
            ExtendedSide extendedSide = ExtendedSide.getExtendedSideFor(primarySide, sideRotation);

            blocks.put(extendedSide,
                    transformBlock(blockBuilder,
                            shape,
                            definition,
                            new BlockUri(uri, new Name(extendedSide.name())),
                            rot,
                            extendedSide));
        }
        for (Rotation rot : Rotation.horizontalRotations()) {
            Side primarySide = Side.TOP;
            Side sideRotation = rot.rotate(Side.FRONT);
            ExtendedSide extendedSide = ExtendedSide.getExtendedSideFor(primarySide, sideRotation);

            blocks.put(extendedSide,
                    transformBlock(blockBuilder,
                            shape,
                            definition,
                            new BlockUri(uri, new Name(extendedSide.name())),
                            Rotation.rotate(rot.getYaw(), Pitch.CLOCKWISE_90, Roll.NONE),
                            extendedSide));
        }
        for (Rotation rot : Rotation.horizontalRotations()) {
            Side primarySide = Side.BOTTOM;
            Side sideRotation = rot.rotate(Side.FRONT);
            ExtendedSide extendedSide = ExtendedSide.getExtendedSideFor(primarySide, sideRotation);

            blocks.put(extendedSide,
                    transformBlock(blockBuilder,
                            shape,
                            definition,
                            new BlockUri(uri, new Name(extendedSide.name())),
                            Rotation.rotate(rot.getYaw(), Pitch.CLOCKWISE_270, Roll.NONE),
                            extendedSide));
        }
    }

    /**
     * Build and rotate the block according to the specified rotation.
     *
     * @param blockBuilder The builder instance to use
     * @param shape        The shape specified, or null if none was
     * @param definition   The definition instance of the family to pass to the builder
     * @param uri          The URI of the block being build
     * @param rot          The rotation to apply to the block
     * @param side         The subsection of the block that this is.
     */
    private Block transformBlock(BlockBuilderHelper blockBuilder, BlockShape shape, BlockFamilyDefinition definition, BlockUri uri, Rotation rot, ExtendedSide side) {
        Block block;
        if (shape == null) {
            block = blockBuilder.constructTransformedBlock(definition, side.name(), rot, uri, this);
        } else {
            block = blockBuilder.constructTransformedBlock(definition, shape, side.name(), rot, uri, this);
        }
        if (block == null) {
            throw new IllegalArgumentException("Missing block for side: " + side.toString());
        }
        return block;
    }

    @Override
    public Block getBlockForPlacement(Vector3i location, Side surfaceSide, Side secondaryDirection) {
        // Surface side is TOP -> rotate to face secondary but point FRONT upwards
        // Surface side is BOTTOM -> rotate to face secondary but point FRONT downwards
        // Surface side is horizontal -> choose between secondary or surfaceSide to face the player
        ExtendedSide side;
        if (surfaceSide == Side.BOTTOM) {
            side = ExtendedSide.getExtendedSideFor(Side.BOTTOM, secondaryDirection);
        } else if (surfaceSide == Side.TOP) {
            side = ExtendedSide.getExtendedSideFor(Side.TOP, secondaryDirection);
        } else {
            // secondaryDirection and surfaceSide can never be the same.
            // Hence, if the secondaryDirection is TOP or BOTTOM, it means that the player is looking in the same direction as the surfaceSide.
            // Additionally, if the surface side is a horizontal side, then the block will never face up or down, so we don't need to worry about if the player is looking vertically
            // Thus we can use this to determine which out of secondaryDirection or surfaceSide best represents which way the player is looking.
            if (secondaryDirection == Side.TOP || secondaryDirection == Side.BOTTOM) {
                // The view direction is the surface side
                side = ExtendedSide.getExtendedSideFor(surfaceSide, Side.TOP);
            } else {
                // The view direction is the secondary
                side = ExtendedSide.getExtendedSideFor(secondaryDirection, Side.TOP);
            }
        }
        return blocks.get(side);
    }

    @Override
    public Block getArchetypeBlock() {
        return blocks.get(ExtendedSide.getExtendedSideFor(Side.FRONT, Side.TOP));
    }

    @Override
    public Block getBlockFor(BlockUri blockUri) {
        if (getURI().equals(blockUri.getFamilyUri())) {
            try {
                ExtendedSide side = ExtendedSide.valueOf(blockUri.getIdentifier().toString());
                return blocks.get(side);
            } catch (IllegalArgumentException e) {
                // "fall through"
            }
        }
        return null;
    }

    @Override
    public Iterable<Block> getBlocks() {
        return blocks.values();
    }


    /**
     * This enum encapsulates both sides, and possible rotations of those sides.
     * The primary side is the same as the standard Side
     * The rotation represent how that side is orientated.
     * For instance FRONT_TOP is the front side, with it's TOP edge aligned with the TOP side. (aka default)
     * FRONT_BOTTOM is a 180 degree rotation of this about the front side. ie the former TOP edge is now aligned with the BOTTOM face
     */
    private enum ExtendedSide {
        FRONT_TOP, FRONT_BOTTOM, FRONT_LEFT, FRONT_RIGHT,
        BACK_TOP, BACK_BOTTOM, BACK_LEFT, BACK_RIGHT,
        LEFT_TOP, LEFT_BOTTOM, LEFT_FRONT, LEFT_BACK,
        RIGHT_TOP, RIGHT_BOTTOM, RIGHT_FRONT, RIGHT_BACK,
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
