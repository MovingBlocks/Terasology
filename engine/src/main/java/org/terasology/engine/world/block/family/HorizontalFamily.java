// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.family;

import com.google.common.collect.Maps;
import org.terasology.engine.math.Rotation;
import org.terasology.engine.math.Side;
import org.terasology.engine.world.block.loader.BlockFamilyDefinition;
import org.terasology.engine.world.block.shapes.BlockShape;
import org.terasology.gestalt.naming.Name;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockBuilderHelper;
import org.terasology.engine.world.block.BlockUri;

import java.util.Locale;
import java.util.Map;

/**
 * Block group for blocks that can be oriented around the vertical axis.
 */
@RegisterBlockFamily("horizontal")
@BlockSections({"front", "left", "right", "back", "top", "bottom"})
@MultiSections({
        @MultiSection(name = "all", coversSection = "front", appliesToSections = {"front", "left", "right", "back", "top", "bottom"}),
        @MultiSection(name = "topBottom", coversSection = "top", appliesToSections = {"top", "bottom"}),
        @MultiSection(name = "sides", coversSection = "front", appliesToSections = {"front", "left", "right", "back"})})
public class HorizontalFamily extends AbstractBlockFamily implements SideDefinedBlockFamily {
    private Map<Side, Block> blocks = Maps.newEnumMap(Side.class);

    public HorizontalFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        super(definition, shape, blockBuilder);
        BlockUri uri;
        if (CUBE_SHAPE_URN.equals(shape.getUrn())) {
            uri = new BlockUri(definition.getUrn());
        } else {
            uri = new BlockUri(definition.getUrn(), shape.getUrn());
        }
        for (Rotation rot : Rotation.horizontalRotations()) {
            Side side = rot.rotate(Side.FRONT);
            Block block = blockBuilder.constructTransformedBlock(definition, shape, side.toString().toLowerCase(Locale.ENGLISH), rot,
                    new BlockUri(uri, new Name(side.name())), this);
            if (block == null) {
                throw new IllegalArgumentException("Missing block for side: " + side.toString());
            }
            blocks.put(side, block);
        }
        setBlockUri(uri);
    }

    public HorizontalFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        super(definition, blockBuilder);
        BlockUri uri = new BlockUri(definition.getUrn());
        for (Rotation rot : Rotation.horizontalRotations()) {
            Side side = rot.rotate(Side.FRONT);

            Block block = blockBuilder.constructTransformedBlock(definition, side.toString().toLowerCase(Locale.ENGLISH), rot, new BlockUri(uri, new Name(side.name())), this);
            if (block == null) {
                throw new IllegalArgumentException("Missing block for side: " + side.toString());
            }
            blocks.put(side, block);
        }
    }

    protected Side getArchetypeSide() {
        return Side.FRONT;
    }

    @Override
    public Block getBlockForPlacement(BlockPlacementData data) {
        if (data.attachmentSide.isHorizontal()) {
            return blocks.get(data.attachmentSide);
        } else {
            Side secondaryDirection = Side.inDirection(-data.viewingDirection.x(), 0, -data.viewingDirection.z());
            return blocks.get(secondaryDirection);
        }
    }

    @Override
    public Block getArchetypeBlock() {
        return blocks.get(this.getArchetypeSide());
    }

    @Override
    public Block getBlockFor(BlockUri blockUri) {
        if (getURI().equals(blockUri.getFamilyUri())) {
            try {
                Side side = Side.valueOf(blockUri.getIdentifier().toString().toUpperCase(Locale.ENGLISH));
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

    @Override
    public Block getBlockForSide(Side side) {
        return blocks.get(side);
    }

    @Override
    public Side getSide(Block block) {
        for (Map.Entry<Side, Block> sideBlockEntry : blocks.entrySet()) {
            if (block == sideBlockEntry.getValue()) {
                return sideBlockEntry.getKey();
            }
        }
        return null;
    }
}
