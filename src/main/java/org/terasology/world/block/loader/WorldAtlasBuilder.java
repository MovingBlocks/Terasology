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
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.lwjgl.opengl.Util;
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
import org.terasology.world.block.Block;

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
public class WorldAtlasBuilder {
    private static final Logger logger = LoggerFactory.getLogger(WorldAtlasBuilder.class);

    private static final int MAX_TILES = 256;
    private int atlasSize = 256;
    private int tileSize = 16;

    private TObjectIntMap<AssetUri> tileIndexes = new TObjectIntHashMap<AssetUri>();
    private List<TileData> tiles = Lists.newArrayList();

    public int getAtlasSize() {
        return atlasSize;
    }

    public int getNumMipmaps() {
        return TeraMath.sizeOfPower(tileSize) + 1;
    }

    public void buildAtlas() {
        int numMipMaps = getNumMipmaps();
        ByteBuffer[] data = new ByteBuffer[numMipMaps];
        for (int i = 0; i < numMipMaps; ++i) {
            BufferedImage image = generateAtlas(i);
            if (i == 0) {
                try (OutputStream stream = new BufferedOutputStream(Files.newOutputStream(PathManager.getInstance().getScreenshotPath().resolve("tiles.png")))) {
                    ImageIO.write(image, "png", stream);
                } catch (IOException e) {
                    logger.warn("Failed to write atlas");
                }
            }

            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                ImageIO.write(image, "png", bos);
                PNGDecoder decoder = new PNGDecoder(new ByteArrayInputStream(bos.toByteArray()));
                ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
                decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
                buf.flip();
                data[i] = buf;
            } catch (IOException e) {
                logger.error("Failed to create atlas texture");
            }
        }

        Util.checkGLError();
        TextureData terrainTexData = new TextureData(atlasSize, atlasSize, data, Texture.WrapMode.Clamp, Texture.FilterMode.Nearest);
        Util.checkGLError();
        Texture terrainTex = Assets.generateAsset(new AssetUri(AssetType.TEXTURE, "engine:terrain"), terrainTexData, Texture.class);
        Util.checkGLError();
        MaterialData terrainMatData = new MaterialData(Assets.getShader("engine:block"));
        Util.checkGLError();
        terrainMatData.setParam("textureAtlas", terrainTex);
        terrainMatData.setParam("colorOffset", new float[]{1, 1, 1});
        terrainMatData.setParam("textured", 1);
        Util.checkGLError();
        Assets.generateAsset(new AssetUri(AssetType.MATERIAL, "engine:terrain"), terrainMatData, Material.class);
        Util.checkGLError();
    }

    private BufferedImage generateAtlas(int mipMapLevel) {
        int size = atlasSize / (1 << mipMapLevel);
        int textureSize = tileSize / (1 << mipMapLevel);
        int tilesPerDim = atlasSize / tileSize;

        BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics g = result.getGraphics();

        if (tiles.size() > MAX_TILES) {
            logger.error("Too many tiles, culling overflow");
        }

        for (int index = 0; index < tiles.size() && index < MAX_TILES; ++index) {
            TileData tile = tiles.get(index);

            int posX = (index) % tilesPerDim;
            int posY = (index) / tilesPerDim;
            g.drawImage(tile.getImage().getScaledInstance(textureSize, textureSize, Image.SCALE_SMOOTH), posX * textureSize, posY * textureSize, null);
        }

        return result;
    }

    public void addToAtlas(AssetUri uri) {
        if (!tileIndexes.containsKey(uri)) {
            indexTile(uri, true);
        }
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
        return new Vector2f((id % tilesPerDim) * Block.TEXTURE_OFFSET, (id / tilesPerDim) * Block.TEXTURE_OFFSET);
    }

    private int getTileIndex(AssetUri uri, boolean warnOnError) {
        if (tileIndexes.containsKey(uri)) {
            return tileIndexes.get(uri);
        }
        return indexTile(uri, warnOnError);
    }

    private int indexTile(AssetUri uri, boolean warnOnError) {
        TileData tile = AssetManager.tryLoadAssetData(uri, TileData.class);
        if (tile != null) {
            int index = tiles.size();
            tiles.add(tile);
            tileIndexes.put(uri, index);
            return index;
        } else if (warnOnError) {
            logger.warn("Unable to resolve block tile '{}'", uri);
        }
        return 0;
    }
}
