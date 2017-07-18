/*
 * Copyright 2015 MovingBlocks
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

import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

/**
 */
@RegisterBlockFamilyFactory("freeform")
public class FreeformBlockFamilyFactory implements BlockFamilyFactory {

    private BlockFamilyFactory side = new SideBlockFamilyFactory();
    private BlockFamilyFactory edge = new EdgeBlockFamilyFactory();
    private BlockFamilyFactory corner = new CornerBlockFamilyFactory();
    private BlockFamilyFactory horizontal = new HorizontalBlockFamilyFactory();
    private BlockFamilyFactory symmetric = new SymmetricBlockFamilyFactory();

    @Override
    public BlockFamily createBlockFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        throw new UnsupportedOperationException("Shape expected");
    }

    @Override
    public BlockFamily createBlockFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        switch( shape.getBlockShapePlacement().getType() ) {
            case SIDE:
                return side.createBlockFamily(definition, shape, blockBuilder);
            case EDGE:
                return edge.createBlockFamily(definition, shape, blockBuilder);
            case CORNER:
                return corner.createBlockFamily(definition, shape, blockBuilder);
            case HORIZONTAL_SIDE:
                return horizontal.createBlockFamily(definition, shape, blockBuilder);
            case CUBE:
                return symmetric.createBlockFamily(definition, shape, blockBuilder);
        }
        return symmetric.createBlockFamily(definition, shape, blockBuilder);
    }

    @Override
    public boolean isFreeformSupported() {
        return true;
    }
}
