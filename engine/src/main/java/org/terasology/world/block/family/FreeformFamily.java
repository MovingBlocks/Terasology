// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.block.family;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.naming.Name;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

/**
 * A freeform family is a pseudo block family that can be combined with any block shape to produce an actual block family.
 */
@RegisterBlockFamily("freeform")
@FreeFormSupported(true)
public class FreeformFamily extends AbstractBlockFamily implements SideDefinedBlockFamily {
    private static final Logger logger = LoggerFactory.getLogger(FreeformFamily.class);

    private Map<Side, Block> blocks = Maps.newEnumMap(Side.class);
    private Block archetypeBlock;

    public FreeformFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        super(definition, shape, blockBuilder);
        BlockUri uri;
        if (CUBE_SHAPE_URN.equals(shape.getUrn())) {
            uri = new BlockUri(definition.getUrn());
        } else {
            uri = new BlockUri(definition.getUrn(), shape.getUrn());
        }
        if (shape.isCollisionYawSymmetric()) {
            archetypeBlock = blockBuilder.constructSimpleBlock(definition, shape, uri, this);
        } else {
            for (Rotation rot : Rotation.horizontalRotations()) {
                Side side = rot.rotate(Side.FRONT);
                Block block = blockBuilder.constructTransformedBlock(definition, shape, side.toString().toLowerCase(Locale.ENGLISH), rot,
                        new BlockUri(uri, new Name(side.name())), this);
                if (block == null) {
                    throw new IllegalArgumentException("Missing block for side: " + side.toString());
                }
                blocks.put(side, block);
            }
        }

        setBlockUri(uri);
    }

    public FreeformFamily(BlockFamilyDefinition blockFamilyDefinition, BlockBuilderHelper blockBuilderHelper) {
        super(blockFamilyDefinition, blockBuilderHelper);
        throw new UnsupportedOperationException("Shape expected");
    }

    @Override
    public Block getBlockForPlacement(BlockPlacementData data) {
        if (archetypeBlock != null) {
            return archetypeBlock;
        }

        if (data.attachmentSide.isHorizontal()) {
            return blocks.get(data.attachmentSide);
        } else {
            Side blockDirection = Side.inDirection(-data.viewingDirection.x(), 0, -data.viewingDirection.z());
            return blocks.get(blockDirection);
        }
    }

    @Override
    public Block getArchetypeBlock() {
        if (archetypeBlock == null) {
            return blocks.get(this.getArchetypeSide());
        }
        return archetypeBlock;
    }

    protected Side getArchetypeSide() {
        return Side.FRONT;
    }

    @Override
    public Block getBlockFor(BlockUri blockUri) {
        if (archetypeBlock == null && getURI().equals(blockUri.getFamilyUri())) {
            try {
                Side side = Side.valueOf(blockUri.getIdentifier().toString().toUpperCase(Locale.ENGLISH));
                return blocks.get(side);
            } catch (IllegalArgumentException e) {
                logger.error("can't find block with URI: {}", blockUri, e);
                return null;
            }

        }
        return archetypeBlock;
    }

    @Override
    public Iterable<Block> getBlocks() {
        if (archetypeBlock == null) {
            return blocks.values();
        }
        return Arrays.asList(archetypeBlock);
    }

    @Override
    public Block getBlockForSide(Side side) {
        if (archetypeBlock == null) {
            return blocks.get(side);
        }
        return archetypeBlock;
    }

    @Override
    public Side getSide(Block block) {
        if (archetypeBlock == null) {
            for (Map.Entry<Side, Block> sideBlockEntry : blocks.entrySet()) {
                if (block == sideBlockEntry.getValue()) {
                    return sideBlockEntry.getKey();
                }
            }
        }
        return archetypeBlock.getDirection();
    }
}
