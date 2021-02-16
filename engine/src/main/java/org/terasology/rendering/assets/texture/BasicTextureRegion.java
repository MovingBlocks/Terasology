// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.assets.texture;

import org.joml.RoundingMode;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.math.TeraMath;

/**
 */
public class BasicTextureRegion implements TextureRegion {
    private Texture texture;
    private Rectanglef region = new Rectanglef();

    public BasicTextureRegion(Texture texture, Rectanglef region) {
        this.texture = texture;
        this.region.set(region);
    }

    public BasicTextureRegion(Texture texture, Vector2fc offset, Vector2fc size) {
        this(texture, new Rectanglef().setMin(offset).setSize(size));
    }

    @Override
    public Texture getTexture() {
        return texture;
    }

    @Override
    public Rectanglef getRegion() {
        return region;
    }

    @Override
    public int getWidth() {
        return TeraMath.ceilToInt(texture.getWidth() * region.lengthX());
    }

    @Override
    public int getHeight() {
        return TeraMath.ceilToInt(texture.getHeight() * region.lengthY());
    }

    @Override
    public Vector2i size() {
        return new Vector2i(getWidth(), getHeight());
    }

    @Override
    public Rectanglei getPixelRegion() {
        Vector2i min = new Vector2i(region.minX * texture.getWidth(), region.minY * texture.getHeight(), RoundingMode.FLOOR);
        return new Rectanglei(min).setSize(getWidth(), getHeight());
    }
}
