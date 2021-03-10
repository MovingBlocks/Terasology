// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.family;

import org.terasology.engine.world.block.loader.BlockFamilyDefinition;
import org.terasology.engine.world.block.shapes.BlockShape;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockBuilderHelper;
import org.terasology.engine.world.block.BlockUri;

import java.util.Collections;

/**
 * The standard block family consisting of a single symmetrical block that doesn't have unique rotations.
 */
@RegisterBlockFamily("symmetric")
public class SymmetricFamily extends AbstractBlockFamily {

    private Block block;

    public SymmetricFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        super(definition, shape, blockBuilder);
        BlockUri uri;
        if (CUBE_SHAPE_URN.equals(shape.getUrn())) {
            uri = new BlockUri(definition.getUrn());
        } else {
            uri = new BlockUri(definition.getUrn(), shape.getUrn());
        }

        block = blockBuilder.constructSimpleBlock(definition, shape, uri, this);

        setBlockUri(uri);
    }

    public SymmetricFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        super(definition, blockBuilder);
        block = blockBuilder.constructSimpleBlock(definition, new BlockUri(definition.getUrn()), this);
    }

    @Override
    public Block getBlockForPlacement(BlockPlacementData data) {
        return block;
    }

    @Override
    public Block getArchetypeBlock() {
        return block;
    }


    @Override
    public Block getBlockFor(BlockUri blockUri) {
        if (getURI().equals(blockUri.getFamilyUri()) && blockUri.getIdentifier().isEmpty()) {
            return block;
        }
        return null;
    }

    @Override
    public Iterable<Block> getBlocks() {
        return Collections.singletonList(block);
    }
}
