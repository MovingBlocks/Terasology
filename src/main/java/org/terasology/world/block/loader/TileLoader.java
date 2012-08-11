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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.terasology.asset.AssetLoader;
import org.terasology.asset.AssetUri;

/**
 * Loader for block tiles
 * @author Immortius
 */
public class TileLoader implements AssetLoader<Tile> {

    private Logger logger = Logger.getLogger(getClass().getName());
    private static final int TILE_SIZE = 16;

    public TileLoader() {
    }

    @Override
    public Tile load(InputStream stream, AssetUri uri, List<URL> urls) throws IOException {
        BufferedImage image = ImageIO.read(stream);
        if (image.getHeight() == TILE_SIZE && image.getWidth() == TILE_SIZE) {
            return new Tile(uri, image);
        }
        logger.log(Level.SEVERE, "Invalid tile '" + uri + "', tiles must be 16x16");
        return null;
    }
}