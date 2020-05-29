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
     * @param blockBuilder
     * @param shape
     * @param definition
     * @param uri
     */
    private void populateBlockMaps(BlockBuilderHelper blockBuilder, BlockShape shape, BlockFamilyDefinition definition, BlockUri uri) {
        for (Rotation rot : Rotation.horizontalRotations()) {
            Side side = rot.rotate(Side.FRONT);
            Side rotation = Side.TOP;
            blocks.put(ExtendedSide.getExtendedSideFor(side, rotation), transformBlock(blockBuilder, shape, definition, uri, rot, side));
        }
        for (Rotation rot : Rotation.horizontalRotations()) {
            Side side = Side.TOP;
            Side rotation = rot.rotate(Side.FRONT);
            blocks.put(ExtendedSide.getExtendedSideFor(side, rotation), transformBlock(blockBuilder, shape, definition, uri, rot, side));
        }
        for (Rotation rot : Rotation.horizontalRotations()) {
            Side side = Side.BOTTOM;
            Side rotation = rot.rotate(Side.FRONT);
            blocks.put(ExtendedSide.getExtendedSideFor(side, rotation), transformBlock(blockBuilder, shape, definition, uri, rot, side));
        }
    }

    /**
     * @param blockBuilder
     * @param shape
     * @param definition
     * @param uri
     * @param rot
     * @param side
     */
    private Block transformBlock(BlockBuilderHelper blockBuilder, BlockShape shape, BlockFamilyDefinition definition, BlockUri uri, Rotation rot, Side side) {
        Block block;
        if (shape == null) {
            block = blockBuilder.constructTransformedBlock(definition, side.name(), rot, new BlockUri(uri, new Name(side.name())), this);
        } else {
            block = blockBuilder.constructTransformedBlock(definition, shape, side.name(), rot, new BlockUri(uri, new Name(side.name())), this);
        }
        if (block == null) {
            throw new IllegalArgumentException("Missing block for side: " + side.toString());
        }
        return block;
    }

    @Override
    public Block getBlockForPlacement(Vector3i location, Side surfaceSide, Side secondaryDirection) {
        // Surface side is horizontal -> go based on secondary
        // Surface side is TOP -> rotate to face secondary but be right way up
        // Surface side is BOTTOM -> rotate to face secondary but be upside down
        ExtendedSide side;
        if (surfaceSide == Side.BOTTOM) {
            side = ExtendedSide.getExtendedSideFor(Side.BOTTOM, secondaryDirection);
        } else if (surfaceSide == Side.TOP) {
            side = ExtendedSide.getExtendedSideFor(Side.TOP, secondaryDirection);
        } else {
            side = ExtendedSide.getExtendedSideFor(surfaceSide, Side.TOP);
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


    private enum ExtendedSide {
        FRONT_TOP, FRONT_BOTTOM, FRONT_LEFT, FRONT_RIGHT,
        BACK_TOP, BACK_BOTTOM, BACK_LEFT, BACK_RIGHT,
        LEFT_TOP, LEFT_BOTTOM, LEFT_FRONT, LEFT_BACK,
        RIGHT_TOP, RIGHT_BOTTOM, RIGHT_FRONT, RIGHT_BACK,
        TOP_FRONT, TOP_BACK, TOP_LEFT, TOP_RIGHT,
        BOTTOM_FRONT, BOTTOM_BACK, BOTTOM_LEFT, BOTTOM_RIGHT;

        public static ExtendedSide getExtendedSideFor(Side side, Side rotation) {
            return ExtendedSide.valueOf(side.name() + "_" + rotation.name());
        }
    }
}
