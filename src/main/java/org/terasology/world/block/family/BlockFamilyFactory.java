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

import com.google.gson.JsonObject;
import org.terasology.asset.AssetUri;
import org.terasology.world.block.loader.BlockDefinition;

public interface BlockFamilyFactory {
    /**
     * Called by the Block Loader to create a block family.
     *
     *
     * @param blockBuilder
     * @param blockDefUri
     * @param blockDefinition
     * @param blockDefJson
     * @return
     */
    public BlockFamily createBlockFamily(BlockBuilderHelper blockBuilder, AssetUri blockDefUri, BlockDefinition blockDefinition, JsonObject blockDefJson);

}
