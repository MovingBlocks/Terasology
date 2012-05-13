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

/**
 * Basic interface for anything providing chunks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public interface IChunkProvider {

    /**
     * @param x
     * @param z
     * @return Whether this chunk is immediately available
     */
    public boolean isChunkAvailable(int x, int y, int z);

    /**
     * Returns the chunk at the given position.
     *
     * @param x The chunk position on the x-axis
     * @param z The chunk position on the z-axis
     * @return The chunk
     */
    public Chunk getChunk(int x, int y, int z);

    // TODO: Need some flavour of blocking, non-blocking, etc getChunk, for different circumstances

    /**
     * Frees obsolete chunks.
     */
    public void flushCache();

    /**
     * Disposes all chunks managed by this chunk provider.
     */
    public void dispose();

    /**
     * Returns the amount of chunks managed by this chunk provider.
     *
     * @return The amount of managed chunks
     */
    public float size();
}
