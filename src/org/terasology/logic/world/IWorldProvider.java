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

import org.terasology.logic.generators.ChunkGeneratorTerrain;
import org.terasology.logic.generators.GeneratorManager;
import org.terasology.logic.simulators.GrowthSimulator;
import org.terasology.logic.simulators.LiquidSimulator;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Vector3d;

/**
 * Provides the basic interface for all world providers.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public interface IWorldProvider {

    /**
     * Places a block of a specific type at a given position and refreshes the
     * corresponding light values.
     *
     * @param x           The X-coordinate
     * @param y           The Y-coordinate
     * @param z           The Z-coordinate
     * @param type        The type of the block to set
     * @param updateLight Update light values
     * @param overwrite   If true currently present blocks get replaced
     * @return True if a block was set/replaced
     */
    public boolean setBlock(int x, int y, int z, byte type, boolean updateLight, boolean overwrite);

    /**
     * Sets the given state at the given position.
     *
     * @param x     The X-coordinate
     * @param y     The Y-coordinate
     * @param z     The Z-coordinate
     * @param state The state value
     */
    public void setState(int x, int y, int z, byte state);

    /**
     * Returns the block at the given position.
     *
     * @param x The X-coordinate
     * @param y The Y-coordinate
     * @param z The Z-coordinate
     * @return The type of the block
     */
    public byte getBlock(int x, int y, int z);

    public boolean canBlockSeeTheSky(int x, int y, int z);

    /**
     * Returns the state at the given position.
     *
     * @param x The X-coordinate
     * @param y The Y-coordinate
     * @param z The Z-coordinate
     * @return The type of the block
     */
    public byte getState(int x, int y, int z);

    /**
     * Returns the light value at the given position.
     *
     * @param x    The X-coordinate
     * @param y    The Y-coordinate
     * @param z    The Z-coordinate
     * @param type The type of light
     * @return The light value
     */
    public byte getLight(int x, int y, int z, Chunk.LIGHT_TYPE type);

    /**
     * Sets the light value at the given position.
     *
     * @param x         The X-coordinate
     * @param y         The Y-coordinate
     * @param z         The Z-coordinate
     * @param intensity The light intensity value
     * @param type      The type of light
     */
    public void setLight(int x, int y, int z, byte intensity, Chunk.LIGHT_TYPE type);

    /**
     * Returns the block value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    public byte getBlockAtPosition(Vector3d pos);


    /**
     * Returns the light value at the given position.
     *
     * @param pos  The position
     * @param type The type of light
     * @return The block value at the given position
     */
    public byte getLightAtPosition(Vector3d pos, Chunk.LIGHT_TYPE type);


    /**
     * Returns the humidity at the given position.
     *
     * @param x The X-coordinate
     * @param z The Z-coordinate
     * @return The humidity
     */
    public double getHumidityAt(int x, int z);

    /**
     * Returns the temperature at the given position.
     *
     * @param x The X-coordinate
     * @param z The Z-coordinate
     * @return The temperature
     */
    public double getTemperatureAt(int x, int z);

    /*
    * Returns the biome type at the given position.
    */
    public ChunkGeneratorTerrain.BIOME_TYPE getActiveBiome(int x, int z);

    /**
     * Returns the current time.
     *
     * @return The current time
     */
    public double getTime();

    /**
     * Sets the current time of the world.
     *
     * @param time The current time
     */
    public void setTime(double time);

    /**
     * Returns the title of this world.
     *
     * @return
     */
    public String getTitle();

    /**
     * Returns the seed of this world.
     *
     * @return The seed value
     */
    public String getSeed();

    /**
     * Returns the chunk provider of this world.
     *
     * @return The chunk provider
     */
    public IChunkProvider getChunkProvider();

    /**
     * Returns the generator manager of this world.
     *
     * @return The generator manager
     */
    public GeneratorManager getGeneratorManager();

    /**
     * Returns the liquid simulator.
     *
     * @return The liquid simulator
     */
    public LiquidSimulator getLiquidSimulator();

    /**
     * Returns the growth simulator.
     *
     * @return The growth simulator
     */
    public GrowthSimulator getGrowthSimulator();

    /**
     * Returns a new random spawning point.
     *
     * @return A new random spawning point
     */
    public Vector3d nextSpawningPoint();

    /**
     * Returns the RNG for this world provider.
     *
     * @return The RNG
     */
    public FastRandom getRandom();

    /**
     * Disposes this world provider.
     */
    public void dispose();
}
