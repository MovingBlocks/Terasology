/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.newdawn.slick.opengl.PNGDecoder;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.logic.manager.PathManager;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.rendering.assets.Material;
import org.terasology.rendering.assets.Texture;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.AlignToSurfaceFamily;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.family.HorizontalBlockFamily;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.shapes.BlockShape;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * @author Immortius
 */
public class BlockLoader {
    private static final int MAX_TILES = 256;
    public static final String AUTO_BLOCK_URL_FRAGMENT = "/auto/";

    // TODO: for now these are fixed (constant in the chunk shader)
    private int atlasSize = 256;
    private int tileSize = 16;

    private Logger logger = Logger.getLogger(getClass().getName());
    private JsonParser parser;
    private Gson gson;

    private BlockShape cubeShape;
    private BlockShape loweredShape;
    private BlockShape trimmedLoweredShape;

    private TObjectIntMap<AssetUri> tileIndexes = new TObjectIntHashMap<AssetUri>();
    private List<Tile> tiles = Lists.newArrayList();

    public BlockLoader() {
        parser = new JsonParser();
        gson = new GsonBuilder()
                .registerTypeAdapterFactory(new CaseInsensitiveEnumTypeAdapterFactory())
                .registerTypeAdapter(BlockDefinition.Tiles.class, new BlockTilesDefinitionHandler())
                .registerTypeAdapter(BlockDefinition.ColorSources.class, new BlockColorSourceDefinitionHandler())
                .registerTypeAdapter(BlockDefinition.ColorOffsets.class, new BlockColorOffsetDefinitionHandler())
                .registerTypeAdapter(Vector4f.class, new Vector4fHandler())
                .create();
        cubeShape = (BlockShape) AssetManager.load(new AssetUri(AssetType.SHAPE, "engine:cube"));
        loweredShape = (BlockShape) AssetManager.load(new AssetUri(AssetType.SHAPE, "engine:loweredCube"));
        trimmedLoweredShape = (BlockShape) AssetManager.load(new AssetUri(AssetType.SHAPE, "engine:trimmedLoweredCube"));
    }

    public int getAtlasSize() {
        return atlasSize;
    }

    public int getNumMipmaps() {
        return TeraMath.sizeOfPower(tileSize) + 1;
    }

    public LoadBlockDefinitionResults loadBlockDefinitions() {
        logger.log(Level.INFO, "Loading Blocks...");
        LoadBlockDefinitionResults result = new LoadBlockDefinitionResults();
        for (AssetUri blockDefUri : AssetManager.list(AssetType.BLOCK_DEFINITION)) {
            try {
                JsonElement rawJson = readJson(blockDefUri);
                if (rawJson != null) {
                    JsonObject blockDefJson = rawJson.getAsJsonObject();

                    // Don't process templates
                    if (blockDefJson.has("template") && blockDefJson.get("template").getAsBoolean()) {
                        continue;
                    }
                    logger.log(Level.INFO, "Loading " + blockDefUri);

                    BlockDefinition blockDef = loadBlockDefinition(inheritData(blockDefUri, blockDefJson));

                    if (isShapelessBlockFamily(blockDef)) {
                        indexTile(getDefaultTile(blockDef, blockDefUri), true);
                        result.shapelessDefinitions.add(new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName()));
                    } else {
                        if (blockDef.liquid) {
                            blockDef.rotation = BlockDefinition.RotationType.NONE;
                            blockDef.shapes.clear();
                            blockDef.shape = trimmedLoweredShape.getURI().getSimpleString();
                        }

                        if (blockDef.shapes.isEmpty()) {
                            switch (blockDef.rotation) {
                                case ALIGNTOSURFACE:
                                    result.families.add(processAlignToSurfaceFamily(blockDefUri, blockDefJson));
                                    break;
                                case HORIZONTAL:
                                    result.families.add(processHorizontalBlockFamily(blockDefUri, blockDef));
                                    break;

                                default:
                                    result.families.add(processSingleBlockFamily(blockDefUri, blockDef));
                                    break;
                            }
                        } else {
                            result.families.addAll(processMultiBlockFamily(blockDefUri, blockDef));
                        }
                    }

                }
            } catch (JsonParseException e) {
                logger.log(Level.SEVERE, "Failed to load block '" + blockDefUri + "'", e);
            } catch (NullPointerException e) {
                logger.log(Level.SEVERE, "Failed to load block '" + blockDefUri + "'", e);
            }
        }
        result.shapelessDefinitions.addAll(loadAutoBlocks());
        return result;
    }

    public BlockFamily loadWithShape(BlockUri uri) {
        BlockShape shape = cubeShape;
        if (uri.hasShape()) {
            AssetUri shapeUri = uri.getShapeUri();
            if (!shapeUri.isValid()) {
                return null;
            }
            shape = (BlockShape) AssetManager.load(shapeUri);
            if (shape == null) {
                return null;
            }
        }
        AssetUri blockDefUri = new AssetUri(AssetType.BLOCK_DEFINITION, uri.getPackage(), uri.getFamily());
        BlockDefinition def;
        if (AssetManager.getInstance().getAssetURLs(blockDefUri).isEmpty()) {
            // An auto-block
            def = new BlockDefinition();
        } else {
            def = loadBlockDefinition(inheritData(blockDefUri, readJson(blockDefUri).getAsJsonObject()));
        }

        def.shape = (shape.getURI().getSimpleString());
        if (shape.isCollisionSymmetric()) {
            Block block = constructSingleBlock(blockDefUri, def);
            return new SymmetricFamily(uri, block);
        } else {
            Map<Side, Block> blockMap = Maps.newEnumMap(Side.class);
            constructHorizontalBlocks(blockDefUri, def, blockMap);
            return new HorizontalBlockFamily(uri, blockMap);
        }
    }

    public void buildAtlas() {
        int numMipMaps = getNumMipmaps();
        ByteBuffer[] data = new ByteBuffer[numMipMaps];
        for (int i = 0; i < numMipMaps; ++i) {
            BufferedImage image = generateAtlas(i);
            if (i == 0) {
                try {
                    ImageIO.write(image, "png", new File(PathManager.getInstance().getScreensPath(), "tiles.png"));
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

            // TODO: Read data directly from image buffer into texture
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, "png", bos);
                PNGDecoder decoder = new PNGDecoder(new ByteArrayInputStream(bos.toByteArray()));
                ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
                decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.RGBA);
                buf.flip();
                data[i] = buf;
            } catch (IOException e) {
                logger.severe("Failed to write in-memory image");
            }
        }

        Texture terrainTex = new Texture(data, atlasSize, atlasSize, Texture.WrapMode.Clamp, Texture.FilterMode.Nearest);
        AssetManager.getInstance().addAssetTemporary(new AssetUri(AssetType.TEXTURE, "engine:terrain"), terrainTex);
        Material terrainMat = new Material(new AssetUri(AssetType.MATERIAL, "engine:terrain"), AssetManager.loadShader("engine:block"));
        terrainMat.setTexture("textureAtlas", terrainTex);
        terrainMat.setFloat3("colorOffset", 1, 1, 1);
        terrainMat.setInt("textured", 1);
        AssetManager.getInstance().addAssetTemporary(new AssetUri(AssetType.MATERIAL, "engine:terrain"), terrainMat);
    }

    private BufferedImage generateAtlas(int mipMapLevel) {
        int size = atlasSize / (1 << mipMapLevel);
        int textureSize = tileSize / (1 << mipMapLevel);
        int tilesPerDim = atlasSize / tileSize;

        BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics g = result.getGraphics();

        if (tiles.size() > MAX_TILES) {
            logger.severe("Too many tiles, culling overflow");
        }

        for (int index = 0; index < tiles.size() && index < MAX_TILES; ++index) {
            Tile tile = tiles.get(index);

            int posX = (index) % tilesPerDim;
            int posY = (index) / tilesPerDim;
            g.drawImage(tile.getImage().getScaledInstance(textureSize, textureSize, Image.SCALE_SMOOTH), posX * textureSize, posY * textureSize, null);
        }

        return result;
    }

    private List<BlockUri> loadAutoBlocks() {
        logger.log(Level.INFO, "Loading Auto Blocks...");
        List<BlockUri> result = Lists.newArrayList();
        for (AssetUri blockTileUri : AssetManager.list(AssetType.BLOCK_TILE)) {
            if (AssetManager.getInstance().getAssetURLs(blockTileUri).get(0).getPath().contains(AUTO_BLOCK_URL_FRAGMENT)) {
                BlockUri uri = new BlockUri(blockTileUri.getPackage(), blockTileUri.getAssetName());
                result.add(uri);
                getTileIndex(blockTileUri, true);
            }
        }
        return result;
    }

    private boolean isShapelessBlockFamily(BlockDefinition blockDef) {
        return blockDef.shapes.isEmpty() && blockDef.shape.isEmpty() && blockDef.rotation == BlockDefinition.RotationType.NONE && !blockDef.liquid && blockDef.tiles == null;
    }

    private JsonObject inheritData(AssetUri rootAssetUri, JsonObject blockDefJson) {

        JsonObject parentObj = blockDefJson;
        while (parentObj.has("basedOn")) {
            AssetUri parentUri = new AssetUri(AssetType.BLOCK_DEFINITION, parentObj.get("basedOn").getAsString());
            if (rootAssetUri.equals(parentUri)) {
                logger.severe("Circular inheritance detected in " + rootAssetUri);
                break;
            } else if (!parentUri.isValid()) {
                logger.severe(rootAssetUri + " based on invalid uri: " + parentObj.get("basedOn").getAsString());
                break;
            }
            JsonObject parent = readJson(parentUri).getAsJsonObject();
            mergeJsonInto(parent, blockDefJson);
            parentObj = parent;
        }
        return blockDefJson;
    }

    private List<BlockFamily> processMultiBlockFamily(AssetUri blockDefUri, BlockDefinition blockDef) {
        List<BlockFamily> result = Lists.newArrayList();
        for (String shapeString : blockDef.shapes) {
            AssetUri shapeUri = new AssetUri(AssetType.SHAPE, shapeString);
            BlockShape shape = (BlockShape) AssetManager.load(shapeUri);
            if (shape != null) {
                BlockUri familyUri;
                if (shape.equals(cubeShape)) {
                    familyUri = new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName());
                } else {
                    familyUri = new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName(), shapeUri.getPackage(), shapeUri.getAssetName());
                }
                blockDef.shape = shapeString;
                if (shape.isCollisionSymmetric()) {
                    Block block = constructSingleBlock(blockDefUri, blockDef);
                    result.add(new SymmetricFamily(familyUri, block));
                } else {
                    Map<Side, Block> blockMap = Maps.newEnumMap(Side.class);
                    constructHorizontalBlocks(blockDefUri, blockDef, blockMap);
                    result.add(new HorizontalBlockFamily(familyUri, blockMap));
                }
            }
        }
        return result;
    }

    private BlockFamily processAlignToSurfaceFamily(AssetUri blockDefUri, JsonObject blockDefJson) {
        Map<Side, Block> blockMap = Maps.newEnumMap(Side.class);
        if (blockDefJson.has("top")) {
            JsonObject topDefJson = blockDefJson.getAsJsonObject("top");
            blockDefJson.remove("top");
            mergeJsonInto(blockDefJson, topDefJson);
            BlockDefinition topDef = loadBlockDefinition(topDefJson);
            blockMap.put(Side.TOP, constructSingleBlock(blockDefUri, topDef));
        }
        if (blockDefJson.has("sides")) {
            JsonObject sideDefJson = blockDefJson.getAsJsonObject("sides");
            blockDefJson.remove("sides");
            mergeJsonInto(blockDefJson, sideDefJson);
            BlockDefinition sideDef = loadBlockDefinition(sideDefJson);
            constructHorizontalBlocks(blockDefUri, sideDef, blockMap);
        }
        if (blockDefJson.has("bottom")) {
            JsonObject bottomDefJson = blockDefJson.getAsJsonObject("bottom");
            blockDefJson.remove("bottom");
            mergeJsonInto(blockDefJson, bottomDefJson);
            BlockDefinition bottomDef = loadBlockDefinition(bottomDefJson);
            blockMap.put(Side.BOTTOM, constructSingleBlock(blockDefUri, bottomDef));
        }
        return new AlignToSurfaceFamily(new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName()), blockMap);
    }

    private void mergeJsonInto(JsonObject from, JsonObject to) {
        for (Map.Entry<String, JsonElement> entry : from.entrySet()) {
            if (entry.getValue().isJsonObject()) {
               if (!to.has(entry.getKey())) {
                   to.add(entry.getKey(), entry.getValue());
               }
            } else {
                if (!to.has(entry.getKey())) {
                    to.add(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private BlockFamily processSingleBlockFamily(AssetUri blockDefUri, BlockDefinition blockDef) {
        Block block = constructSingleBlock(blockDefUri, blockDef);

        return new SymmetricFamily(new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName()), block);
    }

    private Block constructSingleBlock(AssetUri blockDefUri, BlockDefinition blockDef) {
        Map<BlockPart, AssetUri> tileUris = prepareTiles(blockDef, blockDefUri);
        Map<BlockPart, Block.ColorSource> colorSourceMap = prepareColorSources(blockDef);
        Map<BlockPart, Vector4f> colorOffsetsMap = prepareColorOffsets(blockDef);
        BlockShape shape = getShape(blockDef);

        Block block = createRawBlock(blockDef, properCase(blockDefUri.getAssetName()));
        applyShape(block, shape, tileUris, Rotation.NONE);

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

    private BlockFamily processHorizontalBlockFamily(AssetUri blockDefUri, BlockDefinition blockDef) {
        Map<Side, Block> blockMap = Maps.newEnumMap(Side.class);
        constructHorizontalBlocks(blockDefUri, blockDef, blockMap);

        return new HorizontalBlockFamily(new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName()), blockMap);
    }

    private void constructHorizontalBlocks(AssetUri blockDefUri, BlockDefinition blockDef, Map<Side, Block> blockMap) {
        Map<BlockPart, AssetUri> tileUris = prepareTiles(blockDef, blockDefUri);
        Map<BlockPart, Block.ColorSource> colorSourceMap = prepareColorSources(blockDef);
        Map<BlockPart, Vector4f> colorOffsetsMap = prepareColorOffsets(blockDef);
        BlockShape shape = getShape(blockDef);

        for (Rotation rot : Rotation.horizontalRotations()) {
            Block block = createRawBlock(blockDef, properCase(blockDefUri.getAssetName()));
            applyShape(block, shape, tileUris, rot);

            for (BlockPart part : BlockPart.values()) {
                block.setColorSource(part, colorSourceMap.get(part));
                block.setColorOffset(part, colorOffsetsMap.get(part));
            }

            blockMap.put(rot.rotate(Side.FRONT), block);
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
            shape = (BlockShape) AssetManager.load(new AssetUri(AssetType.SHAPE, blockDef.shape));
        }
        if (shape == null) {
            return cubeShape;
        }
        return shape;
    }

    private void applyShape(Block block, BlockShape shape, Map<BlockPart, AssetUri> tileUris, Rotation rot) {
        for (BlockPart part : BlockPart.values()) {
            // TODO: Need to be more sensible with the texture atlas. Because things like block particles read from a part that may not exist, we're being fairly lenient
            int tileIndex = getTileIndex(tileUris.get(part), shape.getMeshPart(part) != null);
            Vector2f atlasPos = calcAtlasPositionForId(tileIndex);
            BlockPart targetPart = rot.rotate(part);
            block.setTextureAtlasPos(targetPart, atlasPos);
            if (shape.getMeshPart(part) != null) {
                block.setMeshPart(targetPart, shape.getMeshPart(part).rotate(rot.getQuat4f()).mapTexCoords(atlasPos, Block.TEXTURE_OFFSET_WIDTH));
                if (part.isSide()) {
                    block.setFullSide(targetPart.getSide(), shape.isBlockingSide(part.getSide()));
                }
            }
        }
        block.setCollision(shape.getCollisionOffset(rot), shape.getCollisionShape(rot));
    }

    private void applyLoweredShape(Block block, BlockShape shape, Map<BlockPart, AssetUri> tileUris) {
        for (Side side : Side.values()) {
            BlockPart part = BlockPart.fromSide(side);
            block.setLoweredLiquidMesh(part.getSide(), shape.getMeshPart(part).rotate(Rotation.NONE.getQuat4f()).mapTexCoords(calcAtlasPositionForId(getTileIndex(tileUris.get(part), true)), Block.TEXTURE_OFFSET_WIDTH));
        }
    }

    private Vector2f calcAtlasPositionForId(int id) {
        int tilesPerDim = atlasSize / tileSize;
        return new Vector2f((id % tilesPerDim) * Block.TEXTURE_OFFSET, (id / tilesPerDim) * Block.TEXTURE_OFFSET);
    }

    private Block createRawBlock(BlockDefinition def, String defaultName) {
        Block block = new Block();
        block.setLiquid(def.liquid);
        block.setHardness(def.hardness);
        block.setAttachmentAllowed(def.attachmentAllowed);
        block.setReplacementAllowed(def.replacementAllowed);
        block.setSupportRequired(def.supportRequired);
        block.setPenetrable(def.penetrable);
        block.setTargetable(def.targetable);
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
            block.setEntityPrefab(def.entity.prefab);
            block.setEntityTemporary(def.entity.temporary);
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
        String defaultName = uri.getSimpleString();
        if (!blockDef.tile.isEmpty()) {
            defaultName = blockDef.tile;
        }
        return new AssetUri(AssetType.BLOCK_TILE, defaultName);
    }

    private int getTileIndex(AssetUri uri, boolean warnOnError) {
        if (tileIndexes.containsKey(uri)) {
            return tileIndexes.get(uri);
        }
        return indexTile(uri, warnOnError);
    }

    private int indexTile(AssetUri uri, boolean warnOnError) {
        Tile tile = (Tile) AssetManager.tryLoad(uri);
        if (tile != null) {
            int index = tiles.size();
            tiles.add(tile);
            tileIndexes.put(uri, index);
            return index;
        } else if (warnOnError) {
            logger.warning("Unable to resolve block tile '" + uri + "'");
        }
        return 0;
    }

    private JsonElement readJson(AssetUri blockDefUri) {
        InputStream stream = null;
        try {
            stream = AssetManager.assetStream(blockDefUri);
            if (stream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                return parser.parse(reader);
            } else {
                logger.severe("Failed to load block definition '" + blockDefUri + "'");
            }
        } catch (JsonParseException e) {
            logger.log(Level.WARNING, "Failed to parse block definition '" + blockDefUri + "'", e);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load block definition '" + blockDefUri + "'", e);
        } finally {
            // JAVA7: Clean up closing
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Failed to close stream", e);
                }
            }
        }
        return null;
    }

    private BlockDefinition loadBlockDefinition(JsonElement element) {
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

    private static class Vector4fHandler implements JsonDeserializer<Vector4f> {

        @Override
        public Vector4f deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonArray()) {
                JsonArray array = json.getAsJsonArray();
                if (array.size() == 4) {
                    return new Vector4f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat(), array.get(3).getAsFloat());
                } else if (array.size() == 3) {
                    return new Vector4f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat(), 1);
                }
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

    public static class CaseInsensitiveEnumTypeAdapterFactory implements TypeAdapterFactory {
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            Class<T> rawType = (Class<T>) type.getRawType();
            if (!rawType.isEnum()) {
                return null;
            }

            final Map<String, T> lowercaseToConstant = Maps.newHashMap();
            for (T constant : rawType.getEnumConstants()) {
                lowercaseToConstant.put(toLowercase(constant), constant);
            }

            return new TypeAdapter<T>() {
                @Override
                public void write(JsonWriter out, T value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                    } else {
                        out.value(toLowercase(value));
                    }
                }

                @Override
                public T read(JsonReader reader) throws IOException {
                    if (reader.peek() == JsonToken.NULL) {
                        reader.nextNull();
                        return null;
                    } else {
                        return lowercaseToConstant.get(toLowercase(reader.nextString()));
                    }
                }
            };
        }

        private String toLowercase(Object o) {
            return o.toString().toLowerCase(Locale.ENGLISH);
        }
    }

    public static class LoadBlockDefinitionResults {
        public List<BlockFamily> families = Lists.newArrayList();
        public List<BlockUri> shapelessDefinitions = Lists.newArrayList();
    }

}
