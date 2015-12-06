/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.assets.texture;

import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;

/**
 * Interface for any asset that describes a region of a texture that can be rendered (can include textures themselves).
 *
 */
public interface TextureRegion {

    Texture getTexture();

    /**
     * @return The region of the texture represented by this asset
     */
    Rect2f getRegion();

    /**
     * @return The pixel region of the texture represented by this asset
     */
    Rect2i getPixelRegion();

    int getWidth();

    int getHeight();

    Vector2i size();
}
