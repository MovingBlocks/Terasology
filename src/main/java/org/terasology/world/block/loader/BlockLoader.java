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

package org.terasology.world.block.loader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.utilities.gson.CaseInsensitiveEnumTypeAdapterFactory;
import org.terasology.utilities.gson.JsonMergeUtil;
import org.terasology.utilities.gson.Vector4fHandler;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockAppearance;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockBuilderHelper;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.family.BlockFamilyFactory;
import org.terasology.world.block.family.BlockFamilyFactoryRegistry;
import org.terasology.world.block.family.HorizontalBlockFamily;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.shapes.BlockMeshPart;
import org.terasology.world.block.shapes.BlockShape;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Block Loader processes all the block assets, creating a set of Block Families and Freeform block uris.
 *
 * @author Immortius
 */
public class BlockLoader implements BlockBuilderHelper {
    public static final String AUTO_BLOCK_URL_FRAGMENT = "/auto/";

    private static final Logger logger = LoggerFactory.getLogger(BlockLoader.class);

    private JsonParser parser;
    private Gson gson;

    private BlockShape cubeShape;
    private BlockShape loweredShape;
    private BlockShape trimmedLoweredShape;

    private final WorldAtlas atlas;
    private BlockFamilyFactoryRegistry blockFamilyFactoryRegistry;

    public BlockLoader(BlockFamilyFactoryRegistry blockFamilyFactoryRegistry, WorldAtlas atlas) {
        this.atlas = atlas;
        parser = new JsonParser();
        gson = new GsonBuilder()
                .registerTypeAdapterFactory(new CaseInsensitiveEnumTypeAdapterFactory())
                .registerTypeAdapter(BlockDefinition.Tiles.class, new BlockTilesDefinitionHandler())
                .registerTypeAdapter(BlockDefinition.ColorSources.class, new BlockColorSourceDefinitionHandler())
                .registerTypeAdapter(BlockDefinition.ColorOffsets.class, new BlockColorOffsetDefinitionHandler())
                .registerTypeAdapter(Vector4f.class, new Vector4fHandler())
                .create();
        cubeShape = (BlockShape) Assets.get(new AssetUri(AssetType.SHAPE, "engine:cube"));
        loweredShape = (BlockShape) Assets.get(new AssetUri(AssetType.SHAPE, "engine:loweredCube"));
        trimmedLoweredShape = (BlockShape) Assets.get(new AssetUri(AssetType.SHAPE, "engine:trimmedLoweredCube"));
        this.blockFamilyFactoryRegistry = blockFamilyFactoryRegistry;
    }

    public LoadBlockDefinitionResults loadBlockDefinitions() {
        logger.info("Loading Blocks...");

        LoadBlockDefinitionResults result = new LoadBlockDefinitionResults();
        for (AssetUri blockDefUri : Assets.list(AssetType.BLOCK_DEFINITION)) {
            try {
                JsonElement rawJson = readJson(blockDefUri);
                if (rawJson != null) {
                    JsonObject blockDefJson = rawJson.getAsJsonObject();

                    // Don't process templates
                    if (blockDefJson.has("template") && blockDefJson.get("template").getAsBoolean()) {
                        continue;
                    }
                    logger.debug("Loading {}", blockDefUri);

                    BlockDefinition blockDef = createBlockDefinition(inheritData(blockDefUri, blockDefJson));

                    if (isShapelessBlockFamily(blockDef)) {
                        result.shapelessDefinitions.add(new FreeformFamily(new BlockUri(blockDefUri.getModuleName(), blockDefUri.getAssetName()), blockDef.categories));
                    } else {
                        if (blockDef.liquid) {
                            blockDef.rotation = null;
                            blockDef.shapes.clear();
                            blockDef.shape = trimmedLoweredShape.getURI().toSimpleString();
                        }

                        if (blockDef.shapes.isEmpty()) {
                            BlockFamilyFactory familyFactory = blockFamilyFactoryRegistry.getBlockFamilyFactory(blockDef.rotation);
                            result.families.add(familyFactory.createBlockFamily(this, blockDefUri, blockDef, blockDefJson));
                        } else {
                            result.families.addAll(processMultiBlockFamily(blockDefUri, blockDef));
                        }
                    }
                }
            } catch (JsonParseException | NullPointerException e) {
                logger.error("Failed to load block '{}'", blockDefUri, e);
            }
        }
        result.shapelessDefinitions.addAll(loadAutoBlocks());
        return result;
    }

    @Override
    public BlockDefinition getBlockDefinitionForSection(JsonObject json, String sectionName) {
        if (json.has(sectionName) && json.get(sectionName).isJsonObject()) {
            JsonObject sectionJson = json.getAsJsonObject(sectionName);
            json.remove(sectionName);
            JsonMergeUtil.mergeOnto(json, sectionJson);
            return createBlockDefinition(sectionJson);
        }
        return null;
    }

    public BlockFamily loadWithShape(BlockUri uri) {
        BlockShape shape = cubeShape;
        if (uri.hasShape()) {
            AssetUri shapeUri = uri.getShapeUri();
            if (!shapeUri.isValid()) {
                return null;
            }
            shape = (BlockShape) Assets.get(shapeUri);
            if (shape == null) {
                return null;
            }
        }
        AssetUri blockDefUri = new AssetUri(AssetType.BLOCK_DEFINITION, uri.getModuleName(), uri.getFamilyName());
        BlockDefinition def;
        if (AssetManager.getInstance().getAssetURLs(blockDefUri).isEmpty()) {
            // An auto-block
            def = new BlockDefinition();
        } else {
            def = createBlockDefinition(inheritData(blockDefUri, readJson(blockDefUri).getAsJsonObject()));
        }

        def.shape = (shape.getURI().toSimpleString());
        if (shape.isCollisionYawSymmetric()) {
            Block block = constructSingleBlock(blockDefUri, def);
            return new SymmetricFamily(uri, block, def.categories);
        } else {
            Map<Side, Block> blockMap = Maps.newEnumMap(Side.class);
            constructHorizontalBlocks(blockDefUri, def, blockMap);
            return new HorizontalBlockFamily(uri, blockMap, def.categories);
        }
    }

    private List<FreeformFamily> loadAutoBlocks() {
        logger.debug("Loading Auto Blocks...");
        List<FreeformFamily> result = Lists.newArrayList();
        for (AssetUri blockTileUri : Assets.list(AssetType.BLOCK_TILE)) {
            if (AssetManager.getInstance().getAssetURLs(blockTileUri).get(0).getPath().contains(AUTO_BLOCK_URL_FRAGMENT)) {
                logger.debug("Loading auto block {}", blockTileUri);
                BlockUri uri = new BlockUri(blockTileUri.getModuleName(), blockTileUri.getAssetName());
                result.add(new FreeformFamily(uri));
            }
        }
        return result;
    }

    private boolean isShapelessBlockFamily(BlockDefinition blockDef) {
        return blockDef.shapes.isEmpty() && blockDef.shape.isEmpty() && blockDef.rotation == null && !blockDef.liquid && blockDef.tiles == null;
    }

    private JsonObject inheritData(AssetUri rootAssetUri, JsonObject blockDefJson) {
        JsonObject parentObj = blockDefJson;
        while (parentObj.has("basedOn")) {
            AssetUri parentUri = new AssetUri(AssetType.BLOCK_DEFINITION, parentObj.get("basedOn").getAsString());
            if (rootAssetUri.equals(parentUri)) {
                logger.error("Circular inheritance detected in {}", rootAssetUri);
                break;
            } else if (!parentUri.isValid()) {
                logger.error("{} based on invalid uri: {}", rootAssetUri, parentObj.get("basedOn").getAsString());
                break;
            }
            JsonObject parent = readJson(parentUri).getAsJsonObject();
            JsonMergeUtil.mergeOnto(parent, blockDefJson);
            parentObj = parent;
        }
        return blockDefJson;
    }

    private List<BlockFamily> processMultiBlockFamily(AssetUri blockDefUri, BlockDefinition blockDef) {
        List<BlockFamily> result = Lists.newArrayList();
        for (String shapeString : blockDef.shapes) {
            AssetUri shapeUri = new AssetUri(AssetType.SHAPE, shapeString);
            BlockShape shape = (BlockShape) Assets.get(shapeUri);
            if (shape != null) {
                BlockUri familyUri;
                if (shape.equals(cubeShape)) {
                    familyUri = new BlockUri(blockDefUri.getModuleName(), blockDefUri.getAssetName());
                } else {
                    familyUri = new BlockUri(blockDefUri.getModuleName(), blockDefUri.getAssetName(), shapeUri.getModuleName(), shapeUri.getAssetName());
                }
                blockDef.shape = shapeString;
                if (shape.isCollisionYawSymmetric()) {
                    Block block = constructSingleBlock(blockDefUri, blockDef);
                    result.add(new SymmetricFamily(familyUri, block, blockDef.categories));
                } else {
                    Map<Side, Block> blockMap = Maps.newEnumMap(Side.class);
                    constructHorizontalBlocks(blockDefUri, blockDef, blockMap);
                    result.add(new HorizontalBlockFamily(familyUri, blockMap, blockDef.categories));
                }
            }
        }
        return result;
    }

    @Override
    public Block constructSimpleBlock(AssetUri blockDefUri, BlockDefinition blockDefinition) {
        return constructSingleBlock(blockDefUri, blockDefinition);
    }

    private Block constructSingleBlock(AssetUri blockDefUri, BlockDefinition blockDef) {
        Map<BlockPart, AssetUri> tileUris = prepareTiles(blockDef, blockDefUri);
        Map<BlockPart, Block.ColorSource> colorSourceMap = prepareColorSources(blockDef);
        Map<BlockPart, Vector4f> colorOffsetsMap = prepareColorOffsets(blockDef);
        BlockShape shape = getShape(blockDef);

        Block block = createRawBlock(blockDef, properCase(blockDefUri.getAssetName()));
        block.setPrimaryAppearance(createAppearance(shape, tileUris, Rotation.none()));
        setBlockFullSides(block, shape, Rotation.none());
        block.setCollision(shape.getCollisionOffset(Rotation.none()), shape.getCollisionShape(Rotation.none()));

        for (BlockPart part : BlockPart.values()) {
            block.setColorSource(part, colorSourceMap.get(part));
            block.setColorOffset(part, colorOffsetsMap.get(part));
        }

        // Lowered mesh for liquids
        if (block.isLiquid()) {
            applyLoweredShape(block, loweredShape, tileUris);
        }
        return block;
    }

    @Override
    public Map<Side, Block> constructHorizontalRotatedBlocks(AssetUri blockDefUri, BlockDefinition blockDefinition) {
        Map<Side, Block> result = Maps.newHashMap();
        constructHorizontalBlocks(blockDefUri, blockDefinition, result);
        return result;
    }

    @Override
    public Block constructTransformedBlock(AssetUri blockDefUri, BlockDefinition blockDef, Rotation rotation) {
        Map<BlockPart, AssetUri> tileUris = prepareTiles(blockDef, blockDefUri);
        Map<BlockPart, Block.ColorSource> colorSourceMap = prepareColorSources(blockDef);
        Map<BlockPart, Vector4f> colorOffsetsMap = prepareColorOffsets(blockDef);
        BlockShape shape = getShape(blockDef);

        Block block = createRawBlock(blockDef, properCase(blockDefUri.getAssetName()));
        block.setDirection(rotation.rotate(Side.FRONT));
        block.setPrimaryAppearance(createAppearance(shape, tileUris, rotation));
        setBlockFullSides(block, shape, rotation);
        block.setCollision(shape.getCollisionOffset(rotation), shape.getCollisionShape(rotation));

        for (BlockPart part : BlockPart.values()) {
            block.setColorSource(part, colorSourceMap.get(part));
            block.setColorOffset(part, colorOffsetsMap.get(part));
        }

        return block;
    }

    private void constructHorizontalBlocks(AssetUri blockDefUri, BlockDefinition blockDef, Map<Side, Block> blockMap) {
        for (Rotation rot : Rotation.horizontalRotations()) {
            blockMap.put(rot.rotate(Side.FRONT), constructTransformedBlock(blockDefUri, blockDef, rot));
        }
    }

    private Map<BlockPart, Vector4f> prepareColorOffsets(BlockDefinition blockDef) {
        Map<BlockPart, Vector4f> result = Maps.newEnumMap(BlockPart.class);
        for (BlockPart part : BlockPart.values()) {
            result.put(part, blockDef.colorOffset);
        }
        if (blockDef.colorOffsets != null) {
            for (BlockPart part : BlockPart.values()) {
                if (blockDef.colorOffsets.map.get(part) != null) {
                    result.put(part, blockDef.colorOffsets.map.get(part));
                }
            }
        }
        return result;
    }

    private Map<BlockPart, Block.ColorSource> prepareColorSources(BlockDefinition blockDef) {
        Map<BlockPart, Block.ColorSource> result = Maps.newEnumMap(BlockPart.class);
        for (BlockPart part : BlockPart.values()) {
            result.put(part, blockDef.colorSource);
        }
        if (blockDef.colorSources != null) {
            for (BlockPart part : BlockPart.values()) {
                if (blockDef.colorSources.map.get(part) != null) {
                    result.put(part, blockDef.colorSources.map.get(part));
                }
            }
        }
        return result;
    }

    private BlockShape getShape(BlockDefinition blockDef) {
        BlockShape shape = null;
        if (!blockDef.shape.isEmpty()) {
            shape = (BlockShape) Assets.get(new AssetUri(AssetType.SHAPE, blockDef.shape));
        }
        if (shape == null) {
            return cubeShape;
        }
        return shape;
    }

    private BlockAppearance createAppearance(BlockShape shape, Map<BlockPart, AssetUri> tileUris, Rotation rot) {
        Map<BlockPart, BlockMeshPart> meshParts = Maps.newEnumMap(BlockPart.class);
        Map<BlockPart, Vector2f> textureAtlasPositions = Maps.newEnumMap(BlockPart.class);
        for (BlockPart part : BlockPart.values()) {
            // TODO: Need to be more sensible with the texture atlas. Because things like block particles read from a part that may not exist, we're being fairly lenient
            Vector2f atlasPos = atlas.getTexCoords(tileUris.get(part), shape.getMeshPart(part) != null);
            BlockPart targetPart = part.rotate(rot);
            textureAtlasPositions.put(targetPart, atlasPos);
            if (shape.getMeshPart(part) != null) {
                meshParts.put(targetPart, shape.getMeshPart(part).rotate(rot.getQuat4f()).mapTexCoords(atlasPos, atlas.getRelativeTileSizeWithOffset()));
            }
        }
        return new BlockAppearance(meshParts, textureAtlasPositions);
    }

    private void setBlockFullSides(Block block, BlockShape shape, Rotation rot) {
        for (Side side : Side.values()) {
            BlockPart targetPart = BlockPart.fromSide(rot.rotate(side));
            block.setFullSide(targetPart.getSide(), shape.isBlockingSide(side));
        }
    }

    private void applyLoweredShape(Block block, BlockShape shape, Map<BlockPart, AssetUri> tileUris) {
        for (Side side : Side.values()) {
            BlockPart part = BlockPart.fromSide(side);
            BlockMeshPart meshPart = shape
                    .getMeshPart(part)
                    .rotate(Rotation.none().getQuat4f())
                    .mapTexCoords(atlas.getTexCoords(tileUris.get(part), true), atlas.getRelativeTileSizeWithOffset());
            block.setLoweredLiquidMesh(part.getSide(), meshPart);
        }
    }

    private Block createRawBlock(BlockDefinition def, String defaultName) {
        Block block = new Block();
        block.setLiquid(def.liquid);
        block.setWater(def.water);
        block.setLava(def.lava);
        block.setHardness(def.hardness);
        block.setAttachmentAllowed(def.attachmentAllowed);
        block.setReplacementAllowed(def.replacementAllowed);
        block.setSupportRequired(def.supportRequired);
        block.setPenetrable(def.penetrable);
        block.setTargetable(def.targetable);
        block.setClimbable(def.climbable);
        block.setInvisible(def.invisible);
        block.setTranslucent(def.translucent);
        block.setDoubleSided(def.doubleSided);
        block.setShadowCasting(def.shadowCasting);
        block.setWaving(def.waving);
        block.setLuminance(def.luminance);
        if (!def.displayName.isEmpty()) {
            block.setDisplayName(def.displayName);
        } else {
            block.setDisplayName(properCase(defaultName));
        }

        block.setMass(def.mass);
        block.setDebrisOnDestroy(def.debrisOnDestroy);

        if (def.entity != null) {
            block.setPrefab(def.entity.prefab);
            block.setKeepActive(def.entity.keepActive);
        }

        if (def.inventory != null) {
            block.setStackable(def.inventory.stackable);
            block.setDirectPickup(def.inventory.directPickup);
        }

        return block;
    }

    private Map<BlockPart, AssetUri> prepareTiles(BlockDefinition blockDef, AssetUri uri) {
        AssetUri tileUri = getDefaultTile(blockDef, uri);

        Map<BlockPart, AssetUri> tileUris = Maps.newEnumMap(BlockPart.class);
        for (BlockPart part : BlockPart.values()) {
            tileUris.put(part, tileUri);
        }

        if (blockDef.tiles != null) {
            for (BlockPart part : BlockPart.values()) {
                String partTile = blockDef.tiles.map.get(part);
                if (partTile != null) {
                    tileUri = new AssetUri(AssetType.BLOCK_TILE, blockDef.tiles.map.get(part));
                    tileUris.put(part, tileUri);
                }
            }
        }
        return tileUris;
    }

    private AssetUri getDefaultTile(BlockDefinition blockDef, AssetUri uri) {
        String defaultName = uri.toSimpleString();
        if (!blockDef.tile.isEmpty()) {
            defaultName = blockDef.tile;
        }
        return new AssetUri(AssetType.BLOCK_TILE, defaultName);
    }

    private JsonElement readJson(AssetUri blockDefUri) {
        InputStream stream = null;
        try {
            stream = AssetManager.assetStream(blockDefUri);
            if (stream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                return parser.parse(reader);
            } else {
                logger.error("Failed to load block definition '{}'", blockDefUri);
            }
        } catch (JsonParseException e) {
            logger.error("Failed to parse block definition '{}'", blockDefUri, e);
        } catch (IOException e) {
            logger.error("Failed to load block definition '{}'", blockDefUri, e);
        } finally {
            // JAVA7: Clean up closing
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    logger.error("Failed to close stream", e);
                }
            }
        }
        return null;
    }

    private BlockDefinition createBlockDefinition(JsonElement element) {
        return gson.fromJson(element, BlockDefinition.class);
    }

    private static class BlockTilesDefinitionHandler implements JsonDeserializer<BlockDefinition.Tiles> {

        @Override
        public BlockDefinition.Tiles deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonObject()) {
                BlockDefinition.Tiles result = new BlockDefinition.Tiles();
                deserializeBlockPartMap(result.map, json.getAsJsonObject(), String.class, context);
                return result;
            }
            return null;
        }
    }

    private static class BlockColorSourceDefinitionHandler implements JsonDeserializer<BlockDefinition.ColorSources> {

        @Override
        public BlockDefinition.ColorSources deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonObject()) {
                BlockDefinition.ColorSources result = new BlockDefinition.ColorSources();
                deserializeBlockPartMap(result.map, json.getAsJsonObject(), Block.ColorSource.class, context);
                return result;
            }
            return null;
        }
    }

    private static class BlockColorOffsetDefinitionHandler implements JsonDeserializer<BlockDefinition.ColorOffsets> {

        @Override
        public BlockDefinition.ColorOffsets deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonObject()) {
                BlockDefinition.ColorOffsets result = new BlockDefinition.ColorOffsets();
                deserializeBlockPartMap(result.map, json.getAsJsonObject(), Vector4f.class, context);
                return result;
            }
            return null;
        }
    }

    public static <T> void deserializeBlockPartMap(EnumMap<BlockPart, T> target, JsonObject jsonObj, Class<T> type, JsonDeserializationContext context) {
        if (jsonObj.has("all")) {
            T value = context.deserialize(jsonObj.get("all"), type);
            for (BlockPart part : BlockPart.values()) {
                target.put(part, value);
            }
        }
        if (jsonObj.has("sides")) {
            T value = context.deserialize(jsonObj.get("sides"), type);
            for (Side side : Side.horizontalSides()) {
                target.put(BlockPart.fromSide(side), value);
            }
        }
        if (jsonObj.has("topBottom")) {
            T value = context.deserialize(jsonObj.get("topBottom"), type);
            target.put(BlockPart.TOP, value);
            target.put(BlockPart.BOTTOM, value);
        }
        if (jsonObj.has("top")) {
            T value = context.deserialize(jsonObj.get("top"), type);
            target.put(BlockPart.TOP, value);
        }
        if (jsonObj.has("bottom")) {
            T value = context.deserialize(jsonObj.get("bottom"), type);
            target.put(BlockPart.BOTTOM, value);
        }
        if (jsonObj.has("front")) {
            T value = context.deserialize(jsonObj.get("front"), type);
            target.put(BlockPart.FRONT, value);
        }
        if (jsonObj.has("back")) {
            T value = context.deserialize(jsonObj.get("back"), type);
            target.put(BlockPart.BACK, value);
        }
        if (jsonObj.has("left")) {
            T value = context.deserialize(jsonObj.get("left"), type);
            target.put(BlockPart.LEFT, value);
        }
        if (jsonObj.has("right")) {
            T value = context.deserialize(jsonObj.get("right"), type);
            target.put(BlockPart.RIGHT, value);
        }
    }

    private String properCase(String s) {
        if (s.length() > 1) {
            return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
        } else {
            return s.toUpperCase();
        }
    }


    public static class LoadBlockDefinitionResults {
        public List<BlockFamily> families = Lists.newArrayList();
        public List<FreeformFamily> shapelessDefinitions = Lists.newArrayList();
    }
}
