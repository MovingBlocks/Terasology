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
package org.terasology.world.block.family;

import com.google.common.collect.Sets;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.math.JomlUtil;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

import java.util.Locale;
import java.util.Set;

public abstract class AbstractBlockFamily implements BlockFamily {

    private BlockUri uri;
    private Set<String> categories = Sets.newHashSet();

    protected AbstractBlockFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        setBlockUri(new BlockUri(definition.getUrn()));
        setCategory(definition.getCategories());
    }

    protected void setCategory(Iterable<String> newCategories) {
        for (String category : newCategories) {
            this.categories.add(category.toLowerCase(Locale.ENGLISH));
        }
    }

    protected void setBlockUri(BlockUri newUri) {
        uri = newUri;
    }


    @Override
    public BlockPlacement calculateBlockPlacement(BlockPlacementData blockPlacementData) {
        Vector3i position = calculateStandardAttachmentPosition(blockPlacementData);

        Block block = getBlockForPlacement(blockPlacementData, position);

        return new BlockPlacement(position, block);
    }

    /**
     * Get the block that is appropriate for placement in the given situation,
     * which is determined by the provided block placement data.
     *
     * @param data block placement data
     * @return The appropriate block
     */
    protected abstract Block getBlockForPlacement(BlockPlacementData data, Vector3ic position);

    protected Vector3i calculateStandardAttachmentPosition(BlockPlacementData blockPlacementData) {
        Vector3ic attachmentDirection = blockPlacementData.attachmentSide.direction();
        return blockPlacementData.targetPosition.add(attachmentDirection, new Vector3i());
    }

    @Override
    public BlockUri getURI() {
        return uri;
    }

    @Override
    public String getDisplayName() {
        return getArchetypeBlock().getDisplayName();
    }

    @Override
    public Iterable<String> getCategories() {
        return categories;
    }

    @Override
    public boolean hasCategory(String category) {
        return categories.contains(category.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public boolean canBlockReplace(Block targetBlock, Block replacingBlock) {
        return targetBlock.isReplacementAllowed() && !targetBlock.isTargetable();
    }

    @Override
    public String toString() {
        String familyType = "";
        RegisterBlockFamily registerInfo = this.getClass().getAnnotation(RegisterBlockFamily.class);
        if (registerInfo != null) {
            familyType = registerInfo.value();
        }
        return "BlockFamily[" + familyType + "," + uri.toString() + "]";
    }
}
