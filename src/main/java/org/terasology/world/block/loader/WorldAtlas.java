/*
 * Copyright 2013 MovingBlocks
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
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.newdawn.slick.opengl.PNGDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.engine.paths.PathManager;
import org.terasology.math.TeraMath;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.material.MaterialData;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;

import javax.imageio.ImageIO;
import javax.vecmath.Vector2f;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.List;

/**
 * @author Immortius
 */
public class WorldAtlas {
    private static final Logger logger = LoggerFactory.getLogger(WorldAtlas.class);

    private static final int MAX_TILES = 65536;
    private static final Color UNIT_Z_COLOR = new Color(0.5f, 0.5f, 1.0f, 1.0f);
    private static final Color TRANSPARENT_COLOR = new Color(0.0f, 0.0f, 0.0f, 0.0f);
    private static final Color BLACK_COLOR = new Color(0.0f, 0.0f, 0.0f, 1.0f);

    private int maxAtlasSize = 4096;
    private int atlasSize = 256;
    private int tileSize = 16;

    private TObjectIntMap<AssetUri> tileIndexes = new TObjectIntHashMap<>();
    private List<TileData> tiles = Lists.newArrayList();
    private List<TileData> tilesNormal = Lists.newArrayList();
    private List<TileData> tilesHeight = Lists.newArrayList();

    private TileData defaultNormal;
    private TileData defaultHeight;

    /**
     * @param maxAtlasSize The maximum dimensions of the atlas (both width and height, in pixels)
     */
    public WorldAtlas(int maxAtlasSize) {
        this.maxAtlasSize = maxAtlasSize;
        for (AssetUri tile : AssetManager.getInstance().listAssets(AssetType.BLOCK_TILE)) {
            indexTile(tile, false);
        }
        buildAtlas();
    }

    public int getTileSize() {
        return tileSize;
    }

    public int getAtlasSize() {
        return atlasSize;
    }

    public float getRelativeTileSize() {
        return ((float) getTileSize()) / (float) getAtlasSize();
    }

    public int getNumMipmaps() {
        return TeraMath.sizeOfPower(tileSize) + 1;
    }

    /**
     * Obtains the tex coords of a block tile. If it isn't part of the atlas it is added to the atlas.
     *
     * @param uri         The uri of the block tile of interest.
     * @param warnOnError Whether a warning should be logged if the asset canot be found
     * @return The tex coords of the tile in the atlas.
     */
    public Vector2f getTexCoords(AssetUri uri, boolean warnOnError) {
        return getTexCoords(getTileIndex(uri, warnOnError));
    }

    private Vector2f getTexCoords(int id) {
        int tilesPerDim = atlasSize / tileSize;
        return new Vector2f((id % tilesPerDim) * getRelativeTileSize(), (id / tilesPerDim) * getRelativeTileSize());
    }

    private int getTileIndex(AssetUri uri, boolean warnOnError) {
        if (tileIndexes.containsKey(uri)) {
            return tileIndexes.get(uri);
        }
        return indexTile(uri, warnOnError);
    }

    private int indexTile(AssetUri uri, boolean warnOnError) {
        if (tiles.size() == MAX_TILES) {
            logger.error("Maximum tiles exceeded");
            return 0;
        }
        TileData tile = AssetManager.tryLoadAssetData(uri, TileData.class);
        if (tile != null) {
            if (checkTile(tile)) {
                int index = tiles.size();
                tiles.add(tile);
                addNormal(uri);
                addHeightMap(uri);
                tileIndexes.put(uri, index);
                return index;
            } else {
                logger.error("Invalid tile {}, must be a square with power-of-two sides.", uri);
                return 0;
            }
        } else if (warnOnError) {
            logger.warn("Unable to resolve block tile '{}'", uri);
        }
        return 0;
    }

    private boolean checkTile(TileData tile) {
        return tile.getImage().getWidth() == tile.getImage().getHeight()
                && TeraMath.isPowerOfTwo(tile.getImage().getWidth());
    }

    private void addNormal(AssetUri uri) {
        String name = uri.toSimpleString() + "Normal";
        TileData tile = AssetManager.tryLoadAssetData(new AssetUri(AssetType.BLOCK_TILE, name), TileData.class);
        if (tile != null) {
            tilesNormal.add(tile);
        } else {
            tilesNormal.add(defaultNormal);
        }
    }

    private void addHeightMap(AssetUri uri) {
        String name = uri.toSimpleString() + "Height";
        TileData tile = AssetManager.tryLoadAssetData(new AssetUri(AssetType.BLOCK_TILE, name), TileData.class);
        if (tile != null) {
            tilesHeight.add(tile);
        } else {
            tilesHeight.add(defaultHeight);
        }
    }

    private void buildAtlas() {
        calculateAtlasSizes();

        int numMipMaps = getNumMipmaps();
        ByteBuffer[] data = createAtlasMipmaps(numMipMaps, TRANSPARENT_COLOR, tiles, "tiles.png");
        ByteBuffer[] dataNormal = createAtlasMipmaps(numMipMaps, UNIT_Z_COLOR, tilesNormal, "tilesNormal.png");
        ByteBuffer[] dataHeight = createAtlasMipmaps(numMipMaps, BLACK_COLOR, tilesHeight, "tilesHeight.png");

        TextureData terrainTexData = new TextureData(atlasSize, atlasSize, data, Texture.WrapMode.Clamp, Texture.FilterMode.Nearest);
        Texture terrainTex = Assets.generateAsset(new AssetUri(AssetType.TEXTURE, "engine:terrain"), terrainTexData, Texture.class);

        TextureData terrainNormalData = new TextureData(atlasSize, atlasSize, dataNormal, Texture.WrapMode.Clamp, Texture.FilterMode.Nearest);
        Assets.generateAsset(new AssetUri(AssetType.TEXTURE, "engine:terrainNormal"), terrainNormalData, Texture.class);

        TextureData terrainHeightData = new TextureData(atlasSize, atlasSize, dataHeight, Texture.WrapMode.Clamp, Texture.FilterMode.Nearest);
        Assets.generateAsset(new AssetUri(AssetType.TEXTURE, "engine:terrainHeight"), terrainHeightData, Texture.class);

        MaterialData terrainMatData = new MaterialData(Assets.getShader("engine:block"));
        terrainMatData.setParam("textureAtlas", terrainTex);
        terrainMatData.setParam("colorOffset", new float[]{1, 1, 1});
        terrainMatData.setParam("textured", true);
        Assets.generateAsset(new AssetUri(AssetType.MATERIAL, "engine:terrain"), terrainMatData, Material.class);
    }

    private ByteBuffer[] createAtlasMipmaps(int numMipMaps, Color initialColor, List<TileData> tileImages, String screenshotName) {
        ByteBuffer[] data = new ByteBuffer[numMipMaps];
        for (int i = 0; i < numMipMaps; ++i) {
            BufferedImage image = generateAtlas(i, tileImages, initialColor);
            if (i == 0) {
                try (OutputStream stream = new BufferedOutputStream(Files.newOutputStream(PathManager.getInstance().getScreenshotPath().resolve(screenshotName)))) {
                    ImageIO.write(image, "png", stream);
                } catch (IOException e) {
                    logger.warn("Failed to write atlas");
                }
            }

            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                ImageIO.write(image, "png", bos);
                PNGDecoder decoder = new PNGDecoder(new ByteArrayInputStream(bos.toByteArray()));
                ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
                decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.RGBA);
                buf.flip();
                data[i] = buf;
            } catch (IOException e) {
                logger.error("Failed to create atlas texture");
            }
        }
        return data;
    }

    // The atlas is configured using the following constraints...
    // 1.   The overall tile size is the size of the largest tile loaded
    // 2.   The atlas will never be larger than 4096*4096 px
    // 3.   The tile size gets adjusted if the tiles won't fit into the atlas using the overall tile size
    //      (the tile size gets halved until all tiles will fit into the atlas)
    // 4.   The size of the atlas is always a power of two - as is the tile size
    private void calculateAtlasSizes() {
        tileSize = 16;
        for (TileData tile : tiles) {
            if (tile.getImage().getWidth() > tileSize) {
                tileSize = tile.getImage().getWidth();
            }
        }

        atlasSize = 1;
        while (atlasSize * atlasSize < tiles.size()) {
            atlasSize *= 2;
        }
        atlasSize = atlasSize * tileSize;

        if (atlasSize > maxAtlasSize) {
            atlasSize = maxAtlasSize;
            int maxTiles = (atlasSize / tileSize) * (atlasSize / tileSize);
            while (maxTiles < tiles.size()) {
                tileSize >>= 1;
                maxTiles = (atlasSize / tileSize) * (atlasSize / tileSize);
            }
        }
    }

    private BufferedImage generateAtlas(int mipMapLevel, List<TileData> tileImages, Color clearColor) {
        int size = atlasSize / (1 << mipMapLevel);
        int textureSize = tileSize / (1 << mipMapLevel);
        int tilesPerDim = atlasSize / tileSize;

        BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics g = result.getGraphics();

        g.setColor(clearColor);
        g.fillRect(0, 0, size, size);
        for (int index = 0; index < tileImages.size(); ++index) {
            int posX = (index) % tilesPerDim;
            int posY = (index) / tilesPerDim;
            TileData tile = tileImages.get(index);
            if (tile != null) {
                g.drawImage(tile.getImage().getScaledInstance(textureSize, textureSize, Image.SCALE_SMOOTH), posX * textureSize, posY * textureSize, null);
            }
        }

        return result;
    }
}
