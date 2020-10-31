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

import com.google.common.collect.Maps;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.naming.Name;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

import java.util.Locale;
import java.util.Map;

/**
 * Block group for blocks that can be oriented around the vertical axis.
 */
@RegisterBlockFamily("horizontal")
@BlockSections({"front", "left", "right", "back", "top", "bottom"})
@MultiSections({
        @MultiSection(name = "all", coversSection = "front", appliesToSections = {"front", "left", "right", "back", "top", "bottom"}),
        @MultiSection(name = "topBottom", coversSection = "top", appliesToSections = {"top", "bottom"}),
        @MultiSection(name = "sides", coversSection = "front", appliesToSections = {"front", "left", "right", "back"})})
public class HorizontalFamily extends AbstractBlockFamily implements SideDefinedBlockFamily {
    private Map<Side, Block> blocks = Maps.newEnumMap(Side.class);

    public HorizontalFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        super(definition, shape, blockBuilder);
        BlockUri uri;
        if (CUBE_SHAPE_URN.equals(shape.getUrn())) {
            uri = new BlockUri(definition.getUrn());
        } else {
            uri = new BlockUri(definition.getUrn(), shape.getUrn());
        }
        for (Rotation rot : Rotation.horizontalRotations()) {
            Side side = rot.rotate(Side.FRONT);
            Block block = blockBuilder.constructTransformedBlock(definition, shape, side.toString().toLowerCase(Locale.ENGLISH), rot,
                    new BlockUri(uri, new Name(side.name())), this);
            if (block == null) {
                throw new IllegalArgumentException("Missing block for side: " + side.toString());
            }
            blocks.put(side, block);
        }
        setBlockUri(uri);
    }

    public HorizontalFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        super(definition, blockBuilder);
        BlockUri uri = new BlockUri(definition.getUrn());
        for (Rotation rot : Rotation.horizontalRotations()) {
            Side side = rot.rotate(Side.FRONT);

            Block block = blockBuilder.constructTransformedBlock(definition, side.toString().toLowerCase(Locale.ENGLISH), rot, new BlockUri(uri, new Name(side.name())), this);
            if (block == null) {
                throw new IllegalArgumentException("Missing block for side: " + side.toString());
            }
            blocks.put(side, block);
        }
    }

    protected Side getArchetypeSide() {
        return Side.FRONT;
    }

    @Override
    public Block getBlockForPlacement(BlockPlacementData data) {
        if (data.attachmentSide.isHorizontal()) {
            return blocks.get(data.attachmentSide);
        } else {
            Side secondaryDirection = Side.inDirection(-data.viewingDirection.x(), 0, -data.viewingDirection.z());
            return blocks.get(secondaryDirection);
        }
    }

    @Override
    public Block getArchetypeBlock() {
        return blocks.get(this.getArchetypeSide());
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

    @Override
    public Block getBlockForSide(Side side) {
        return blocks.get(side);
    }

    @Override
    public Side getSide(Block block) {
        for (Map.Entry<Side, Block> sideBlockEntry : blocks.entrySet()) {
            if (block == sideBlockEntry.getValue()) {
                return sideBlockEntry.getKey();
            }
        }
        return null;
    }


}
