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

import com.github.begla.blockmania.datastructures.BlockPosition;
import com.github.begla.blockmania.generators.ChunkGeneratorTerrain;
import com.github.begla.blockmania.utilities.FastRandom;
import com.github.begla.blockmania.world.chunk.Chunk;
import com.github.begla.blockmania.world.chunk.ChunkProvider;
import javolution.util.FastList;
import org.lwjgl.util.vector.Vector3f;

/**
 * Provides the basic interface for all world providers.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class WorldProvider {

    /* WORLD OBSERVERS */
    private FastList<WorldProviderObserver> _observers = new FastList<WorldProviderObserver>();

    /**
     * Places a block of a specific type at a given position and refreshes the
     * corresponding light values.
     *
     * @param x           The X-coordinate
     * @param y           The Y-coordinate
     * @param z           The Z-coordinate
     * @param type        The type of the block to set
     * @param updateLight If set the affected chunk is queued for updating
     * @param overwrite   If true currently present blocks get replaced
     * @return True if a block was set/replaced
     */
    public abstract boolean setBlock(int x, int y, int z, byte type, boolean updateLight, boolean overwrite);

    /**
     * Returns the block at the given position.
     *
     * @param x The X-coordinate
     * @param y The Y-coordinate
     * @param z The Z-coordinate
     * @return The type of the block
     */
    public abstract byte getBlock(int x, int y, int z);

    /**
     * Returns the light value at the given position.
     *
     * @param x    The X-coordinate
     * @param y    The Y-coordinate
     * @param z    The Z-coordinate
     * @param type The type of light
     * @return The light value
     */
    public abstract byte getLight(int x, int y, int z, Chunk.LIGHT_TYPE type);

    /**
     * Sets the light value at the given position.
     *
     * @param x         The X-coordinate
     * @param y         The Y-coordinate
     * @param z         The Z-coordinate
     * @param intensity The light intensity value
     * @param type      The type of light
     */
    public abstract void setLight(int x, int y, int z, byte intensity, Chunk.LIGHT_TYPE type);

    /**
     * Returns the block value at the given position.
     *
     * @param pos The position
     * @return The block value at the given position
     */
    public abstract byte getBlockAtPosition(Vector3f pos);


    /**
     * Returns the light value at the given position.
     *
     * @param pos  The position
     * @param type The type of light
     * @return The block value at the given position
     */
    public abstract byte getLightAtPosition(Vector3f pos, Chunk.LIGHT_TYPE type);


    /**
     * Returns the humidity at the given position.
     *
     * @param x The X-coordinate
     * @param z The Z-coordinate
     * @return The humidity
     */
    public abstract double getHumidityAt(int x, int z);

    /**
     * Returns the temperature at the given position.
     *
     * @param x The X-coordinate
     * @param z The Z-coordinate
     * @return The temperature
     */
    public abstract double getTemperatureAt(int x, int z);

    /*
    * Returns the biome type at the given position.
    */
    public abstract ChunkGeneratorTerrain.BIOME_TYPE getActiveBiome(int x, int z);

    /**
     * Returns the current time.
     *
     * @return The current time
     */
    public abstract double getTime();

    /**
     * Sets the current time of the world.
     *
     * @param time The current time
     */
    public abstract void setTime(double time);

    /**
     * Returns the title of this world.
     *
     * @return
     */
    public abstract String getTitle();

    /**
     * Returns the seed of this world.
     *
     * @return The seed value
     */
    public abstract String getSeed();

    /**
     * Returns the chunk provider of this world.
     *
     * @return The chunk provider
     */
    public abstract ChunkProvider getChunkProvider();

    /**
     * Returns a new random spawning point.
     *
     * @return A new random spawning point
     */
    public abstract Vector3f nextSpawningPoint();

    /**
     * Disposes this world provider.
     */
    public abstract void dispose();

    /**
     * Returns the RNG for this world provider.
     *
     * @return The RNG
     */
    public abstract FastRandom getRandom();

    /**
     * @return The rendering origin
     */
    public abstract Vector3f getRenderingOrigin();


    public void registerObserver(WorldProviderObserver observer) {
        _observers.add(observer);
    }

    public void unregisterObserver(WorldProviderObserver observer) {
        _observers.remove(observer);
    }

    public void notifyObserversBlockChanged(Chunk chunk, BlockPosition pos) {
        for (WorldProviderObserver ob : _observers)
            ob.blockChanged(chunk, pos);
    }

    public void notifyObserversLightChanged(Chunk chunk, BlockPosition pos) {
        for (WorldProviderObserver ob : _observers)
            ob.lightChanged(chunk, pos);
    }
}
