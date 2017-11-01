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

import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

import java.util.Arrays;

/**
 * The standard block group consisting of a single symmetrical block that doesn't need rotations
 */
@RegisterBlockFamily("symmetric")
public class SymmetricFamily extends AbstractBlockFamily {

    private Block block;

    public SymmetricFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        super(definition, shape, blockBuilder);
        block = blockBuilder.constructSimpleBlock(definition, shape);
        BlockUri uri;
        if (CUBE_SHAPE_URN.equals(shape.getUrn())) {
            uri = new BlockUri(definition.getUrn());
        } else {
            uri = new BlockUri(definition.getUrn(), shape.getUrn());
        }

        block.setBlockFamily(this);
        block.setUri(uri);

        this.setBlockUri(uri);
        this.setCategory(definition.getCategories());
    }

    public SymmetricFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        super(definition, blockBuilder);
        BlockUri uri = new BlockUri(definition.getUrn());

        block = blockBuilder.constructSimpleBlock(definition);
        block.setBlockFamily(this);
        block.setUri(uri);
        this.setBlockUri(uri);
        this.setCategory(definition.getCategories());
    }


    @Override
    public Block getBlockForPlacement(Vector3i location, Side attachmentSide, Side direction) {
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
        return Arrays.asList(block);
    }

}
