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

import com.google.common.math.IntMath;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.format.AbstractAssetFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetFileFormat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Loader for block tiles
 *
 */
@RegisterAssetFileFormat
public class TileFormat extends AbstractAssetFileFormat<TileData> {

    public TileFormat() {
        super("png");
    }

    @Override
    public TileData load(ResourceUrn resourceUrn, List<AssetDataFile> list) throws IOException {
        boolean auto = list.get(0).getPath().contains("auto");
        try (InputStream stream = list.get(0).openStream()) {
            BufferedImage image = ImageIO.read(stream);
            if (!IntMath.isPowerOfTwo(image.getHeight()) || image.getWidth() % image.getHeight() != 0 || image.getWidth() == 0) {
                throw new IOException("Invalid tile - must be horizontal row of power-of-two sized squares");
            }
            BufferedImage[] frames = new BufferedImage[image.getWidth()/image.getHeight()];
            for (int i=0; i<frames.length; i++) {
                frames[i] = new BufferedImage(image.getHeight(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
                frames[i].createGraphics().drawImage(image, -image.getHeight() * i, 0, null);
            }
            return new TileData(frames, auto);
        }
    }

}
