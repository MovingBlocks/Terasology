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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.newdawn.slick.opengl.PNGDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.game.CoreRegistry;
import org.terasology.game.paths.PathManager;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.rendering.assets.Material;
import org.terasology.rendering.assets.Texture;
import org.terasology.utilities.gson.Vector3fHandler;
import org.terasology.utilities.gson.Vector4fHandler;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.*;
import org.terasology.world.block.shapes.BlockShape;

import javax.imageio.ImageIO;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.awt.*;
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
import java.util.*;
import java.util.List;

/**
 * @author Immortius
 */
public class BlockLoader {
    public static final String AUTO_BLOCK_URL_FRAGMENT = "/auto/";

    private static final Logger logger = LoggerFactory.getLogger(BlockLoader.class);

    private JsonParser parser;
    private Gson gson;

    private BlockShape cubeShape;
    private BlockShape loweredShape;
    private BlockShape trimmedLoweredShape;

    private Tile tiles[] = new Tile[1024];
    private Tile tilesNormal[] = new Tile[1024];
    private Tile tilesHeight[] = new Tile[1024];
    private int currentMaxTileIndex = 0;

    private TObjectIntMap<AssetUri> tileIndices = new TObjectIntHashMap<AssetUri>();

    public BlockLoader() {
        parser = new JsonParser();
        gson = new GsonBuilder()
                .registerTypeAdapterFactory(new CaseInsensitiveEnumTypeAdapterFactory())
                .registerTypeAdapter(BlockDefinition.Tiles.class, new BlockTilesDefinitionHandler())
                .registerTypeAdapter(BlockDefinition.ColorSources.class, new BlockColorSourceDefinitionHandler())
                .registerTypeAdapter(BlockDefinition.ColorOffsets.class, new BlockColorOffsetDefinitionHandler())
                .registerTypeAdapter(Vector3f.class, new Vector3fHandler())
                .registerTypeAdapter(Vector4f.class, new Vector4fHandler())
                .create();
        cubeShape = (BlockShape) Assets.get(new AssetUri(AssetType.SHAPE, "engine:cube"));
        loweredShape = (BlockShape) Assets.get(new AssetUri(AssetType.SHAPE, "engine:loweredCube"));
        trimmedLoweredShape = (BlockShape) Assets.get(new AssetUri(AssetType.SHAPE, "engine:trimmedLoweredCube"));
    }

    public <T> T fromJson(JsonElement element, Class<T> type) {
        return gson.fromJson(element, type);
    }

    public int getNumMipmaps() {
        return TeraMath.sizeOfPower(Block.TILE_SIZE) + 1;
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

                    BlockDefinition blockDef = loadBlockDefinition(inheritData(blockDefUri, blockDefJson));

                    if (isShapelessBlockFamily(blockDef)) {
                        int index = indexTile(getDefaultTile(blockDef, blockDefUri), true);

                        Tile tileNormal = (Tile) AssetManager.tryLoad(getDefaultTileNormal(blockDef, blockDefUri));
                        tilesNormal[index] = tileNormal;
                        Tile tileHeight = (Tile) AssetManager.tryLoad(getDefaultTileHeight(blockDef, blockDefUri));
                        tilesHeight[index] = tileHeight;

                        result.shapelessDefinitions.add(new ShapelessFamily(new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName()), getCategories(blockDef)));
                    } else {
                        if (blockDef.liquid) {
                            blockDef.rotation = null;
                            blockDef.shapes.clear();
                            blockDef.shape = trimmedLoweredShape.getURI().getSimpleString();
                        }

                        BlockFamilyFactoryRegistry blockFamilyFactoryRegistry = CoreRegistry.get(BlockFamilyFactoryRegistry.class);

                        if (blockDef.shapes.isEmpty()) {
                            final BlockFamilyFactory blockFamilyFactory = blockFamilyFactoryRegistry.getBlockFamilyFactory(blockDef.rotation);
                            result.families.add(blockFamilyFactory.createBlockFamily(this, blockDefUri, blockDef, blockDefJson));
                        } else {
                            result.families.addAll(processMultiBlockFamily(blockDefUri, blockDef));
                        }
                    }

                }
            } catch (JsonParseException e) {
                logger.error("Failed to load block '{}'", blockDefUri, e);
            } catch (NullPointerException e) {
                logger.error("Failed to load block '{}'", blockDefUri, e);
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
            shape = (BlockShape) Assets.get(shapeUri);
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
            return new SymmetricFamily(uri, block, getCategories(def));
        } else {
            Map<Side, Block> blockMap = Maps.newEnumMap(Side.class);
            constructHorizontalBlocks(blockDefUri, def, blockMap);
            return new HorizontalBlockFamily(uri, blockMap, getCategories(def));
        }
    }

    public void buildAtlas() {
        // Update the atlas configuration using the given set of tiles
        for (int index = 0; index < currentMaxTileIndex; ++index) {
            if (tiles[index] != null) {
                Tile tile = tiles[index];
                updateAtlasConfiguration(tile);
            }
        }

        int numMipMaps = getNumMipmaps();
        ByteBuffer[] data = new ByteBuffer[numMipMaps];
        ByteBuffer[] dataNormal = new ByteBuffer[numMipMaps];
        ByteBuffer[] dataHeight = new ByteBuffer[numMipMaps];

        final Color unitZColor = new Color(0.5f, 0.5f, 1.0f, 1.0f);
        final Color transparentColor = new Color(0.0f, 0.0f, 0.0f, 0.0f);
        final Color blackColor = new Color(0.0f, 0.0f, 0.0f, 1.0f);

        for (int i = 0; i < numMipMaps; ++i) {
            BufferedImage imageDiffuse = generateAtlas(i, tiles, transparentColor);
            BufferedImage imageNormal = generateAtlas(i, tilesNormal, unitZColor);
            BufferedImage imageHeight = generateAtlas(i, tilesHeight, blackColor);

            if (i == 0) {
                try {
                    ImageIO.write(imageDiffuse, "png", new File(PathManager.getInstance().getScreenshotPath(), "tiles.png"));
                    ImageIO.write(imageNormal, "png", new File(PathManager.getInstance().getScreenshotPath(), "tiles_normal.png"));
                    ImageIO.write(imageHeight, "png", new File(PathManager.getInstance().getScreenshotPath(), "tiles_height.png"));
                } catch (IOException e) {
                    logger.warn("Failed to write atlas");
                }
            }

            writeImageToBuffer(imageDiffuse, i, data);
            writeImageToBuffer(imageNormal, i, dataNormal);
            writeImageToBuffer(imageHeight, i, dataHeight);
        }

        Texture terrainTex = new Texture(data, Block.ATLAS_SIZE, Block.ATLAS_SIZE, Texture.WrapMode.Clamp, Texture.FilterMode.Nearest);
        AssetManager.getInstance().addAssetTemporary(new AssetUri(AssetType.TEXTURE, "engine:terrain"), terrainTex);
        Texture terrainNormalTex = new Texture(dataNormal, Block.ATLAS_SIZE, Block.ATLAS_SIZE, Texture.WrapMode.Clamp, Texture.FilterMode.Nearest);
        AssetManager.getInstance().addAssetTemporary(new AssetUri(AssetType.TEXTURE, "engine:terrainNormal"), terrainNormalTex);
        Texture terrainHeightTex = new Texture(dataHeight, Block.ATLAS_SIZE, Block.ATLAS_SIZE, Texture.WrapMode.Clamp, Texture.FilterMode.Nearest);
        AssetManager.getInstance().addAssetTemporary(new AssetUri(AssetType.TEXTURE, "engine:terrainHeight"), terrainHeightTex);

        Material terrainMat = new Material(new AssetUri(AssetType.MATERIAL, "engine:terrain"), Assets.getShader("engine:blockMaterial"));
        terrainMat.setTexture("textureAtlas", terrainTex);
        terrainMat.setFloat3("colorOffset", 1, 1, 1);
        terrainMat.setInt("textured", 1);
        AssetManager.getInstance().addAssetTemporary(new AssetUri(AssetType.MATERIAL, "engine:terrain"), terrainMat);
    }

    private void writeImageToBuffer(BufferedImage image , int mipMapIndex, ByteBuffer[] ouputData) {
        // TODO: Read data directly from image buffer into texture
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", bos);
            PNGDecoder decoder = new PNGDecoder(new ByteArrayInputStream(bos.toByteArray()));
            ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
            decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
            buf.flip();
            ouputData[mipMapIndex] = buf;
        } catch (IOException e) {
            logger.error("Failed to create atlas texture");
        }
    }

    private BufferedImage generateAtlas(int mipMapLevel, Tile[] tiles, Color clearColor) {
        int size = Block.ATLAS_SIZE / (1 << mipMapLevel);
        int textureSize = Block.TILE_SIZE / (1 << mipMapLevel);
        int tilesPerDim = Block.ATLAS_SIZE / Block.TILE_SIZE;

        BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics g = result.getGraphics();

        g.setColor(clearColor);
        g.fillRect(0, 0, size, size);

        for (int index = 0; index < currentMaxTileIndex; ++index) {

            int posX = (index) % tilesPerDim;
            int posY = (index) / tilesPerDim;

            Tile tile = tiles[index];
            if (tile != null) {
                g.drawImage(tile.getImage().getScaledInstance(textureSize, textureSize, Image.SCALE_SMOOTH), posX * textureSize, posY * textureSize, null);
            }
        }

        return result;
    }

    private List<ShapelessFamily> loadAutoBlocks() {
        logger.debug("Loading Auto Blocks...");
        List<ShapelessFamily> result = Lists.newArrayList();
        for (AssetUri blockTileUri : Assets.list(AssetType.BLOCK_TILE)) {
            if (AssetManager.getInstance().getAssetURLs(blockTileUri).get(0).getPath().contains(AUTO_BLOCK_URL_FRAGMENT)) {
                logger.debug("Loading auto block {}", blockTileUri);
                BlockUri uri = new BlockUri(blockTileUri.getPackage(), blockTileUri.getAssetName());
                result.add(new ShapelessFamily(uri));
                getTileIndex(blockTileUri, true);
            }
        }
        return result;
    }

    private boolean isShapelessBlockFamily(BlockDefinition blockDef) {
        return blockDef.shapes.isEmpty() && blockDef.shape.isEmpty() && blockDef.rotation==null && !blockDef.liquid && blockDef.tiles == null;
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
            mergeJsonInto(parent, blockDefJson);
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
                    familyUri = new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName());
                } else {
                    familyUri = new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName(), shapeUri.getPackage(), shapeUri.getAssetName());
                }
                blockDef.shape = shapeString;
                if (shape.isCollisionSymmetric()) {
                    Block block = constructSingleBlock(blockDefUri, blockDef);
                    result.add(new SymmetricFamily(familyUri, block, getCategories(blockDef)));
                } else {
                    Map<Side, Block> blockMap = Maps.newEnumMap(Side.class);
                    constructHorizontalBlocks(blockDefUri, blockDef, blockMap);
                    result.add(new HorizontalBlockFamily(familyUri, blockMap, getCategories(blockDef)));
                }
            }
        }
        return result;
    }

    public void mergeJsonInto(JsonObject from, JsonObject to) {
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

    public Block constructSingleBlock(AssetUri blockDefUri, BlockDefinition blockDef) {
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

    public void constructHorizontalBlocks(AssetUri blockDefUri, BlockDefinition blockDef, Map<Side, Block> blockMap) {
        Map<BlockPart, AssetUri> tileUris = prepareTiles(blockDef, blockDefUri);
        Map<BlockPart, Block.ColorSource> colorSourceMap = prepareColorSources(blockDef);
        Map<BlockPart, Vector4f> colorOffsetsMap = prepareColorOffsets(blockDef);
        BlockShape shape = getShape(blockDef);

        for (Rotation rot : Rotation.horizontalRotations()) {
            Block block = createRawBlock(blockDef, properCase(blockDefUri.getAssetName()));

            block.setDirection(rot.rotate(Side.FRONT));

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
            shape = (BlockShape) Assets.get(new AssetUri(AssetType.SHAPE, blockDef.shape));
        }
        if (shape == null) {
            return cubeShape;
        }
        return shape;
    }

    private void applyShape(Block block, BlockShape shape, Map<BlockPart, AssetUri> tileUris, Rotation rot) {
        BlockLoader loader = CoreRegistry.get(BlockLoader.class);

        for (BlockPart part : BlockPart.values()) {
            // TODO: Need to be more sensible with the texture atlas. Because things like block particles read from a part that may not exist, we're being fairly lenient
            int tileIndex = getTileIndex(tileUris.get(part), shape.getMeshPart(part) != null);
            Vector2f atlasPos = calcAtlasPositionForId(tileIndex);
            BlockPart targetPart = rot.rotate(part);
            block.setTextureAtlasPos(targetPart, atlasPos);
            if (shape.getMeshPart(part) != null) {
                block.setMeshPart(targetPart, shape.getMeshPart(part).rotate(rot.getQuat4f()).mapTexCoords(atlasPos, Block.calcRelativeTileSizeWithOffset()));
                if (part.isSide()) {
                    block.setFullSide(targetPart.getSide(), shape.isBlockingSide(part.getSide()));
                }
            }
        }
        block.setCollision(shape.getCollisionOffset(rot), shape.getCollisionShape(rot));
    }

    private void applyLoweredShape(Block block, BlockShape shape, Map<BlockPart, AssetUri> tileUris) {
        BlockLoader loader = CoreRegistry.get(BlockLoader.class);

        for (Side side : Side.values()) {
            BlockPart part = BlockPart.fromSide(side);
            block.setLoweredLiquidMesh(part.getSide(), shape.getMeshPart(part).rotate(Rotation.NONE.getQuat4f()).mapTexCoords(calcAtlasPositionForId(getTileIndex(tileUris.get(part), true)), Block.calcRelativeTileSizeWithOffset()));
        }
    }

    private Vector2f calcAtlasPositionForId(int id) {
        int tilesPerDim = Block.ATLAS_SIZE / Block.TILE_SIZE;
        return new Vector2f((id % tilesPerDim) * Block.calcRelativeTileSize(), (id / tilesPerDim) * Block.calcRelativeTileSize());
    }

    private Block createRawBlock(BlockDefinition def, String defaultName) {
        Block block = new Block();
        block.setLiquid(def.liquid);
        block.setClimbable(def.climbable);
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
        block.setTint(def.tint);
        block.setCraftPlace(def.craftPlace);
        block.setConnectToAllBlocks(def.connectToAllBlock);
        block.setCheckHeightDiff(def.checkHeightDiff);
        if (!def.displayName.isEmpty()) {
            block.setDisplayName(def.displayName);
        } else {
            block.setDisplayName(properCase(defaultName));
        }

        block.setAcceptedToConnectBlocks(def.acceptedToConnectBlocks);

        block.setMass(def.mass);
        block.setDebrisOnDestroy(def.debrisOnDestroy);

        if (def.entity != null) {
            block.setEntityPrefab(def.entity.prefab);
            block.setEntityMode(def.entity.mode);
        }

        if (def.inventory != null) {
            block.setStackable(def.inventory.stackable);
            block.setDirectPickup(def.inventory.directPickup);
            if (!def.inventory.pickupFamily.isEmpty()) {
                BlockUri uri = new BlockUri(def.inventory.pickupFamily);
                if (uri.isValid()) {
                    block.setPickupBlockFamily(uri);
                }
            }
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

    public String[] getCategories(BlockDefinition def) {
        return def.categories.toArray(new String[def.categories.size()]);
    }

    private AssetUri getDefaultTile(BlockDefinition blockDef, AssetUri uri) {
        String defaultName = uri.getSimpleString();
        if (!blockDef.tile.isEmpty()) {
            defaultName = blockDef.tile;
        }
        return new AssetUri(AssetType.BLOCK_TILE, defaultName);
    }

    private AssetUri getDefaultTileNormal(BlockDefinition blockDef, AssetUri uri) {
        String defaultName = uri.getSimpleString()+"Normal";
        if (!blockDef.tileNormal.isEmpty()) {
            defaultName = blockDef.tileNormal;
        }
        return new AssetUri(AssetType.BLOCK_TILE, defaultName);
    }

    private AssetUri getDefaultTileHeight(BlockDefinition blockDef, AssetUri uri) {
        String defaultName = uri.getSimpleString()+"Height";
        if (!blockDef.tileHeight.isEmpty()) {
            defaultName = blockDef.tileHeight;
        }
        return new AssetUri(AssetType.BLOCK_TILE, defaultName);
    }

    private int getTileIndex(AssetUri uri, boolean warnOnError) {
        if (tileIndices.containsKey(uri)) {
            return tileIndices.get(uri);
        }
        return indexTile(uri, warnOnError);
    }

    private int indexTile(AssetUri uri, boolean warnOnError) {
        Tile tile = (Tile) AssetManager.tryLoad(uri);
        if (tile != null) {
            int index = currentMaxTileIndex++;
            tiles[index] = tile;
            tileIndices.put(uri, index);
            return index;
        } else if (warnOnError) {
            logger.warn("Unable to resolve block tile '{}'", uri);
        }
        return -1;
    }

    private void updateAtlasConfiguration(Tile currentTile) {
        // The atlas is configured using the following constraints...
        // 1.   The overall tile size is the size of the largest tile loaded
        // 2.   The atlas will never be larger than 4096*4096 px
        // 3.   The tile size gets adjusted if the tiles won't fit into the atlas using the overall tile size
        //      (the tile size gets halved until all tiles will fit into the atlas)
        // 4.   The size of the atlas is always a power of two - as is the tile size
        int tileSize = Block.TILE_SIZE;
        int atlasSize = Block.ATLAS_SIZE;

        if (currentTile.getImage().getWidth() > tileSize) {
            tileSize = currentTile.getImage().getWidth();
        }

        int atlasSizePow = 0, count = 0;
        while (atlasSizePow * atlasSizePow < currentMaxTileIndex) {
            atlasSizePow = (1 << count);
            count++;
        }

        atlasSize = atlasSizePow * tileSize;

        final int maxTextureAtlasRes = CoreRegistry.get(Config.class).getRendering().getMaxTextureAtlasResolution();
        if (atlasSize > maxTextureAtlasRes) {
            atlasSize = maxTextureAtlasRes;
        }

        int maxTiles = (atlasSize / tileSize) * (atlasSize / tileSize);
        while (maxTiles < currentMaxTileIndex) {
            tileSize >>= 1;
            maxTiles = (atlasSize / tileSize) * (atlasSize / tileSize);
        }

        Block.ATLAS_SIZE = atlasSize;
        Block.TILE_SIZE = tileSize;
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

    public BlockDefinition loadBlockDefinition(JsonElement element) {
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

    public static class CaseInsensitiveEnumTypeAdapterFactory implements TypeAdapterFactory {
        @Override
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
        public List<ShapelessFamily> shapelessDefinitions = Lists.newArrayList();
    }

    public static class ShapelessFamily {
        public BlockUri uri;
        public String[] categories = new String[0];

        public ShapelessFamily(BlockUri uri) {
            this.uri = uri;
        }

        public ShapelessFamily(BlockUri uri, String[] categories) {
            this(uri);
            this.categories = categories;
        }

    }

}
