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

import org.terasology.assets.ResourceUrn;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface BlockFamilyFactory {

    ResourceUrn CUBE_SHAPE_URN = new ResourceUrn("engine:cube");

    BlockFamily createBlockFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder);

    default BlockFamily createBlockFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        throw new UnsupportedOperationException("Freeform blocks not supported");
    }

    /**
     * @return The set of block names this family contains. A block definition will be loaded for each one.
     */
    default Set<String> getSectionNames() {
        return Collections.emptySet();
    }

    /**
     * @return The multi-sections that should be applied to the final main sections.
     */
    default List<MultiSection> getMultiSections() {
        return Collections.emptyList();
    }

    default boolean isFreeformSupported() {
        return false;
    }

}
