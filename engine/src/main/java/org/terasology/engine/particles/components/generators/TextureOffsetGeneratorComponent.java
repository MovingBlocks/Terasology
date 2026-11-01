// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.components.generators;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.context.annotation.API;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generator used to choose a particle's textureOffset (from a tile-map texture)
 */
@API
public class TextureOffsetGeneratorComponent implements Component<TextureOffsetGeneratorComponent> {
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
     *                     </ul>
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

    @Override
    public void copyFrom(TextureOffsetGeneratorComponent other) {
        this.validOffsets = other.validOffsets.stream()
                .map(Vector2f::new)
                .collect(Collectors.toList());
    }
}
