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
package org.terasology.world.block.internal;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.terasology.math.JomlUtil;
import org.terasology.utilities.Assets;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector2f;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockAppearance;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.loader.SectionDefinitionData;
import org.terasology.world.block.shapes.BlockMeshPart;
import org.terasology.world.block.shapes.BlockShape;
import org.terasology.world.block.tiles.BlockTile;
import org.terasology.world.block.tiles.WorldAtlas;

import java.util.Map;

public class BlockBuilder implements BlockBuilderHelper {

    private WorldAtlas worldAtlas;

    private BlockShape cubeShape;
    private BlockShape lowShape;
    private BlockShape topShape;

    public BlockBuilder(WorldAtlas worldAtlas) {
        this.worldAtlas = worldAtlas;

        cubeShape = Assets.get("engine:cube", BlockShape.class).get();
        lowShape = Assets.get("engine:trimmedLoweredCube", BlockShape.class).get();
        topShape = Assets.get("engine:trimmedRaisedCube",  BlockShape.class).get();
    }

    @Override
    public Block constructSimpleBlock(BlockFamilyDefinition definition, BlockUri uri, BlockFamily blockFamily) {
        BlockShape shape = definition.getData().getBaseSection().getShape();
        if (shape == null) {
            shape = cubeShape;
        }

        return constructCustomBlock(definition.getUrn().getResourceName().toString(), shape, Rotation.none(), definition.getData().getBaseSection(), uri, blockFamily);
    }

    @Override
    public Block constructSimpleBlock(BlockFamilyDefinition definition, BlockShape shape, BlockUri uri, BlockFamily blockFamily) {
        return constructCustomBlock(definition.getUrn().getResourceName().toString(), shape, Rotation.none(), definition.getData().getBaseSection(), uri, blockFamily);
    }

    @Override
    public Block constructSimpleBlock(BlockFamilyDefinition definition, String section, BlockUri uri, BlockFamily blockFamily) {
        BlockShape shape = definition.getData().getSection(section).getShape();
        if (shape == null) {
            shape = cubeShape;
        }

        return constructCustomBlock(definition.getUrn().getResourceName().toString(), shape, Rotation.none(), definition.getData().getSection(section), uri, blockFamily);
    }

    @Override
    public Block constructSimpleBlock(BlockFamilyDefinition definition, BlockShape shape, String section, BlockUri uri, BlockFamily blockFamily) {
        return constructCustomBlock(definition.getUrn().getResourceName().toString(), shape, Rotation.none(), definition.getData().getSection(section), uri, blockFamily);
    }

    @Override
    public Block constructTransformedBlock(BlockFamilyDefinition definition, Rotation rotation, BlockUri uri, BlockFamily blockFamily) {
        BlockShape shape = definition.getData().getBaseSection().getShape();
        if (shape == null) {
            shape = cubeShape;
        }

        return constructCustomBlock(definition.getUrn().getResourceName().toString(), shape, rotation, definition.getData().getBaseSection(), uri, blockFamily);
    }

    @Override
    public Block constructTransformedBlock(BlockFamilyDefinition definition, String section, Rotation rotation, BlockUri uri, BlockFamily blockFamily) {
        BlockShape shape = definition.getData().getSection(section).getShape();
        if (shape == null) {
            shape = cubeShape;
        }

        return constructCustomBlock(definition.getUrn().getResourceName().toString(), shape, rotation, definition.getData().getSection(section), uri, blockFamily);
    }

    @Override
    public Block constructTransformedBlock(BlockFamilyDefinition definition, BlockShape shape, Rotation rotation, BlockUri uri, BlockFamily blockFamily) {
        return constructCustomBlock(definition.getUrn().getResourceName().toString(), shape, rotation, definition.getData().getBaseSection(), uri, blockFamily);
    }

    @Override
    public Block constructTransformedBlock(BlockFamilyDefinition definition, BlockShape shape, String section, Rotation rotation, BlockUri uri, BlockFamily blockFamily) {
        return constructCustomBlock(definition.getUrn().getResourceName().toString(), shape, rotation, definition.getData().getSection(section), uri, blockFamily);
    }

    @Override
    public Block constructCustomBlock(String defaultName, BlockShape shape, Rotation rotation, SectionDefinitionData section, BlockUri uri, BlockFamily blockFamily) {
        Block block = createRawBlock(defaultName, section);
        block.setRotation(rotation);
        block.setPrimaryAppearance(createAppearance(shape, section.getBlockTiles(), rotation));
        setBlockFullSides(block, shape, rotation);
        block.setCollision(shape.getCollisionOffset(rotation), shape.getCollisionShape(rotation));

        block.setUri(uri);
        block.setBlockFamily(blockFamily);

        // Lowered mesh for liquids
        if (block.isLiquid()) {
            applyLiquidShapes(block, section.getBlockTiles());
        }

        return block;
    }

    private Block createRawBlock(String defaultName, SectionDefinitionData def) {
        Block block = new Block();
        block.setLiquid(def.isLiquid());
        block.setWater(def.isWater());
        block.setGrass(def.isGrass());
        block.setIce(def.isIce());
        block.setHardness(def.getHardness());
        block.setAttachmentAllowed(def.isAttachmentAllowed());
        block.setReplacementAllowed(def.isReplacementAllowed());
        block.setSupportRequired(def.isSupportRequired());
        block.setPenetrable(def.isPenetrable());
        block.setTargetable(def.isTargetable());
        block.setClimbable(def.isClimbable());
        block.setTranslucent(def.isTranslucent());
        block.setDoubleSided(def.isDoubleSided());
        block.setShadowCasting(def.isShadowCasting());
        block.setWaving(def.isWaving());
        block.setLuminance(def.getLuminance());
        block.setTint(def.getTint());
        if (Strings.isNullOrEmpty(def.getDisplayName())) {
            block.setDisplayName(properCase(defaultName));
        } else {
            block.setDisplayName(def.getDisplayName());
        }
        block.setSounds(def.getSounds());

        block.setMass(def.getMass());
        block.setDebrisOnDestroy(def.isDebrisOnDestroy());
        block.setFriction(def.getFriction());
        block.setRestitution(def.getRestitution());

        if (def.getEntity() != null) {
            block.setPrefab(def.getEntity().getPrefab());
            block.setKeepActive(def.getEntity().isKeepActive());
        }

        if (def.getInventory() != null) {
            block.setStackable(def.getInventory().isStackable());
            block.setDirectPickup(def.getInventory().isDirectPickup());
        }

        return block;
    }

    private BlockAppearance createAppearance(BlockShape shape, Map<BlockPart, BlockTile> tiles, Rotation rot) {
        Map<BlockPart, BlockMeshPart> meshParts = Maps.newEnumMap(BlockPart.class);
        Map<BlockPart, Vector2f> textureAtlasPositions = Maps.newEnumMap(BlockPart.class);
        for (BlockPart part : BlockPart.values()) {
            // TODO: Need to be more sensible with the texture atlas. Because things like block particles read from a part that may not exist, we're being fairly lenient
            Vector2f atlasPos;
            int frameCount;
            BlockTile tile = tiles.get(part);
            if (tile == null) {
                atlasPos = new Vector2f();
                frameCount = 1;
            } else {
                atlasPos = worldAtlas.getTexCoords(tile, shape.getMeshPart(part) != null);
                frameCount = tile.getLength();
            }
            BlockPart targetPart = part.rotate(rot);
            textureAtlasPositions.put(targetPart, atlasPos);
            if (shape.getMeshPart(part) != null) {
                meshParts.put(targetPart,
                    shape.getMeshPart(part).rotate(JomlUtil.from(rot.getQuat4f())).mapTexCoords(JomlUtil.from(atlasPos),
                    worldAtlas.getRelativeTileSize(), frameCount));
            }
        }
        return new BlockAppearance(meshParts, textureAtlasPositions);
    }

    private void setBlockFullSides(Block block, BlockShape shape, Rotation rot) {
        for (Side side : Side.getAllSides()) {
            BlockPart targetPart = BlockPart.fromSide(rot.rotate(side));
            block.setFullSide(targetPart.getSide(), shape.isBlockingSide(side));
        }
    }

    private void applyLiquidShapes(Block block, Map<BlockPart, BlockTile> tiles) {
        for (Side side : Side.getAllSides()) {
            BlockPart part = BlockPart.fromSide(side);
            BlockTile blockTile = tiles.get(part);
            if (blockTile != null) {
                BlockMeshPart lowMeshPart = lowShape
                        .getMeshPart(part)
                        .mapTexCoords(JomlUtil.from(worldAtlas.getTexCoords(blockTile, true)), worldAtlas.getRelativeTileSize(), blockTile.getLength());
                block.setLowLiquidMesh(part.getSide(), lowMeshPart);
                BlockMeshPart topMeshPart = topShape
                        .getMeshPart(part)
                        .mapTexCoords(JomlUtil.from(worldAtlas.getTexCoords(blockTile, true)), worldAtlas.getRelativeTileSize(), blockTile.getLength());
                block.setTopLiquidMesh(part.getSide(), topMeshPart);
            }
        }
    }

    private String properCase(String s) {
        if (s.length() > 1) {
            return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
        } else {
            return s.toUpperCase();
        }
    }
}
