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

package org.terasology.world.internal;

import org.joml.Vector3ic;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.ChunkView;

public interface ChunkViewCore extends ChunkView {

    /**
     * Sets the light level at the given position. the value is usually bounded by {@link org.terasology.world.chunks.ChunkConstants#MAX_LIGHT}
     *
     * @param pos   The position relative to the corner of the chunk
     * @param light set the light value of a block.
     */
    void setLight(Vector3ic pos, byte light);

    /**
     * Sets the light level at the given coordinates. the value is usually bounded by {@link org.terasology.world.chunks.ChunkConstants#MAX_LIGHT}
     *
     * @param blockX X offset from the corner of the chunk
     * @param blockY Y offset from the corner of the chunk
     * @param blockZ Z offset from the corner of the chunk
     * @param light  set the light value of a block.
     */
    void setLight(int blockX, int blockY, int blockZ, byte light);


    /**
     * Sets the sunlight level at the given position. the value is usually bounded by {@link org.terasology.world.chunks.ChunkConstants#MAX_SUNLIGHT}
     *
     * @param pos   The position relative to the corner of the chunk
     * @param light set the sunlight light value of a block.
     */
    void setSunlight(Vector3ic pos, byte light);


    /**
     * Sets the sunlight level at the given coordinates. the value is usually bounded by {@link org.terasology.world.chunks.ChunkConstants#MAX_SUNLIGHT}
     *
     * @param blockX X offset from the corner of the chunk
     * @param blockY Y offset from the corner of the chunk
     * @param blockZ Z offset from the corner of the chunk
     * @param light  set the sunlight light value of a block.
     */
    void setSunlight(int blockX, int blockY, int blockZ, byte light);

}
