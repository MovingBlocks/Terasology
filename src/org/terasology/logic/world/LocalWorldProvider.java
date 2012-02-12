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
package org.terasology.logic.world;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.terasology.game.Terasology;
import org.terasology.logic.generators.ChunkGeneratorTerrain;
import org.terasology.logic.generators.GeneratorManager;
import org.terasology.logic.manager.ConfigurationManager;
import org.terasology.logic.simulators.GrowthSimulator;
import org.terasology.logic.simulators.LiquidSimulator;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.utilities.FastRandom;
import org.terasology.utilities.MathHelper;
import org.xml.sax.InputSource;

import javax.vecmath.Tuple3i;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3d;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Provides basic support for generating worlds.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class LocalWorldProvider implements IWorldProvider {

    /* WORLD GENERATION */
    protected final GeneratorManager _generatorManager;

    /* CHUNK PROVIDER */
    protected final IChunkProvider _chunkProvider;

    /* CONST */
    protected final long DAY_NIGHT_LENGTH_IN_MS = (Long) ConfigurationManager.getInstance().getConfig().get("World.dayNightLengthInMs");
    protected final Vector2f SPAWN_ORIGIN = (Vector2f) ConfigurationManager.getInstance().getConfig().get("World.spawnOrigin");

    /* PROPERTIES */
    protected String _title, _seed;
    protected long _creationTime = Terasology.getInstance().getTime() - (Long) ConfigurationManager.getInstance().getConfig().get("World.initialTimeOffsetInMs");

    /* SIMULATORS */
    private final LiquidSimulator _liquidSimulator;
    private final GrowthSimulator _growthSimulator;

    /* RANDOMNESS */
    protected final FastRandom _random;

    /**
     * Initializes a new world.
     *
     * @param title The title/description of the world
     * @param seed  The seed string used to generate the terrain
     */
    public LocalWorldProvider(String title, String seed) {

        if (seed == null) {
            throw new IllegalArgumentException("No seed provided.");
        } else if (seed.isEmpty()) {
            throw new IllegalArgumentException("Empty seed provided.");
        }

        if (title == null) {
            title = seed;
        } else if (title.isEmpty()) {
            title = seed;
        }

        _title = title;
        _seed = seed;

        // Init. random generator
        _random = new FastRandom(seed.hashCode());

        // Load the meta data of this world
        loadMetaData();

        _generatorManager = new GeneratorManager(this);
        _chunkProvider = new LocalChunkCache(this);

        _liquidSimulator = new LiquidSimulator(this);
        _growthSimulator = new GrowthSimulator(this);
    }

    /**
     * Places a block of a specific type at a given position and optionally refreshes the
     * corresponding light values.
     *
     * @param x         The X-coordinate
     * @param y         The Y-coordinate
     * @param z         The Z-coordinate
     * @param type      The type of the block to set
     * @param overwrite If true currently present blocks get replaced
     * @return True if a block was set/replaced
     */
    public final boolean setBlock(int x, int y, int z, byte type, boolean updateLight, boolean overwrite) {
        int chunkPosX = MathHelper.calcChunkPosX(x);
        int chunkPosZ = MathHelper.calcChunkPosZ(z);

        int blockPosX = MathHelper.calcBlockPosX(x, chunkPosX);
        int blockPosZ = MathHelper.calcBlockPosZ(z, chunkPosZ);

        Chunk c = getChunkProvider().loadOrCreateChunk(chunkPosX, chunkPosZ);

        if (c == null) {
            return false;
        }

        if (overwrite || c.getBlock(blockPosX, y, blockPosZ) == 0x0) {
            byte oldBlock = c.getBlock(blockPosX, y, blockPosZ);
            byte newBlock;

            c.setBlock(blockPosX, y, blockPosZ, type);
            newBlock = type;

            if (updateLight) {
                /*
                * Update sunlight.
                */
                c.refreshSunlightAtLocalPos(blockPosX, blockPosZ, true, true);

                byte blockLightPrev = getLight(x, y, z, Chunk.LIGHT_TYPE.BLOCK);
                byte blockLightCurrent;

                // New block placed
                if (oldBlock == 0x0 && newBlock != 0x0) {
                    /*
                    * Spread light of block light sources.
                    */
                    byte luminance = BlockManager.getInstance().getBlock(type).getLuminance();

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
     * Sets the block state value at the given position.
     *
     * @param x The X-coordinate
     * @param y The Y-coordinate
     * @param z The Z-coordinate
     */
    public void setState(int x, int y, int z, byte state) {
        int chunkPosX = MathHelper.calcChunkPosX(x);
        int chunkPosZ = MathHelper.calcChunkPosZ(z);

        int blockPosX = MathHelper.calcBlockPosX(x, chunkPosX);
        int blockPosZ = MathHelper.calcBlockPosZ(z, chunkPosZ);

        Chunk c = getChunkProvider().loadOrCreateChunk(MathHelper.calcChunkPosX(x), MathHelper.calcChunkPosZ(z));
        c.setState(blockPosX, y, blockPosZ, state);
    }


    /**
     * Returns the block value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    public final byte getBlockAtPosition(Vector3d pos) {
        return getBlock((int) (pos.x + ((pos.x >= 0) ? 0.5f : -0.5f)), (int) (pos.y + ((pos.y >= 0) ? 0.5f : -0.5f)), (int) (pos.z + ((pos.z >= 0) ? 0.5f : -0.5f)));
    }

    public final byte getBlockAtPosition(double x, double y, double z) {
        return getBlock((int) (x + ((x >= 0) ? 0.5f : -0.5f)), (int) (y + ((y >= 0) ? 0.5f : -0.5f)), (int) (z + ((z >= 0) ? 0.5f : -0.5f)));
    }

    /**
     * Returns the light value at the given position.
     *
     * @param pos  The position
     * @param type The type of light
     * @return The block value at the given position
     */
    public final byte getLightAtPosition(Vector3d pos, Chunk.LIGHT_TYPE type) {
        return getLight((int) (pos.x + ((pos.x >= 0) ? 0.5f : -0.5f)), (int) (pos.y + ((pos.y >= 0) ? 0.5f : -0.5f)), (int) (pos.z + ((pos.z >= 0) ? 0.5f : -0.5f)), type);
    }

    public final byte getLightAtPosition(double x, double y, double z, Chunk.LIGHT_TYPE type) {
        return getLight((int) (x + ((x >= 0) ? 0.5f : -0.5f)), (int) (y + ((y >= 0) ? 0.5f : -0.5f)), (int) (z + ((z >= 0) ? 0.5f : -0.5f)), type);
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
        int chunkPosX = MathHelper.calcChunkPosX(x);
        int chunkPosZ = MathHelper.calcChunkPosZ(z);

        int blockPosX = MathHelper.calcBlockPosX(x, chunkPosX);
        int blockPosZ = MathHelper.calcBlockPosZ(z, chunkPosZ);

        Chunk c = getChunkProvider().loadOrCreateChunk(MathHelper.calcChunkPosX(x), MathHelper.calcChunkPosZ(z));
        return c.getBlock(blockPosX, y, blockPosZ);
    }
    
    public final byte getBlock(Tuple3i pos)
    {
        return getBlock(pos.x, pos.y, pos.z);
    }

    public final boolean canBlockSeeTheSky(int x, int y, int z) {
        int chunkPosX = MathHelper.calcChunkPosX(x);
        int chunkPosZ = MathHelper.calcChunkPosZ(z);

        int blockPosX = MathHelper.calcBlockPosX(x, chunkPosX);
        int blockPosZ = MathHelper.calcBlockPosZ(z, chunkPosZ);

        Chunk c = getChunkProvider().loadOrCreateChunk(MathHelper.calcChunkPosX(x), MathHelper.calcChunkPosZ(z));
        return c.canBlockSeeTheSky(blockPosX, y, blockPosZ);
    }

    public byte getState(int x, int y, int z) {
        int chunkPosX = MathHelper.calcChunkPosX(x);
        int chunkPosZ = MathHelper.calcChunkPosZ(z);

        int blockPosX = MathHelper.calcBlockPosX(x, chunkPosX);
        int blockPosZ = MathHelper.calcBlockPosZ(z, chunkPosZ);

        Chunk c = getChunkProvider().loadOrCreateChunk(MathHelper.calcChunkPosX(x), MathHelper.calcChunkPosZ(z));
        return c.getState(blockPosX, y, blockPosZ);
    }

    /**
     * Returns the light value at the given position.
     *
     * @param x    The X-coordinate
     * @param y    The Y-coordinate
     * @param z    The Z-coordinate
     * @param type The type of light
     * @return The light value
     */
    public final byte getLight(int x, int y, int z, Chunk.LIGHT_TYPE type) {
        int chunkPosX = MathHelper.calcChunkPosX(x);
        int chunkPosZ = MathHelper.calcChunkPosZ(z);

        int blockPosX = MathHelper.calcBlockPosX(x, chunkPosX);
        int blockPosZ = MathHelper.calcBlockPosZ(z, chunkPosZ);

        Chunk c = getChunkProvider().loadOrCreateChunk(MathHelper.calcChunkPosX(x), MathHelper.calcChunkPosZ(z));
        return c.getLight(blockPosX, y, blockPosZ, type);
    }

    /**
     * Sets the light value at the given position.
     *
     * @param x         The X-coordinate
     * @param y         The Y-coordinate
     * @param z         The Z-coordinate
     * @param intensity The light intensity value
     * @param type      The type of light
     */
    public void setLight(int x, int y, int z, byte intensity, Chunk.LIGHT_TYPE type) {
        int chunkPosX = MathHelper.calcChunkPosX(x);
        int chunkPosZ = MathHelper.calcChunkPosZ(z);

        int blockPosX = MathHelper.calcBlockPosX(x, chunkPosX);
        int blockPosZ = MathHelper.calcBlockPosZ(z, chunkPosZ);

        Chunk c = getChunkProvider().loadOrCreateChunk(MathHelper.calcChunkPosX(x), MathHelper.calcChunkPosZ(z));
        c.setLight(blockPosX, y, blockPosZ, intensity, type);
    }

    /**
     * Finds a spawning point.
     *
     * @return The spawning point.
     */
    public Vector3d nextSpawningPoint() {
        ChunkGeneratorTerrain tGen = ((ChunkGeneratorTerrain) getGeneratorManager().getChunkGenerators().get(0));

        FastRandom nRandom = new FastRandom(Terasology.getInstance().getTime());

        for (; ; ) {
            int randX = (int) (nRandom.randomDouble() * 128f);
            int randZ = (int) (nRandom.randomDouble() * 128f);

            for (int y = Chunk.CHUNK_DIMENSION_Y - 1; y >= 32; y--) {

                double dens = tGen.calcDensity(randX + (int) SPAWN_ORIGIN.x, y, randZ + (int) SPAWN_ORIGIN.y);

                if (dens >= 0 && y < 64)
                    return new Vector3d(randX + SPAWN_ORIGIN.x, y, randZ + SPAWN_ORIGIN.y);
                else if (dens >= 0 && y >= 64)
                    break;
            }
        }
    }

    public void dispose() {
        Terasology.getInstance().getLogger().log(Level.INFO, "Disposing local world \"{0}\" and saving all chunks.", getTitle());

        saveMetaData();
        getChunkProvider().dispose();
    }

    /**
     * Returns the humidity at the given position.
     *
     * @param x The X-coordinate
     * @param z The Z-coordinate
     * @return The humidity
     */

    public double getHumidityAt(int x, int z) {
        return ((ChunkGeneratorTerrain) getGeneratorManager().getChunkGenerators().get(0)).calcHumidityAtGlobalPosition(x, z);
    }

    /**
     * Returns the temperature at the given position.
     *
     * @param x The X-coordinate
     * @param z The Z-coordinate
     * @return The temperature
     */
    public double getTemperatureAt(int x, int z) {
        return ((ChunkGeneratorTerrain) getGeneratorManager().getChunkGenerators().get(0)).calcTemperatureAtGlobalPosition(x, z);
    }

    /*
    * Returns the biome type at the given position.
    */
    public ChunkGeneratorTerrain.BIOME_TYPE getActiveBiome(int x, int z) {
        return ((ChunkGeneratorTerrain) getGeneratorManager().getChunkGenerators().get(0)).calcBiomeTypeForGlobalPosition(x, z);
    }

    /**
     * Returns the world save path, including the world's name. Will try to detect and fix quirky path issues (applet thing)
     * @return path to save stuff at
     */
    public String getWorldSavePath() {
        String path = String.format("SAVED_WORLDS/%s", _title);
        // Try to detect if we're getting a screwy save path (usually/always the case with an applet)
        File f = new File(path);
        //System.out.println("Suggested absolute save path is: " + f.getAbsolutePath());
        if (!f.getAbsolutePath().contains("Terasology")) {
            f = new File(System.getProperty("java.io.tmpdir"), path);
            //System.out.println("Absolute TEMP save path is: " + f.getAbsolutePath());
            return f.getAbsolutePath();
        }
        return path;
    }

    public void setTime(double time) {
        _creationTime = Terasology.getInstance().getTime() - (long) (time * DAY_NIGHT_LENGTH_IN_MS);
    }

    public double getTime() {
        long msSinceCreation = Terasology.getInstance().getTime() - _creationTime;
        return (double) msSinceCreation / (double) DAY_NIGHT_LENGTH_IN_MS;
    }

    public IChunkProvider getChunkProvider() {
        return _chunkProvider;
    }

    public GeneratorManager getGeneratorManager() {
        return _generatorManager;
    }

    public LiquidSimulator getLiquidSimulator() {
        return _liquidSimulator;
    }

    public GrowthSimulator getGrowthSimulator() {
        return _growthSimulator;
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

    /**
     * Saves the meta data of this world.
     *
     * @return True if saving was successful
     */
    public boolean saveMetaData() {
        // Generate the save directory if needed
        File dir = new File(getWorldSavePath());
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Terasology.getInstance().getLogger().log(Level.SEVERE, "Could not create save directory.");
                return false;
            }
        }

        File f = new File(String.format("%s/Metadata.xml", getWorldSavePath()));

        Element root = new Element("World");
        Document doc = new Document(root);

        // Save the world metadata
        root.setAttribute("seed", _seed);
        root.setAttribute("title", _title);
        root.setAttribute("time", Double.toString(getTime()));

        XMLOutputter outputter = new XMLOutputter();
        FileOutputStream output;

        try {
            output = new FileOutputStream(f);
            outputter.output(doc, output);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Loads the meta data of this world.
     *
     * @return True if loading was successful
     */
    private boolean loadMetaData() {
        File f = new File(String.format("%s/Metadata.xml", getWorldSavePath()));

        if (!f.exists())
            return false;

        try {
            SAXBuilder builder = new SAXBuilder();
            InputSource is = new InputSource(new FileInputStream(f));
            Document doc = builder.build(is);
            Element root = doc.getRootElement();

            _seed = root.getAttribute("seed").getValue();
            _title = root.getAttributeValue("title");
            setTime(Double.parseDouble(root.getAttributeValue("time")));

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * "Unspreads" light recursively.
     *
     * @param x           The X-coordinate
     * @param y           The Y-coordinate
     * @param z           The Z-coordinate
     * @param lightValue  The initial light value
     * @param depth       The current depth of the recursion
     * @param type        The type of light
     * @param brightSpots List of bright spots found while unspreading the light
     */
    public void unspreadLight(int x, int y, int z, byte lightValue, int depth, Chunk.LIGHT_TYPE type, ArrayList<Vector3d> brightSpots) {
        int chunkPosX = MathHelper.calcChunkPosX(x);
        int chunkPosZ = MathHelper.calcChunkPosZ(z);

        int blockPosX = MathHelper.calcBlockPosX(x, chunkPosX);
        int blockPosZ = MathHelper.calcBlockPosZ(z, chunkPosZ);

        Chunk c = getChunkProvider().loadOrCreateChunk(MathHelper.calcChunkPosX(x), MathHelper.calcChunkPosZ(z));
        c.unspreadLight(blockPosX, y, blockPosZ, lightValue, depth, type, brightSpots);
    }

    /**
     * Propagates light recursively.
     *
     * @param x          The X-coordinate
     * @param y          The Y-coordinate
     * @param z          The Z-coordinate
     * @param lightValue The initial light value
     * @param depth      The current depth of the recursion
     * @param type       The type of light
     */
    public void spreadLight(int x, int y, int z, byte lightValue, int depth, Chunk.LIGHT_TYPE type) {
        int chunkPosX = MathHelper.calcChunkPosX(x);
        int chunkPosZ = MathHelper.calcChunkPosZ(z);

        int blockPosX = MathHelper.calcBlockPosX(x, chunkPosX);
        int blockPosZ = MathHelper.calcBlockPosZ(z, chunkPosZ);

        Chunk c = getChunkProvider().loadOrCreateChunk(MathHelper.calcChunkPosX(x), MathHelper.calcChunkPosZ(z));
        c.spreadLight(blockPosX, y, blockPosZ, lightValue, depth, type);
    }
}
