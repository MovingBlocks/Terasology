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
package org.terasology.world.block.tiles;

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
