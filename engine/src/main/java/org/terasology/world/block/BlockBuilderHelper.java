/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.world.block;

import org.terasology.math.Rotation;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.loader.SectionDefinitionData;
import org.terasology.world.block.shapes.BlockShape;

public interface BlockBuilderHelper {

    Block constructSimpleBlock(BlockFamilyDefinition definition, BlockUri uri, BlockFamily blockFamily);

    Block constructSimpleBlock(BlockFamilyDefinition definition, BlockShape shape, BlockUri uri, BlockFamily blockFamily);

    Block constructSimpleBlock(BlockFamilyDefinition definition, String section, BlockUri uri, BlockFamily blockFamily);

    Block constructSimpleBlock(BlockFamilyDefinition definition, BlockShape shape, String section, BlockUri uri, BlockFamily blockFamily);

    Block constructTransformedBlock(BlockFamilyDefinition definition, Rotation rotation, BlockUri uri, BlockFamily blockFamily);

    Block constructTransformedBlock(BlockFamilyDefinition definition, String section, Rotation rotation, BlockUri uri, BlockFamily blockFamily);

    Block constructTransformedBlock(BlockFamilyDefinition definition, BlockShape shape, Rotation rotation, BlockUri uri, BlockFamily blockFamily);

    Block constructTransformedBlock(BlockFamilyDefinition definition, BlockShape shape, String section, Rotation rotation, BlockUri uri, BlockFamily blockFamily);

    Block constructCustomBlock(String defaultName, BlockShape shape, Rotation rotation, SectionDefinitionData section, BlockUri uri, BlockFamily blockFamily);
}
