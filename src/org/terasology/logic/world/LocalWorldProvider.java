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

import groovy.util.ConfigObject;
import org.terasology.game.Terasology;
import org.terasology.logic.generators.ChunkGeneratorTerrain;
import org.terasology.logic.generators.GeneratorManager;
import org.terasology.logic.manager.Config;
import org.terasology.logic.simulators.GrowthSimulator;
import org.terasology.logic.simulators.LiquidSimulator;
import org.terasology.math.TeraMath;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.structures.BlockPosition;
import org.terasology.persistence.PersistableObject;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Tuple3i;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Provides basic support for generating worlds.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class LocalWorldProvider extends PersistableObject implements IWorldProvider {

    /* OBSERVERS */
    private final ArrayList<IBlockObserver> _observers = new ArrayList<IBlockObserver>();

    /* WORLD GENERATION */
    protected final GeneratorManager _generatorManager;

    /* CHUNK PROVIDER */
    protected final IChunkProvider _chunkProvider;

    /* CONST */
    protected final long DAY_NIGHT_LENGTH_IN_MS = Config.getInstance().getDayNightLengthInMs();
    protected final Vector2f SPAWN_ORIGIN = Config.getInstance().getSpawnOrigin();

    /* PROPERTIES */
    protected String _title, _seed;
    protected long _creationTime = Terasology.getInstance().getTimeInMs() - Config.getInstance().getInitialTimeOffsetInMs();

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

        load();

        // Init. random generator
        _random = new FastRandom(seed.hashCode());

        _generatorManager = new GeneratorManager(this);
        _chunkProvider = new ChunkProvider(this);

        _liquidSimulator = new LiquidSimulator(this);
        _growthSimulator = new GrowthSimulator(this);
        registerObserver(_liquidSimulator);
        registerObserver(_growthSimulator);
    }

    public boolean isChunkAvailableAt(Vector3f position) {

        int chunkPosX = TeraMath.calcChunkPosX((int)position.x);
        int chunkPosZ = TeraMath.calcChunkPosZ((int)position.z);

        return _chunkProvider.isChunkAvailable(chunkPosX, 0, chunkPosZ);
    }
    
    public boolean isChunkAvailableAt(Tuple3i position) {
        int chunkPosX = TeraMath.calcChunkPosX(position.x);
        int chunkPosZ = TeraMath.calcChunkPosZ(position.z);

        return _chunkProvider.isChunkAvailable(chunkPosX, 0, chunkPosZ);
    }

    public final boolean setBlock(Tuple3i pos, byte type, boolean updateLight, boolean overwrite) {
        return setBlock(pos.x, pos.y, pos.z, type, updateLight, overwrite, false);
    }

    // TODO: A better system would be to pass through the change maker, and for generators to not work through this interface
    public final boolean setBlock(Tuple3i pos, byte type, boolean updateLight, boolean overwrite, boolean suppressUpdate) {
        return setBlock(pos.x, pos.y, pos.z, type, updateLight, overwrite, suppressUpdate);
    }

    public final boolean setBlock(int x, int y, int z, byte type, boolean updateLight, boolean overwrite) {
        return setBlock(x,y,z,type,updateLight,overwrite, false);
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
    public final boolean setBlock(int x, int y, int z, byte type, boolean updateLight, boolean overwrite, boolean suppressUpdate) {
        int chunkPosX = TeraMath.calcChunkPosX(x);
        int chunkPosZ = TeraMath.calcChunkPosZ(z);

        int blockPosX = TeraMath.calcBlockPosX(x, chunkPosX);
        int blockPosZ = TeraMath.calcBlockPosZ(z, chunkPosZ);

        Chunk c = getChunkProvider().getChunk(chunkPosX, 0, chunkPosZ);

        if (c == null) {
            return false;
        }

        if (overwrite || c.getBlock(blockPosX, y, blockPosZ) == 0x0) {
            byte oldBlock = c.getBlock(blockPosX, y, blockPosZ);
            byte newBlock;

            c.setBlock(blockPosX, y, blockPosZ, type);
            newBlock = type;

            if (!suppressUpdate) {
                if (newBlock == 0x0) {
                    notifyObserversBlockRemoved(c, new BlockPosition(x,y,z));
                } else {
                    notifyObserversBlockPlaced(c, new BlockPosition(x,y,z));
                }
            }

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

    // TODO: Send though source object hint
    private void notifyObserversBlockPlaced(Chunk chunk, BlockPosition pos) {
        for (IBlockObserver ob : _observers)
            ob.blockPlaced(chunk, pos);
    }

    private void notifyObserversBlockRemoved(Chunk chunk, BlockPosition pos) {
        for (IBlockObserver ob : _observers)
            ob.blockRemoved(chunk, pos);
    }

    /**
     * Sets the block state value at the given position.
     *
     * @param x The X-coordinate
     * @param y The Y-coordinate
     * @param z The Z-coordinate
     */
    public void setState(int x, int y, int z, byte state) {
        int chunkPosX = TeraMath.calcChunkPosX(x);
        int chunkPosZ = TeraMath.calcChunkPosZ(z);

        int blockPosX = TeraMath.calcBlockPosX(x, chunkPosX);
        int blockPosZ = TeraMath.calcBlockPosZ(z, chunkPosZ);

        Chunk c = getChunkProvider().getChunk(TeraMath.calcChunkPosX(x), 0, TeraMath.calcChunkPosZ(z));
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
        int chunkPosX = TeraMath.calcChunkPosX(x);
        int chunkPosZ = TeraMath.calcChunkPosZ(z);

        int blockPosX = TeraMath.calcBlockPosX(x, chunkPosX);
        int blockPosZ = TeraMath.calcBlockPosZ(z, chunkPosZ);

        Chunk c = getChunkProvider().getChunk(TeraMath.calcChunkPosX(x), 0, TeraMath.calcChunkPosZ(z));
        return c.getBlock(blockPosX, y, blockPosZ);
    }

    public final byte getBlock(Tuple3i pos) {
        return getBlock(pos.x, pos.y, pos.z);
    }

    public final boolean canBlockSeeTheSky(int x, int y, int z) {
        int chunkPosX = TeraMath.calcChunkPosX(x);
        int chunkPosZ = TeraMath.calcChunkPosZ(z);

        int blockPosX = TeraMath.calcBlockPosX(x, chunkPosX);
        int blockPosZ = TeraMath.calcBlockPosZ(z, chunkPosZ);

        Chunk c = getChunkProvider().getChunk(TeraMath.calcChunkPosX(x), 0, TeraMath.calcChunkPosZ(z));
        return c.canBlockSeeTheSky(blockPosX, y, blockPosZ);
    }

    public byte getState(int x, int y, int z) {
        int chunkPosX = TeraMath.calcChunkPosX(x);
        int chunkPosZ = TeraMath.calcChunkPosZ(z);

        int blockPosX = TeraMath.calcBlockPosX(x, chunkPosX);
        int blockPosZ = TeraMath.calcBlockPosZ(z, chunkPosZ);

        Chunk c = getChunkProvider().getChunk(TeraMath.calcChunkPosX(x), 0, TeraMath.calcChunkPosZ(z));
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
        int chunkPosX = TeraMath.calcChunkPosX(x);
        int chunkPosZ = TeraMath.calcChunkPosZ(z);

        int blockPosX = TeraMath.calcBlockPosX(x, chunkPosX);
        int blockPosZ = TeraMath.calcBlockPosZ(z, chunkPosZ);

        Chunk c = getChunkProvider().getChunk(TeraMath.calcChunkPosX(x), 0, TeraMath.calcChunkPosZ(z));
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
        int chunkPosX = TeraMath.calcChunkPosX(x);
        int chunkPosZ = TeraMath.calcChunkPosZ(z);

        int blockPosX = TeraMath.calcBlockPosX(x, chunkPosX);
        int blockPosZ = TeraMath.calcBlockPosZ(z, chunkPosZ);

        Chunk c = getChunkProvider().getChunk(TeraMath.calcChunkPosX(x), 0, TeraMath.calcChunkPosZ(z));
        c.setLight(blockPosX, y, blockPosZ, intensity, type);
    }

    /**
     * Finds a spawning point.
     *
     * @return The spawning point.
     */
    public Vector3d nextSpawningPoint() {
        ChunkGeneratorTerrain tGen = ((ChunkGeneratorTerrain) getGeneratorManager().getChunkGenerators().get(0));

        FastRandom nRandom = new FastRandom(Terasology.getInstance().getTimeInMs());

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
        super.dispose();
        getChunkProvider().dispose();
    }

    public void registerObserver(IBlockObserver observer) {
        _observers.add(observer);
    }

    public void unregisterObserver(IBlockObserver observer) {
        _observers.remove(observer);
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
        int chunkPosX = TeraMath.calcChunkPosX(x);
        int chunkPosZ = TeraMath.calcChunkPosZ(z);

        int blockPosX = TeraMath.calcBlockPosX(x, chunkPosX);
        int blockPosZ = TeraMath.calcBlockPosZ(z, chunkPosZ);

        Chunk c = getChunkProvider().getChunk(TeraMath.calcChunkPosX(x), 0, TeraMath.calcChunkPosZ(z));
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
        int chunkPosX = TeraMath.calcChunkPosX(x);
        int chunkPosZ = TeraMath.calcChunkPosZ(z);

        int blockPosX = TeraMath.calcBlockPosX(x, chunkPosX);
        int blockPosZ = TeraMath.calcBlockPosZ(z, chunkPosZ);

        Chunk c = getChunkProvider().getChunk(TeraMath.calcChunkPosX(x), 0, TeraMath.calcChunkPosZ(z));
        c.spreadLight(blockPosX, y, blockPosZ, lightValue, depth, type);
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


    public void setTime(double time) {
        if (time >= 0.0)
            _creationTime = Terasology.getInstance().getTimeInMs() - (long) (time * DAY_NIGHT_LENGTH_IN_MS);
    }

    public double getTime() {
        long msSinceCreation = Terasology.getInstance().getTimeInMs() - _creationTime;
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
     * Returns the world save path, including the world's name. Will try to detect and fix quirky path issues (applet thing)
     *
     * @return path to save stuff at
     */
    @Override
    public String getObjectSavePath() {
        return Terasology.getInstance().getWorldSavePath(_title);
    }

    @Override
    public String getObjectFileName() {
        return "WorldManifest.groovy";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writePropertiesToConfigObject(ConfigObject co) {
        co.put("worldTitle", getTitle());
        co.put("worldSeed", getSeed());
        co.put("worldTime", getTime());
    }

    @Override
    public void readPropertiesFromConfigObject(ConfigObject co) {
        _title = (String) co.get("worldTitle");
        _seed = (String) co.get("worldSeed");

        Double time = ((BigDecimal) co.get("worldTime")).doubleValue();

        if (time != null)
            setTime(time);
    }
}
