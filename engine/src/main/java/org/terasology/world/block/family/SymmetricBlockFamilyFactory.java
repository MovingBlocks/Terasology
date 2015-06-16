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

import org.terasology.world.block.Block;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

@RegisterBlockFamilyFactory("symmetric")
public class SymmetricBlockFamilyFactory implements BlockFamilyFactory {

    @Override
    public BlockFamily createBlockFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        if (definition.isFreeform()) {
            throw new IllegalStateException("A shape must be provided when creating a family for a freeform block family definition");
        }
        Block block = blockBuilder.constructSimpleBlock(definition);
        return new SymmetricFamily(new BlockUri(definition.getUrn()), block, definition.getCategories());
    }

    @Override
    public BlockFamily createBlockFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        if (!definition.isFreeform()) {
            throw new IllegalStateException("A shape cannot be provided when creating a family for a non-freeform block family definition");
        }
        Block block = blockBuilder.constructSimpleBlock(definition, shape);
        BlockUri uri;
        if (CUBE_SHAPE_URN.equals(shape.getUrn())) {
            uri = new BlockUri(definition.getUrn());
        } else {
            uri = new BlockUri(definition.getUrn(), shape.getUrn());
        }
        return new SymmetricFamily(uri, block, definition.getCategories());
    }

    @Override
    public boolean isFreeformSupported() {
        return false;
    }
}
