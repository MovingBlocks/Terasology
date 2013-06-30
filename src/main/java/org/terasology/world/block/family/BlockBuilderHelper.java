/*
 * Copyright 2013 Moving Blocks
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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.terasology.asset.AssetUri;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.world.block.Block;
import org.terasology.world.block.loader.BlockDefinition;

import javax.vecmath.Matrix3d;
import java.util.Map;

public interface BlockBuilderHelper {

    public Block constructSimpleBlock(AssetUri blockDefUri, BlockDefinition blockDefinition);

    public Map<Side, Block> constructHorizontalRotatedBlocks(AssetUri blockDefUri, BlockDefinition blockDefinition);

    public Block constructTransformedBlock(AssetUri blockDefUri, BlockDefinition blockDefinition, Rotation rotation);

    public BlockDefinition getBlockDefinitionForSection(JsonObject json, String sectionName);

}
