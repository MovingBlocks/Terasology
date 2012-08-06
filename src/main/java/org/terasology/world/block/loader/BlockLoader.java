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
import com.google.common.collect.Sets;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import gnu.trove.map.TObjectByteMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectByteHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.newdawn.slick.opengl.PNGDecoder;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.rendering.assets.Material;
import org.terasology.rendering.assets.Texture;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.block.shapes.BlockShape;

import javax.imageio.ImageIO;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Immortius
 */
public class BlockLoader {

    private static final int MAX_BLOCKS = 256;
    private static final int MAX_TILES = 256;
    private static final int NUM_MIPMAPS = 5;

    private Logger logger = Logger.getLogger(getClass().getName());
    private Gson gson;

    private BlockShape cubeShape;
    private BlockShape loweredShape;

    private TObjectIntMap<AssetUri> tileIndexes = new TObjectIntHashMap<AssetUri>();
    private List<Tile> tiles = Lists.newArrayList();

    private int nextId = 1;
    private TObjectByteMap<BlockUri> blockIds = new TObjectByteHashMap<BlockUri>();
    private Set<BlockUri> unresolvedBlocks = Sets.newHashSet();

    private List<BlockFamily> blockFamilies = Lists.newArrayList();

    public BlockLoader() {
        gson = new GsonBuilder()
                .registerTypeAdapterFactory(new CaseInsensitiveEnumTypeAdapterFactory())
                .registerTypeAdapter(BlockDefinition.Tiles.class, new BlockTilesDefinitionHandler())
                .registerTypeAdapter(BlockDefinition.ColorSources.class, new BlockColorSourceDefinitionHandler())
                .registerTypeAdapter(BlockDefinition.ColorOffsets.class, new BlockColorOffsetDefinitionHandler())
                .registerTypeAdapter(Vector4f.class, new Vector4fHandler())
                .create();
        cubeShape = (BlockShape) AssetManager.load(new AssetUri(AssetType.SHAPE, "engine:cube"));
        loweredShape = (BlockShape) AssetManager.load(new AssetUri(AssetType.SHAPE, "engine:loweredCube"));
    }

    public void load() {
        loadBlocks();
        registerBlocks();
    }

    public void buildAtlas() {
        ByteBuffer[] data = new ByteBuffer[NUM_MIPMAPS];
        for (int i = 0; i < NUM_MIPMAPS; ++i) {
            BufferedImage image = generateAtlas(i);
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

        Texture terrainTex = new Texture(data, Block.ATLAS_SIZE_IN_PX, Block.ATLAS_SIZE_IN_PX, Texture.WrapMode.Clamp, Texture.FilterMode.Nearest);
        AssetManager.getInstance().addAssetTemporary(new AssetUri(AssetType.TEXTURE, "engine:terrain"), terrainTex);
        Material terrainMat = new Material(new AssetUri(AssetType.MATERIAL, "engine:terrain"), AssetManager.loadShader("engine:block"));
        terrainMat.setTexture("textureAtlas", terrainTex);
        terrainMat.setFloat3("colorOffset", 1, 1, 1);
        terrainMat.setInt("textured", 1);
        AssetManager.getInstance().addAssetTemporary(new AssetUri(AssetType.MATERIAL, "engine:terrain"), terrainMat);
    }

    private BufferedImage generateAtlas(int mipMapLevel) {
        int size = Block.ATLAS_SIZE_IN_PX / (1 << mipMapLevel);
        int textureSize = Block.TEXTURE_SIZE_IN_PX / (1 << mipMapLevel);

        BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics g = result.getGraphics();

        if (tiles.size() > MAX_TILES) {
            logger.severe("Too many tiles, culling overflow");
        }

        g.setColor(new Color(0,0,0));
        g.drawRect(0,0,textureSize,textureSize);

        for (int index = 0; index < tiles.size() && index < MAX_TILES; ++index) {
            Tile tile = tiles.get(index);

            int posX = (index) % Block.ATLAS_ELEMENTS_PER_ROW_AND_COLUMN;
            int posY = (index) / Block.ATLAS_ELEMENTS_PER_ROW_AND_COLUMN;
            g.drawImage(tile.getImage().getScaledInstance(textureSize, textureSize, Image.SCALE_SMOOTH), posX * textureSize, posY * textureSize, null);
        }

        return result;
    }

    private void registerBlocks() {
        BlockManager.getInstance().addAllBlockFamilies(blockFamilies);
    }

    private void loadBlocks() {
        logger.log(Level.INFO, "Loading Blocks...");
        for (AssetUri blockDefUri : AssetManager.list(AssetType.BLOCK_DEFINITION)) {
            logger.log(Level.INFO, "Loading " + blockDefUri);
            BlockDefinition blockDef = loadBlockDefinition(blockDefUri);
            if (blockDef != null) {
                // TODO: Different rotation strategies
                // TODO: Multifamily definitions
                Block block = createRawBlock(blockDef, properCase(blockDefUri.getAssetName()));
                Map<BlockPart, Integer> tileIndices = prepareTiles(blockDef, blockDefUri.getSimpleString());
                BlockShape shape = null;
                if (!blockDef.shape.isEmpty()) {
                    shape = (BlockShape) AssetManager.load(new AssetUri(AssetType.SHAPE, blockDef.shape));
                }
                if (shape == null) {
                    shape = cubeShape;
                }

                applyShape(block, shape, tileIndices);
                Map<BlockPart, Block.ColorSource> colorSourceMap = prepareColorSources(blockDef);
                Map<BlockPart, Vector4f> colorOffsetsMap = prepareColorOffsets(blockDef);
                for (BlockPart part : BlockPart.values()) {
                    block.setColorSource(part, colorSourceMap.get(part));
                    block.setColorOffset(part, colorOffsetsMap.get(part));
                }

                // Lowered mesh for liquids
                if (block.isLiquid()) {
                    applyLoweredShape(block, loweredShape, tileIndices);
                }

                BlockFamily family = new SymmetricFamily(new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName()), block);
                registerFamily(family);
            }
        }
    }

    private void registerFamily(BlockFamily family) {
        blockFamilies.add(family);
        for (Block block : family.listBlocks()) {
            if (blockIds.containsKey(block.getURI())) {
                block.setId(blockIds.get(block.getURI()));
                unresolvedBlocks.remove(block.getURI());
            } else {
                if (nextId == MAX_BLOCKS) {
                    logger.severe("Out of block ids, too many blocks");
                } else {
                    byte id = (byte)(nextId++);
                    block.setId(id);
                    blockIds.put(block.getURI(), id);
                }
            }
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

    // TODO: Rotation
    private void applyShape(Block block, BlockShape shape, Map<BlockPart, Integer> tileIndices) {
        for (BlockPart part : BlockPart.values()) {
            if (shape.getMeshPart(part) != null) {
                Vector2f atlasPos = calcAtlasPositionForId(tileIndices.get(part));
                block.setMeshPart(part, shape.getMeshPart(part).rotate(Rotation.NONE.getQuat4f()).mapTexCoords(atlasPos, Block.TEXTURE_OFFSET_WIDTH));
                block.setTextureAtlasPos(part, atlasPos);
                if (part.isSide()) {
                    block.setFullSide(part.getSide(), shape.isBlockingSide(part.getSide()));
                }
            }
        }
        block.setCollision(shape.getCollisionOffset(), shape.getCollisionShape());
    }

    private void applyLoweredShape(Block block, BlockShape shape, Map<BlockPart, Integer> tileIndices) {
        for (Side side : Side.values()) {
            BlockPart part = BlockPart.fromSide(side);
            block.setLoweredLiquidMesh(part.getSide(), shape.getMeshPart(part).rotate(Rotation.NONE.getQuat4f()).mapTexCoords(calcAtlasPositionForId(tileIndices.get(part)), Block.TEXTURE_OFFSET_WIDTH));
        }
    }

    private Vector2f calcAtlasPositionForId(int id) {
        return new Vector2f((id % Block.ATLAS_ELEMENTS_PER_ROW_AND_COLUMN) * Block.TEXTURE_OFFSET, (id / Block.ATLAS_ELEMENTS_PER_ROW_AND_COLUMN) * Block.TEXTURE_OFFSET);
    }

    private Block createRawBlock(BlockDefinition def, String defaultName) {
        Block block = new Block();
        block.setLiquid(def.liquid);
        block.setHardness(def.hardness);
        block.setAttachmentAllowed(def.attachmentAllowed);
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

        if (def.physics != null) {
            block.setMass(def.physics.mass);
            block.setDebrisOnDestroy(def.physics.debrisOnDestroy);
        }

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

    private Map<BlockPart, Integer> prepareTiles(BlockDefinition blockDef, String defaultName) {
        Map<BlockPart, Integer> tileIndices = Maps.newEnumMap(BlockPart.class);
        if (!blockDef.tile.isEmpty()) {
            defaultName = blockDef.tile;
        }
        int tileId = getTileIndex(defaultName);
        for (BlockPart part : BlockPart.values()) {
            tileIndices.put(part, tileId);
        }

        if (blockDef.tiles != null) {
            for (BlockPart part : BlockPart.values()) {
                String partTile = blockDef.tiles.map.get(part);
                if (partTile != null) {
                    tileId = getTileIndex(blockDef.tiles.map.get(part));
                    tileIndices.put(part, tileId);
                }
            }
        }
        return tileIndices;
    }

    private int getTileIndex(String tileUri) {
        AssetUri uri = new AssetUri(AssetType.BLOCK_TILE, tileUri);
        if (tileIndexes.containsKey(tileUri)) {
            return tileIndexes.get(tileUri);
        }
        Tile tile = (Tile)AssetManager.load(uri);
        if (tile != null) {
            int index = tiles.size();
            tiles.add(tile);
            tileIndexes.put(uri, index);
            return index;
        } else {
            logger.warning("Unable to resolve block tile '" + uri + "'");
            return 0;
        }
    }

    private BlockDefinition loadBlockDefinition(AssetUri blockDefUri) {
        InputStream stream = null;
        try {
            stream = AssetManager.assetStream(blockDefUri);
            if (stream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                return gson.fromJson(reader, BlockDefinition.class);
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

}
