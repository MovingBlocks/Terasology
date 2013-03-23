/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetLoader;
import org.terasology.asset.AssetUri;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Loader for block tiles
 *
 * @author Immortius
 */
public class TileLoader implements AssetLoader<Tile> {

    private static final int TILE_SIZE = 16;

    private static final Logger logger = LoggerFactory.getLogger(TileLoader.class);

    public TileLoader() {
    }

    @Override
    public Tile load(AssetUri uri, InputStream stream, List<URL> urls) throws IOException {
        BufferedImage image = ImageIO.read(stream);
        if (image.getHeight() == TILE_SIZE && image.getWidth() == TILE_SIZE) {
            return new Tile(uri, image);
        }
        logger.error("Invalid tile '{}', tiles must be 16x16", uri);
        return null;
    }
}
