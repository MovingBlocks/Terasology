// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.family;

import com.google.common.collect.Maps;
import org.terasology.engine.math.Pitch;
import org.terasology.engine.math.Rotation;
import org.terasology.engine.math.Side;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockBuilderHelper;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.block.loader.BlockFamilyDefinition;
import org.terasology.engine.world.block.shapes.BlockShape;
import org.terasology.gestalt.naming.Name;

import java.util.Locale;
import java.util.Map;

@RegisterBlockFamily("attachedToSurface")
@BlockSections({"front", "left", "right", "back", "top", "bottom"})
@MultiSections({
        @MultiSection(name = "all", coversSection = "front", appliesToSections = {"front", "left", "right", "back", "top", "bottom"}),
        @MultiSection(name = "topBottom", coversSection = "top", appliesToSections = {"top", "bottom"}),
        @MultiSection(name = "sides", coversSection = "front", appliesToSections = {"front", "left", "right", "back"})})
public class AttachedToSurfaceFamily extends AbstractBlockFamily {


    private Map<Side, Block> blocks = Maps.newEnumMap(Side.class);
    private Block archetype;

    public AttachedToSurfaceFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        super(definition, shape, blockBuilder);
        throw new UnsupportedOperationException("Freeform blocks not supported");
    }

    public AttachedToSurfaceFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        super(definition, blockBuilder);

        Map<Side, Block> blockMap = Maps.newEnumMap(Side.class);
        if (definition.getData().hasSection("top")) {
            Block block = blockBuilder.constructSimpleBlock(definition, "top", new BlockUri(definition.getUrn(),
                    new Name(Side.TOP.name())), this);
            block.setRotation(Rotation.rotate(Pitch.CLOCKWISE_270));
            blockMap.put(Side.TOP, block);
        }
        if (definition.getData().hasSection("front")) {
            for (Rotation rot : Rotation.horizontalRotations()) {
                Side side = rot.rotate(Side.FRONT);
                blockMap.put(side, blockBuilder.constructTransformedBlock(definition, side.toString().toLowerCase(Locale.ENGLISH), rot,
                        new BlockUri(definition.getUrn(), new Name(side.name())), this));
            }
        }
        if (definition.getData().hasSection("bottom")) {
            Block block = blockBuilder.constructSimpleBlock(definition, "bottom", new BlockUri(definition.getUrn(),
                    new Name(Side.BOTTOM.name())), this);
            block.setRotation(Rotation.rotate(Pitch.CLOCKWISE_90));
            blockMap.put(Side.BOTTOM, block);
        }

        for (Side side : Side.values()) {
            Block block = blockMap.get(side);
            if (block != null) {
                blocks.put(side, block);
            }
        }
        if (blocks.containsKey(Side.TOP)) {
            archetype = blocks.get(Side.TOP);
        } else {
            archetype = blocks.get(Side.FRONT);
        }
    }

    @Override
    public Block getBlockForPlacement(BlockPlacementData data) {
        return blocks.get(data.attachmentSide);
    }

    @Override
    public Block getArchetypeBlock() {
        return archetype;
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

    public Side getSideAttachedTo(Block block) {
        for (Map.Entry<Side, Block> sideBlockEntry : blocks.entrySet()) {
            if (sideBlockEntry.getValue().equals(block)) {
                return sideBlockEntry.getKey();
            }
        }
        return null;
    }

}
