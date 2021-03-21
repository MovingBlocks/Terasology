/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.engine.particles.components.generators;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.terasology.engine.entitySystem.Component;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.engine.rendering.assets.texture.Texture;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Generator used to choose a particle's textureOffset (from a tile-map texture)
 */
@API
public class TextureOffsetGeneratorComponent implements Component {
    public List<Vector2f> validOffsets;

    public TextureOffsetGeneratorComponent() {
        this.validOffsets = new LinkedList<>();
    }

    /**
     * @param validOffsets The offsets that the generator will choose from.
     *                     Specify the offsets relative to the size of the image.
     *                     For example: If you have a texture atlas with 6 images in a 4x2 grid, then your
     *                     offsets would be:
     *                     <ul>
     *                     <li>(0.0, 0.0) for the leftmost image on the first row</li>
     *                     <li>(0.75, 0.5) for the rightmost image on the second row</li>
     *                     <li>(0.25, 0.0) for the second image on the first row</li>
     *                     </ul
     */
    public TextureOffsetGeneratorComponent(final Vector2f[] validOffsets) {
        this.validOffsets = new LinkedList<>();
        for (Vector2f offset : validOffsets) {
            this.validOffsets.add(new Vector2f(offset));
        }
    }

    /**
     * @param atlas         The texture atlas that is being used
     * @param atlasSize     How many textures the atlas contains in it's width and height.
     * @param validTextures Indices of the valid textures
     */
    public TextureOffsetGeneratorComponent(final Texture atlas, final Vector2i atlasSize, final Vector2i[] validTextures) {
        final float textureWidth = atlas.getWidth() / (float) atlasSize.x();
        final float textureHeight = atlas.getHeight() / (float) atlasSize.y();

        Function<Vector2i, Vector2f> absolute2Relative = (absolute) -> new Vector2f(
                absolute.x() * textureWidth,
                absolute.y() * textureHeight
        );

        this.validOffsets = new LinkedList<>();
        for (Vector2i offset : validTextures) {
            this.validOffsets.add(absolute2Relative.apply(offset));
        }
    }
}
