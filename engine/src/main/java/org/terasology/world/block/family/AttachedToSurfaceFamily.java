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
import org.terasology.math.Pitch;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.naming.Name;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

import java.util.Locale;
import java.util.Map;

@RegisterBlockFamily("attachedToSurface")
@BlockSections({"front", "left", "right", "back", "top", "bottom"})
@MultiSections({
        @MultiSection(name = "all", coversSection = "front", appliesToSections = {"front", "left", "right", "back", "top", "bottom"}),
        @MultiSection(name = "topBottom", coversSection = "top", appliesToSections = {"top", "bottom"}),
        @MultiSection(name = "sides", coversSection = "front", appliesToSections = {"front", "left", "right", "back"})})
public class AttachedToSurfaceFamily extends AbstractBlockFamily {


    private Map<Side, Block> blocks = Maps.newEnumMap(Side.class);
    private Block archetype;

    public AttachedToSurfaceFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        super(definition, shape, blockBuilder);
        throw new UnsupportedOperationException("Freeform blocks not supported");
    }

    public AttachedToSurfaceFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        super(definition, blockBuilder);

        Map<Side, Block> blockMap = Maps.newEnumMap(Side.class);
        if (definition.getData().hasSection("top")) {
            Block block = blockBuilder.constructSimpleBlock(definition, "top", new BlockUri(definition.getUrn(), new Name(Side.TOP.name())), this);
            block.setRotation(Rotation.rotate(Pitch.CLOCKWISE_270));
            blockMap.put(Side.TOP, block);
        }
        if (definition.getData().hasSection("front")) {
            for (Rotation rot : Rotation.horizontalRotations()) {
                Side side = rot.rotate(Side.FRONT);
                blockMap.put(side, blockBuilder.constructTransformedBlock(definition, side.toString().toLowerCase(Locale.ENGLISH), rot,
                        new BlockUri(definition.getUrn(), new Name(side.name())), this));
            }
        }
        if (definition.getData().hasSection("bottom")) {
            Block block = blockBuilder.constructSimpleBlock(definition, "bottom", new BlockUri(definition.getUrn(), new Name(Side.BOTTOM.name())), this);
            block.setRotation(Rotation.rotate(Pitch.CLOCKWISE_90));
            blockMap.put(Side.BOTTOM, block);
        }

        for (Side side : Side.getAllSides()) {
            Block block = blockMap.get(side);
            if (block != null) {
                blocks.put(side, block);
            }
        }
        if (blocks.containsKey(Side.TOP)) {
            archetype = blocks.get(Side.TOP);
        } else {
            archetype = blocks.get(Side.FRONT);
        }
    }

    @Override
    public Block getBlockForPlacement(BlockPlacementData data) {
        return blocks.get(data.attachmentSide);
    }

    @Override
    public Block getBlockForPlacement(Vector3i location, Side attachmentSide, Side direction) {
        return blocks.get(attachmentSide);
    }

    @Override
    public Block getArchetypeBlock() {
        return archetype;
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

    public Side getSideAttachedTo(Block block) {
        for (Map.Entry<Side, Block> sideBlockEntry : blocks.entrySet()) {
            if (sideBlockEntry.getValue().equals(block)) {
                return sideBlockEntry.getKey();
            }
        }
        return null;
    }

}
