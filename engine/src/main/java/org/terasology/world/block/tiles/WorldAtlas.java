/*
 * Copyright 2014 MovingBlocks
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

import org.terasology.assets.ResourceUrn;
import org.terasology.math.geom.Vector2f;
import org.terasology.module.sandbox.API;

/**
 */
@API
public interface WorldAtlas {

    void update();

    void dispose();

    int getTileSize();

    int getAtlasSize();

    float getRelativeTileSize();

    int getNumMipmaps();

    /**
     * Obtains the tex coords of a block tile. If it isn't part of the atlas it is added to the atlas.
     *
     * @param tile        The block tile of interest.
     * @param warnOnError Whether a warning should be logged if the asset cannot be found
     * @return The tex coords of the tile in the atlas.
     */
    Vector2f getTexCoords(BlockTile tile, boolean warnOnError);

    Vector2f getTexCoords(ResourceUrn uri, boolean warnOnError);

}
