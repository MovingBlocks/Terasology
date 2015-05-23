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

import com.google.common.collect.Maps;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;

import java.util.Map;

public class FullRotationFamily extends AbstractBlockFamily implements RotationBlockFamily {
    private Map<Rotation, Block> blocks;

    private Block archetypeBlock;

    private Map<BlockUri, Block> blockUriMap = Maps.newHashMap();

    public FullRotationFamily(BlockUri uri, Iterable<String> categories, Rotation archetypeRotation,
                              Map<Rotation, Block> blocks) {
        super(uri, categories);
        this.blocks = blocks;

        for (Map.Entry<Rotation, Block> rotationBlockEntry : blocks.entrySet()) {
            Rotation rotation = rotationBlockEntry.getKey();
            Block block = rotationBlockEntry.getValue();
            block.setBlockFamily(this);
            BlockUri blockUri = new BlockUri(uri, rotation.getYaw().ordinal() + "." + rotation.getPitch().ordinal() + "." + rotation.getRoll().ordinal());
            block.setUri(blockUri);
            blockUriMap.put(blockUri, block);
        }

        archetypeBlock = blocks.get(archetypeRotation);
    }

    @Override
    public Block getBlockForPlacement(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i location, Side attachmentSide, Side direction) {
        // Find first one so that FRONT Side of the original block is same as attachmentSide
        for (Map.Entry<Rotation, Block> rotationBlockEntry : blocks.entrySet()) {
            if (rotationBlockEntry.getKey().rotate(Side.FRONT) == attachmentSide) {
                return rotationBlockEntry.getValue();
            }
        }

        return null;
    }

    @Override
    public Block getArchetypeBlock() {
        return archetypeBlock;
    }

    @Override
    public Block getBlockFor(BlockUri blockUri) {
        return blockUriMap.get(blockUri);
    }

    @Override
    public Iterable<Block> getBlocks() {
        return blocks.values();
    }

    @Override
    public Block getBlockForClockwiseRotation(Block currentBlock, Side sideToRotateAround) {
        // This definitely can be done more efficiently, but I'm too lazy to figure it out and it's going to be
        // invoked once in a blue moon anyway, so we can do it the hard way
        Rotation currentRotation = findRotationForBlock(currentBlock);

        // Pick a side we want to rotate
        SideMapping sideMapping = findSideMappingForSide(sideToRotateAround);

        // Find which side the side we want to keep was originally at
        Side originalSide = findOriginalSide(currentRotation, sideToRotateAround);

        // Find which side we want to rotate was originally at
        Side originalRotatedSide = findOriginalSide(currentRotation, sideMapping.originalSide);

        // This is the side we want the leftRelativeToEndUpAt
        Side resultRotatedSide = sideMapping.resultSide;

        Rotation resultRotation = findDesiredRotation(originalSide, sideToRotateAround, originalRotatedSide, resultRotatedSide);

        return blocks.get(resultRotation);
    }

    private SideMapping findSideMappingForSide(Side side) {
        switch (side) {
            case TOP:
                return new SideMapping(Side.RIGHT, Side.FRONT);
            case BOTTOM:
                return new SideMapping(Side.RIGHT, Side.BACK);
            case RIGHT:
                return new SideMapping(Side.FRONT, Side.TOP);
            case LEFT:
                return new SideMapping(Side.FRONT, Side.BOTTOM);
            case FRONT:
                return new SideMapping(Side.TOP, Side.RIGHT);
            default:
                return new SideMapping(Side.TOP, Side.LEFT);
        }
    }

    @Override
    public Block getBlockForRotation(Rotation rotation) {
        return blocks.get(rotation);
    }

    @Override
    public Rotation getRotation(Block block) {
        return findRotationForBlock(block);
    }

    private Rotation findDesiredRotation(Side originalSide, Side relativeSide, Side originalLeftSide, Side resultSide) {
        for (Rotation rotation : Rotation.values()) {
            if (rotation.rotate(originalSide) == relativeSide
                && rotation.rotate(originalLeftSide) == resultSide) {
                return rotation;
            }
        }
        return null;
    }

    private Side findOriginalSide(Rotation rotation, Side resultSide) {
        for (Side side : Side.values()) {
            if (rotation.rotate(side) == resultSide) {
                return side;
            }
        }

        return null;
    }

    private Rotation findRotationForBlock(Block block) {
        for (Map.Entry<Rotation, Block> rotationBlockEntry : blocks.entrySet()) {
            if (rotationBlockEntry.getValue() == block) {
                return rotationBlockEntry.getKey();
            }
        }
        return null;
    }

    private static class SideMapping {
        private final Side originalSide;
        private final Side resultSide;

        private SideMapping(Side resultSide, Side originalSide) {
            this.resultSide = resultSide;
            this.originalSide = originalSide;
        }
    }
}
