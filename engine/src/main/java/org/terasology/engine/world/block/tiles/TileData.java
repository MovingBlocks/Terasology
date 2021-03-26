// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.tiles;

import org.terasology.assets.AssetData;

import java.awt.image.BufferedImage;

/**
 */
public class TileData implements AssetData {
    private BufferedImage[] images;
    private boolean autoBlock;

    public TileData(BufferedImage[] images, boolean autoBlock) {
        this.images = images;
        this.autoBlock = autoBlock;
    }

    public BufferedImage[] getImages() {
        return images;
    }

    public boolean isAutoBlock() {
        return autoBlock;
    }
}
