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

package org.terasology.world.block.loader;

import org.terasology.asset.AssetUri;
import org.terasology.engine.API;

import javax.vecmath.Vector2f;

/**
 * @author Martin Steiger
 */
@API
public interface WorldAtlas {

    int getTileSize();

    int getAtlasSize();

    float getRelativeTileSize();

    int getNumMipmaps();

    /**
     * Obtains the tex coords of a block tile. If it isn't part of the atlas it is added to the atlas.
     *
     * @param uri         The uri of the block tile of interest.
     * @param warnOnError Whether a warning should be logged if the asset canot be found
     * @return The tex coords of the tile in the atlas.
     */
    Vector2f getTexCoords(AssetUri uri, boolean warnOnError);

}
