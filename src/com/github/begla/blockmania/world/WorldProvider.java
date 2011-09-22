/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package com.github.begla.blockmania.world;

import com.github.begla.blockmania.blocks.Block;
import com.github.begla.blockmania.generators.*;
import com.github.begla.blockmania.main.Blockmania;
import com.github.begla.blockmania.main.Configuration;
import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.world.chunk.Chunk;
import com.github.begla.blockmania.world.chunk.ChunkCache;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.lwjgl.util.vector.Vector3f;
import org.xml.sax.InputSource;

import java.io.*;
import java.util.logging.Level;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class WorldProvider {

    /* WORLD GENERATION */
    protected final FastMap<String, ChunkGenerator> _chunkGenerators = new FastMap<String, ChunkGenerator>();
    protected final FastMap<String, ObjectGenerator> _objectGenerators = new FastMap<String, ObjectGenerator>();
    /* PROPERTIES */
    protected String _title, _seed;
    protected long _creationTime = Blockmania.getInstance().getTime();
    protected final long DAY_NIGHT_LENGTH_IN_MS = (60 * 1000) * 20; // 20 Minutes;
    /* UPDATING & CACHING */
    protected final ChunkCache _chunkCache = new ChunkCache(this);
    /* ETC. */
    protected final FastRandom _random;
    /* SPAWNING POINT */
    protected Vector3f _spawningPoint;

    /**
     * Initializes a new world for the single player mode.
     *
     * @param title The title/description of the world
     * @param seed  The seed string used to generate the terrain
     */
    public WorldProvider(String title, String seed) {
        if (title == null) {
            throw new IllegalArgumentException("No title provided.");
        }

        if (title.isEmpty()) {
            throw new IllegalArgumentException("No title provided.");
        }

        if (seed == null) {
            throw new IllegalArgumentException("No seed provided.");
        }

        if (seed.isEmpty()) {
            throw new IllegalArgumentException("No seed provided.");
        }

        _title = title;
        _seed = seed;
        // Init. random generator
        _random = new FastRandom(seed.hashCode());

        // Initial time
        setTime(0.05);

        // Load the meta data of this world
        loadMetaData();

        // Init. generators
        _chunkGenerators.put("terrain", new ChunkGeneratorTerrain(_seed));
        _chunkGenerators.put("forest", new ChunkGeneratorForest(_seed));
        _chunkGenerators.put("resources", new ChunkGeneratorResources(_seed));
        _objectGenerators.put("tree", new ObjectGeneratorTree(this, _seed));
        _objectGenerators.put("pineTree", new ObjectGeneratorPineTree(this, _seed));
        _objectGenerators.put("firTree", new ObjectGeneratorFirTree(this, _seed));
        _objectGenerators.put("cactus", new ObjectGeneratorCactus(this, _seed));

        if (_spawningPoint == null) {
            _spawningPoint = findNewSpawningPoint();
        }
    }

    /**
     * Returns the chunk position of a given coordinate.
     *
     * @param x The X-coordinate of the block
     * @return The X-coordinate of the chunk
     */
    public int calcChunkPosX(int x) {
        // Offset for negative chunks
        if (x < 0)
            x -= 15;

        return (x / (int) Configuration.CHUNK_DIMENSIONS.x);
    }

    /**
     * Returns the chunk position of a given coordinate.
     *
     * @param z The Z-coordinate of the block
     * @return The Z-coordinate of the chunk
     */
    public int calcChunkPosZ(int z) {
        // Offset for negative chunks
        if (z < 0)
            z -= 15;

        return (z / (int) Configuration.CHUNK_DIMENSIONS.z);
    }

    /**
     * Returns the internal position of a block within a chunk.
     *
     * @param x1 The X-coordinate of the block within the world
     * @param x2 The X-coordinate of the chunk within the world
     * @return The X-coordinate of the block within the chunk
     */
    public int calcBlockPosX(int x1, int x2) {
        return Math.abs(x1 - (x2 * (int) Configuration.CHUNK_DIMENSIONS.x));
    }

    /**
     * Returns the internal position of a block within a chunk.
     *
     * @param z1 The Z-coordinate of the block within the world
     * @param z2 The Z-coordinate of the chunk within the world
     * @return The Z-coordinate of the block within the chunk
     */
    public int calcBlockPosZ(int z1, int z2) {
        return Math.abs(z1 - (z2 * (int) Configuration.CHUNK_DIMENSIONS.z));
    }

    /**
     * Places a block of a specific type at a given position and refreshes the
     * corresponding light values.
     *
     * @param x           The X-coordinate
     * @param y           The Y-coordinate
     * @param z           The Z-coordinate
     * @param type        The type of the block to set
     * @param updateLight If set the affected chunk is queued for updating
     * @param overwrite
     */
    public final boolean setBlock(int x, int y, int z, byte type, boolean updateLight, boolean overwrite) {
        int chunkPosX = calcChunkPosX(x);
        int chunkPosZ = calcChunkPosZ(z);

        int blockPosX = calcBlockPosX(x, chunkPosX);
        int blockPosZ = calcBlockPosZ(z, chunkPosZ);

        Chunk c = _chunkCache.loadOrCreateChunk(calcChunkPosX(x), calcChunkPosZ(z));

        if (c == null) {
            return false;
        }

        if (overwrite || c.getBlock(blockPosX, y, blockPosZ) == 0x0) {

            byte oldBlock = c.getBlock(blockPosX, y, blockPosZ);
            byte newBlock = oldBlock;

            if (Block.getBlockForType(c.getBlock(blockPosX, y, blockPosZ)).isRemovable()) {
                c.setBlock(blockPosX, y, blockPosZ, type);
                newBlock = type;
            } else {
                return false;
            }

            if (updateLight) {

                /*
                * Update sunlight.
                */
                c.refreshSunlightAtLocalPos(blockPosX, blockPosZ, true, true);

                byte blockLightPrev = getLight(x, y, z, Chunk.LIGHT_TYPE.BLOCK);
                byte blockLightCurrent = blockLightPrev;

                // New block placed
                if (oldBlock == 0x0 && newBlock != 0x0) {
                    /*
                    * Spread light of block light sources.
                    */
                    byte luminance = Block.getBlockForType(type).getLuminance();

                    // Set the block light value to the luminance of this block
                    c.setLight(blockPosX, y, blockPosZ, luminance, Chunk.LIGHT_TYPE.BLOCK);
                    blockLightCurrent = luminance;
                } else { // Block removed
                    /*
                    * Update the block light intensity of the current block.
                    */
                    c.setLight(blockPosX, y, blockPosZ, (byte) 0x0, Chunk.LIGHT_TYPE.BLOCK);
                    c.refreshLightAtLocalPos(blockPosX, y, blockPosZ, Chunk.LIGHT_TYPE.BLOCK);
                    blockLightCurrent = getLight(x, y, z, Chunk.LIGHT_TYPE.BLOCK);
                }

                // Block light is brighter than before
                if (blockLightCurrent > blockLightPrev) {
                    c.spreadLight(blockPosX, y, blockPosZ, blockLightCurrent, Chunk.LIGHT_TYPE.BLOCK);
                } else if (blockLightCurrent < blockLightPrev) { // Block light is darker than before
                    c.unspreadLight(blockPosX, y, blockPosZ, blockLightPrev, Chunk.LIGHT_TYPE.BLOCK);
                }
            }
        }

        return true;
    }

    /**
     * @param pos
     * @return
     */
    public final byte getBlockAtPosition(Vector3f pos) {
        return getBlock((int) (pos.x + ((pos.x >= 0) ? 0.5f : -0.5f)), (int) (pos.y + ((pos.y >= 0) ? 0.5f : -0.5f)), (int) (pos.z + ((pos.z >= 0) ? 0.5f : -0.5f)));
    }


    /**
     * @param pos
     * @return
     */
    public final byte getLightAtPosition(Vector3f pos, Chunk.LIGHT_TYPE type) {
        return getLight((int) (pos.x + ((pos.x >= 0) ? 0.5f : -0.5f)), (int) (pos.y + ((pos.y >= 0) ? 0.5f : -0.5f)), (int) (pos.z + ((pos.z >= 0) ? 0.5f : -0.5f)), type);
    }

    /**
     * Returns the block at the given position.
     *
     * @param x The X-coordinate
     * @param y The Y-coordinate
     * @param z The Z-coordinate
     * @return The type of the block
     */
    public final byte getBlock(int x, int y, int z) {
        int chunkPosX = calcChunkPosX(x);
        int chunkPosZ = calcChunkPosZ(z);

        int blockPosX = calcBlockPosX(x, chunkPosX);
        int blockPosZ = calcBlockPosZ(z, chunkPosZ);

        Chunk c = _chunkCache.loadOrCreateChunk(calcChunkPosX(x), calcChunkPosZ(z));

        if (c != null) {
            return c.getBlock(blockPosX, y, blockPosZ);
        }

        return 0;
    }

    /**
     * Returns true if the block is surrounded by blocks within the N4-neighborhood on the xz-plane.
     *
     * @param x The X-coordinate
     * @param y The Y-coordinate
     * @param z The Z-coordinate
     * @return
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public final boolean isBlockSurrounded(int x, int y, int z) {
        return (getBlock(x + 1, y, z) > 0 || getBlock(x - 1, y, z) > 0 || getBlock(x, y, z + 1) > 0 || getBlock(x, y, z - 1) > 0);
    }

    /**
     * @param x
     * @param z
     * @return
     */
    public final int maxHeightAt(int x, int z) {
        for (int y = (int) Configuration.CHUNK_DIMENSIONS.y - 1; y >= 0; y--) {
            if (getBlock(x, y, z) != 0x0)
                return y;
        }

        return 0;
    }

    /**
     * Returns the light value at the given position.
     *
     * @param x    The X-coordinate
     * @param y    The Y-coordinate
     * @param z    The Z-coordinate
     * @param type
     * @return The light value
     */
    public final byte getLight(int x, int y, int z, Chunk.LIGHT_TYPE type) {
        int chunkPosX = calcChunkPosX(x);
        int chunkPosZ = calcChunkPosZ(z);

        int blockPosX = calcBlockPosX(x, chunkPosX);
        int blockPosZ = calcBlockPosZ(z, chunkPosZ);

        Chunk c = _chunkCache.loadOrCreateChunk(calcChunkPosX(x), calcChunkPosZ(z));

        if (c != null) {
            return c.getLight(blockPosX, y, blockPosZ, type);
        }

        if (type == Chunk.LIGHT_TYPE.SUN)
            return 15;
        else
            return 0;
    }

    /**
     * Sets the light value at the given position.
     *
     * @param x      The X-coordinate
     * @param y      The Y-coordinate
     * @param z      The Z-coordinate
     * @param intens The light intensity value
     * @param type
     */
    public void setLight(int x, int y, int z, byte intens, Chunk.LIGHT_TYPE type) {
        int chunkPosX = calcChunkPosX(x);
        int chunkPosZ = calcChunkPosZ(z);

        int blockPosX = calcBlockPosX(x, chunkPosX);
        int blockPosZ = calcBlockPosZ(z, chunkPosZ);

        Chunk c = _chunkCache.loadOrCreateChunk(calcChunkPosX(x), calcChunkPosZ(z));

        if (c != null) {
            c.setLight(blockPosX, y, blockPosZ, intens, type);
        }
    }

    /**
     * Refreshes sunlight vertically at a given global position.
     *
     * @param x
     * @param spreadLight
     * @param refreshSunlight
     * @param z
     */
    public void refreshSunlightAt(int x, int z, boolean spreadLight, boolean refreshSunlight) {
        int chunkPosX = calcChunkPosX(x);
        int chunkPosZ = calcChunkPosZ(z);

        int blockPosX = calcBlockPosX(x, chunkPosX);
        int blockPosZ = calcBlockPosZ(z, chunkPosZ);

        Chunk c = _chunkCache.loadOrCreateChunk(calcChunkPosX(x), calcChunkPosZ(z));

        if (c != null) {
            c.refreshSunlightAtLocalPos(blockPosX, blockPosZ, spreadLight, refreshSunlight);
        }
    }

    /**
     * Recursive light calculation.
     *
     * @param x
     * @param y
     * @param z
     * @param lightValue
     * @param depth
     * @param type
     */
    public void unspreadLight(int x, int y, int z, byte lightValue, int depth, Chunk.LIGHT_TYPE type, FastList<Vector3f> brightSpots) {
        int chunkPosX = calcChunkPosX(x);
        int chunkPosZ = calcChunkPosZ(z);

        int blockPosX = calcBlockPosX(x, chunkPosX);
        int blockPosZ = calcBlockPosZ(z, chunkPosZ);

        Chunk c = _chunkCache.loadOrCreateChunk(calcChunkPosX(x), calcChunkPosZ(z));
        if (c != null) {
            c.unspreadLight(blockPosX, y, blockPosZ, lightValue, depth, type, brightSpots);
        }
    }

    /**
     * Recursive light calculation.
     *
     * @param x
     * @param y
     * @param z
     * @param lightValue
     * @param depth
     * @param type
     */
    public void spreadLight(int x, int y, int z, byte lightValue, int depth, Chunk.LIGHT_TYPE type) {
        int chunkPosX = calcChunkPosX(x);
        int chunkPosZ = calcChunkPosZ(z);

        int blockPosX = calcBlockPosX(x, chunkPosX);
        int blockPosZ = calcBlockPosZ(z, chunkPosZ);

        Chunk c = _chunkCache.loadOrCreateChunk(calcChunkPosX(x), calcChunkPosZ(z));
        if (c != null) {
            c.spreadLight(blockPosX, y, blockPosZ, lightValue, depth, type);
        }
    }

    public ObjectGenerator getObjectGenerator(String s) {
        return _objectGenerators.get(s);
    }

    public ChunkGenerator getChunkGenerator(String s) {
        return _chunkGenerators.get(s);
    }

    /**
     * Returns true if it is daytime.
     *
     * @return
     */
    public boolean isDaytime() {
        return getTime() > 0.075f && getTime() < 0.575;
    }

    /**
     * Returns true if it is nighttime.
     *
     * @return
     */
    public boolean isNighttime() {
        return !isDaytime();
    }

    /**
     * @return
     */
    public Vector3f findNewSpawningPoint() {
        for (; ; ) {
            int randX = (int) (_random.randomDouble() * 16000f);
            int randZ = (int) (_random.randomDouble() * 16000f);

            double dens = ((ChunkGeneratorTerrain) getChunkGenerator("terrain")).calcDensity(randX, 32, randZ, ChunkGeneratorTerrain.BIOME_TYPE.PLAINS);

            if (dens >= 0.008 && dens < 0.02)
                return new Vector3f(randX, 128, randZ);
        }
    }

    /**
     * @return
     */
    public String getWorldSavePath() {
        return String.format("SAVED_WORLDS/%s", _title);
    }

    /**
     * @return
     */
    public boolean saveMetaData() {
        if (Blockmania.getInstance().isSandboxed()) {
            return false;
        }

        // Generate the save directory if needed
        File dir = new File(getWorldSavePath());
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Blockmania.getInstance().getLogger().log(Level.SEVERE, "Could not create save directory.");
                return false;
            }
        }

        File f = new File(String.format("%s/Metadata.xml", getWorldSavePath()));

        try {
            f.createNewFile();
        } catch (IOException ex) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, null, ex);
        }

        Element root = new Element("World");
        Document doc = new Document(root);

        // Save the world metadata
        root.setAttribute("seed", _seed);
        root.setAttribute("title", _title);
        root.setAttribute("time", Double.toString(getTime()));

        Element spawningPoint = new Element("SpawningPoint");
        spawningPoint.setAttribute("x", Float.toString(_spawningPoint.x));
        spawningPoint.setAttribute("y", Float.toString(_spawningPoint.y));
        spawningPoint.setAttribute("z", Float.toString(_spawningPoint.z));
        root.addContent(spawningPoint);

        XMLOutputter outputter = new XMLOutputter();
        FileOutputStream output;

        try {
            output = new FileOutputStream(f);

            try {
                outputter.output(doc, output);
            } catch (IOException ex) {
                Blockmania.getInstance().getLogger().log(Level.SEVERE, null, ex);
            }

            return true;
        } catch (FileNotFoundException ex) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, null, ex);
        }


        return false;
    }

    /**
     * @return
     */
    private boolean loadMetaData() {
        if (Blockmania.getInstance().isSandboxed()) {
            return false;
        }

        File f = new File(String.format("%s/Metadata.xml", getWorldSavePath()));

        if (!f.exists())
            return false;

        try {
            SAXBuilder builder = new SAXBuilder();
            InputSource is = new InputSource(new FileInputStream(f));
            Document doc;
            try {
                doc = builder.build(is);
                Element root = doc.getRootElement();
                Element spawningPoint = root.getChild("SpawningPoint");

                _seed = root.getAttribute("seed").getValue();
                _title = root.getAttributeValue("title");
                setTime(Double.parseDouble(root.getAttributeValue("time")));
                _spawningPoint = new Vector3f(Float.parseFloat(spawningPoint.getAttributeValue("x")), Float.parseFloat(spawningPoint.getAttributeValue("y")), Float.parseFloat(spawningPoint.getAttributeValue("z")));

                return true;

            } catch (JDOMException ex) {
                Blockmania.getInstance().getLogger().log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Blockmania.getInstance().getLogger().log(Level.SEVERE, null, ex);
            }

        } catch (FileNotFoundException ex) {
            // Metadata.xml not present
        }

        return false;
    }

    public ChunkCache getChunkCache() {
        return _chunkCache;
    }

    public FastRandom getRandom() {
        return _random;
    }

    public String getTitle() {
        return _title;
    }

    public String getSeed() {
        return _seed;
    }

    public Vector3f getOrigin() {
        return new Vector3f(0, 0, 0);
    }

    public boolean isChunkVisible(Chunk c) {
        return true;
    }

    public double getTime() {
        long milliSecsSinceCreation = Blockmania.getInstance().getTime() - _creationTime;
        long wrappedTime = milliSecsSinceCreation % DAY_NIGHT_LENGTH_IN_MS;

        return (double) wrappedTime / (double) DAY_NIGHT_LENGTH_IN_MS;
    }

    public void setTime(double time) {
        _creationTime = Blockmania.getInstance().getTime() - (long) (time * DAY_NIGHT_LENGTH_IN_MS);
    }

    public Vector3f getSpawningPoint() {
        return _spawningPoint;
    }

    public double getHumidityAt(int x, int z) {
        return ((ChunkGeneratorTerrain) _chunkGenerators.get("terrain")).calcHumidityAtGlobalPosition(x, z);
    }

    public double getTemperatureAt(int x, int z) {
        return ((ChunkGeneratorTerrain) _chunkGenerators.get("terrain")).calcTemperatureAtGlobalPosition(x, z);
    }

    /*
    * Returns the active biome at the given position.
    */
    public ChunkGeneratorTerrain.BIOME_TYPE getActiveBiome(int x, int z) {
        return ((ChunkGeneratorTerrain) _chunkGenerators.get("terrain")).calcBiomeTypeForGlobalPosition(x, z);
    }
}
