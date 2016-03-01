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
package org.terasology.world.block.tiles;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.math.IntMath;
import de.matthiasmann.twl.utils.PNGDecoder;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.utilities.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.paths.PathManager;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Vector2f;
import org.terasology.naming.Name;
import org.terasology.rendering.assets.atlas.Atlas;
import org.terasology.rendering.assets.atlas.AtlasData;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.material.MaterialData;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;
import org.terasology.rendering.assets.texture.subtexture.SubtextureData;

import javax.imageio.ImageIO;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

/**
 */
public class WorldAtlasImpl implements WorldAtlas {
    private static final Logger logger = LoggerFactory.getLogger(WorldAtlasImpl.class);

    private static final int MAX_TILES = 65536;
    private static final Color UNIT_Z_COLOR = new Color(0.5f, 0.5f, 1.0f, 1.0f);
    private static final Color TRANSPARENT_COLOR = new Color(0.0f, 0.0f, 0.0f, 0.0f);
    private static final Color BLACK_COLOR = new Color(0.0f, 0.0f, 0.0f, 1.0f);

    private int maxAtlasSize = 4096;
    private int atlasSize = 256;
    private int tileSize = 16;

    private TObjectIntMap<ResourceUrn> tileIndexes = new TObjectIntHashMap<>();
    private List<BlockTile> tiles = Lists.newArrayList();
    private List<BlockTile> tilesNormal = Lists.newArrayList();
    private List<BlockTile> tilesHeight = Lists.newArrayList();

    private BlockingQueue<BlockTile> reloadQueue = Queues.newLinkedBlockingQueue();

    private Consumer<BlockTile> tileReloadListener = reloadQueue::add;

    /**
     * @param maxAtlasSize The maximum dimensions of the atlas (both width and height, in pixels)
     */
    public WorldAtlasImpl(int maxAtlasSize) {
        this.maxAtlasSize = maxAtlasSize;
        Assets.list(BlockTile.class).forEach(this::indexTile);
        buildAtlas();
    }

    @Override
    public int getTileSize() {
        return tileSize;
    }

    @Override
    public int getAtlasSize() {
        return atlasSize;
    }

    @Override
    public float getRelativeTileSize() {
        return ((float) getTileSize()) / (float) getAtlasSize();
    }

    @Override
    public int getNumMipmaps() {
        return TeraMath.sizeOfPower(tileSize) + 1;
    }

    @Override
    public Vector2f getTexCoords(BlockTile tile, boolean warnOnError) {
        return getTexCoords(tile.getUrn(), warnOnError);
    }

    /**
     * Obtains the tex coords of a block tile. If it isn't part of the atlas it is added to the atlas.
     *
     * @param uri         The uri of the block tile of interest.
     * @param warnOnError Whether a warning should be logged if the asset canot be found
     * @return The tex coords of the tile in the atlas.
     */
    @Override
    public Vector2f getTexCoords(ResourceUrn uri, boolean warnOnError) {
        return getTexCoords(getTileIndex(uri, warnOnError));
    }

    @Override
    public void update() {
        if (!reloadQueue.isEmpty()) {
            List<BlockTile> reloadList = Lists.newArrayListWithExpectedSize(reloadQueue.size());
            reloadQueue.drainTo(reloadList);
            // TODO: does this need to be more efficient? could just reload individual block tile locations.
            buildAtlas();
        }
    }

    @Override
    public void dispose() {
        for (BlockTile tile : tiles) {
            tile.unsubscribe(tileReloadListener);
        }
    }

    private Vector2f getTexCoords(int id) {
        int tilesPerDim = atlasSize / tileSize;
        return new Vector2f((id % tilesPerDim) * getRelativeTileSize(), (id / tilesPerDim) * getRelativeTileSize());
    }

    private int getTileIndex(ResourceUrn uri, boolean warnOnError) {
        if (tileIndexes.containsKey(uri)) {
            return tileIndexes.get(uri);
        }
        if (warnOnError) {
            logger.warn("Tile {} could not be resolved", uri);
        }
        return 0;
    }

    private int indexTile(ResourceUrn uri) {
        if (tiles.size() == MAX_TILES) {
            logger.error("Maximum tiles exceeded");
            return 0;
        }
        Optional<BlockTile> tile = Assets.get(uri, BlockTile.class);
        if (tile.isPresent()) {
            if (checkTile(tile.get())) {
                int index = tiles.size();
                tiles.add(tile.get());
                addNormal(uri);
                addHeightMap(uri);
                tileIndexes.put(uri, index);
                tile.get().subscribe(tileReloadListener);
                return index;
            } else {
                logger.error("Invalid tile {}, must be a square with power-of-two sides.", uri);
                return 0;
            }
        }
        return 0;
    }

    private boolean checkTile(BlockTile tile) {
        return tile.getImage().getWidth() == tile.getImage().getHeight()
                && IntMath.isPowerOfTwo(tile.getImage().getWidth());
    }

    private void addNormal(ResourceUrn uri) {
        String name = uri.toString() + "Normal";
        Optional<BlockTile> tile = Assets.get(name, BlockTile.class);
        if (tile.isPresent()) {
            tilesNormal.add(tile.get());
        }
    }

    private void addHeightMap(ResourceUrn uri) {
        String name = uri.toString() + "Height";
        Optional<BlockTile> tile = Assets.get(name, BlockTile.class);
        if (tile.isPresent()) {
            tilesHeight.add(tile.get());
        }
    }

    private void buildAtlas() {
        calculateAtlasSizes();

        int numMipMaps = getNumMipmaps();
        ByteBuffer[] data = createAtlasMipmaps(numMipMaps, TRANSPARENT_COLOR, tiles, "tiles.png");
        ByteBuffer[] dataNormal = createAtlasMipmaps(numMipMaps, UNIT_Z_COLOR, tilesNormal, "tilesNormal.png");
        ByteBuffer[] dataHeight = createAtlasMipmaps(numMipMaps, BLACK_COLOR, tilesHeight, "tilesHeight.png");

        TextureData terrainTexData = new TextureData(atlasSize, atlasSize, data, Texture.WrapMode.CLAMP, Texture.FilterMode.NEAREST);
        Texture terrainTex = Assets.generateAsset(new ResourceUrn("engine:terrain"), terrainTexData, Texture.class);

        TextureData terrainNormalData = new TextureData(atlasSize, atlasSize, dataNormal, Texture.WrapMode.CLAMP, Texture.FilterMode.NEAREST);
        Assets.generateAsset(new ResourceUrn("engine:terrainNormal"), terrainNormalData, Texture.class);

        TextureData terrainHeightData = new TextureData(atlasSize, atlasSize, dataHeight, Texture.WrapMode.CLAMP, Texture.FilterMode.NEAREST);
        Assets.generateAsset(new ResourceUrn("engine:terrainHeight"), terrainHeightData, Texture.class);

        MaterialData terrainMatData = new MaterialData(Assets.getShader("engine:block").get());
        terrainMatData.setParam("textureAtlas", terrainTex);
        terrainMatData.setParam("colorOffset", new float[]{1, 1, 1});
        terrainMatData.setParam("textured", true);
        Assets.generateAsset(new ResourceUrn("engine:terrain"), terrainMatData, Material.class);

        createTextureAtlas(terrainTex);
    }

    private void createTextureAtlas(final Texture texture) {
        final Map<Name, Map<Name, SubtextureData>> textureAtlases = Maps.newHashMap();
        final Vector2f texSize = new Vector2f(getRelativeTileSize(), getRelativeTileSize());
        tileIndexes.forEachEntry((tileUri, index) -> {
            Vector2f coords = getTexCoords(index);
            SubtextureData subtextureData = new SubtextureData(texture, Rect2f.createFromMinAndSize(coords, texSize));

            Map<Name, SubtextureData> textureAtlas = textureAtlases.get(tileUri.getModuleName());
            if (textureAtlas == null) {
                textureAtlas = Maps.newHashMap();
                textureAtlases.put(tileUri.getModuleName(), textureAtlas);
            }
            textureAtlas.put(tileUri.getResourceName(), subtextureData);

            return true;
        });

        for (Map.Entry<Name, Map<Name, SubtextureData>> atlas : textureAtlases.entrySet()) {
            AtlasData data = new AtlasData(atlas.getValue());
            Assets.generateAsset(new ResourceUrn(atlas.getKey(), new Name("terrain")), data, Atlas.class);
        }
    }

    private ByteBuffer[] createAtlasMipmaps(int numMipMaps, Color initialColor, List<BlockTile> tileImages, String screenshotName) {
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
                decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
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
        tiles.stream().filter(tile -> tile.getImage().getWidth() > tileSize).forEach(tile -> tileSize = tile.getImage().getWidth());

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

    private BufferedImage generateAtlas(int mipMapLevel, List<BlockTile> tileImages, Color clearColor) {
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
            BlockTile tile = tileImages.get(index);
            if (tile != null) {
                g.drawImage(tile.getImage().getScaledInstance(textureSize, textureSize, Image.SCALE_SMOOTH), posX * textureSize, posY * textureSize, null);
            }
        }

        return result;
    }
}
