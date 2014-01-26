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

import javax.vecmath.Vector2f;

import org.terasology.asset.AssetUri;

/**
 * Dummy implementation of WorldAtlas
 * @author Martin Steiger
 */
public class NullWorldAtlas implements WorldAtlas {

    @Override
    public int getTileSize() {
        return 0;
    }

    @Override
    public int getAtlasSize() {
        return 0;
    }

    @Override
    public float getRelativeTileSize() {
        return 0;
    }

    @Override
    public int getNumMipmaps() {
        return 0;
    }

    @Override
    public Vector2f getTexCoords(AssetUri uri, boolean warnOnError) {
        return new Vector2f();
    }

    
}
