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

import org.terasology.asset.Asset;
import org.terasology.asset.AssetUri;

/**
 * @author Immortius
 */
public class Tile implements Asset {
    private AssetUri uri;
    private BufferedImage image;


    public Tile(AssetUri uri, BufferedImage image) {
        this.uri = uri;
        this.image = image;
    }

    @Override
    public AssetUri getURI() {
        return uri;
    }

    @Override
    public void dispose() {
    }

    public BufferedImage getImage() {
        return image;
    }
}
