// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.block.tiles;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.math.geom.Vector2f;

/**
 * Dummy implementation of WorldAtlas
 */
public class NullWorldAtlas implements WorldAtlas {

    @Override
    public void update() {
    }

    @Override
    public void dispose() {
    }

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
    public Vector2f getTexCoords(BlockTile tile, boolean warnOnError) {
        return new Vector2f();
    }

    @Override
    public Vector2f getTexCoords(ResourceUrn uri, boolean warnOnError) {
        return new Vector2f();
    }


}
