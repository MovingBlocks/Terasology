// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.block.tiles;

import org.joml.Vector2f;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.context.annotation.API;

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
