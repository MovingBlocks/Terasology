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

import com.google.api.client.util.StringUtils;
import com.google.common.collect.Maps;
import org.joml.Vector3ic;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
    private final Map<Side, Block> blocks = Maps.newEnumMap(Side.class);

    private final Optional<BlockUri> fullBlockUri;

    @In
    private BlockManager blockManager;

    public HorizontalFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        this(definition, Optional.ofNullable(shape), blockBuilder);
    }

    public HorizontalFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        this(definition, Optional.empty(), blockBuilder);
    }

    private HorizontalFamily(BlockFamilyDefinition definition, Optional<BlockShape> shape, BlockBuilderHelper blockBuilder) {
        super(definition, blockBuilder);
        fullBlockUri = Optional.ofNullable(definition.getData().getSection("top").getFullBlock()).map(BlockUri::new);

        calculateBlockUri(definition, shape);

        Rotation.horizontalRotations().forEach(rot -> calculateBlockForRotation(rot, definition, shape, blockBuilder));
    }

    private void calculateBlockForRotation(Rotation rot, BlockFamilyDefinition definition,
                                           Optional<BlockShape> shape, BlockBuilderHelper blockBuilder) {
        Side side = rot.rotate(Side.FRONT);
        Block block;

        BlockUri sideBlockUri = new BlockUri(getURI(), new Name(side.name()));

        if (shape.isPresent()) {
            block = blockBuilder.constructTransformedBlock(
                    definition,
                    shape.get(),
                    side.toString().toLowerCase(Locale.ENGLISH),
                    rot,
                    sideBlockUri,
                    this);
        } else {
            block = blockBuilder.constructTransformedBlock(
                    definition,
                    side.toString().toLowerCase(Locale.ENGLISH),
                    rot,
                    sideBlockUri,
                    this);
        }

        if (block == null) {
            throw new IllegalArgumentException("Missing block for side: " + side.toString());
        }

        blocks.put(side, block);
    }

    private void calculateBlockUri(BlockFamilyDefinition definition, Optional<BlockShape> shape) {
        if (shape.isPresent()) {
            if (CUBE_SHAPE_URN.equals(shape.get().getUrn())) {
                setBlockUri(new BlockUri(definition.getUrn()));
            } else {
                setBlockUri(new BlockUri(definition.getUrn(), shape.get().getUrn()));
            }
        } else {
            setBlockUri(new BlockUri(definition.getUrn()));
        }
    }

    protected Side getArchetypeSide() {
        return Side.FRONT;
    }

    @Override
    public boolean canBlockReplace(Block targetBlock, Block replacingBlock) {
        if (fullBlockUri.isPresent() && replacingBlock.getURI().getFamilyUri().equals(fullBlockUri.get())) {
            return true;
        } else {
            return super.canBlockReplace(targetBlock, replacingBlock);
        }
    }

    @Override
    public BlockPlacement calculateBlockPlacement(BlockPlacementData data) {
        BlockUri targetUri = data.target.getComponent(BlockComponent.class).block.getURI().getFamilyUri();

        if (fullBlockUri.isPresent() && targetUri.equals(getURI()) && data.attachmentSide == Side.TOP) {

            Side side = data.targetBlock.getRotation().rotate(Side.FRONT);
            BlockUri sideUri = new BlockUri(fullBlockUri.get(), new Name(side.name()));
            Optional<Block> sideBlock = Optional.ofNullable(blockManager.getBlock(sideUri));

            Block block = sideBlock.orElseGet(() -> blockManager.getBlock(fullBlockUri.get()));
            assert(block != null);

            return new BlockPlacement(data.targetPosition, block);
        } else {
            return super.calculateBlockPlacement(data);
        }
    }

    @Override
    protected Block getBlockForPlacement(BlockPlacementData data, Vector3ic position) {
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
