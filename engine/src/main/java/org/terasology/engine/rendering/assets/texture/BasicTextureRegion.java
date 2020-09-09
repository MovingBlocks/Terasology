// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.texture;

import org.joml.Rectanglef;
import org.joml.Rectanglei;
import org.joml.Vector2i;
import org.terasology.engine.math.JomlUtil;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Vector2f;

/**
 */
public class BasicTextureRegion implements TextureRegion {
    private final Texture texture;
    private final Rectanglef region;

    public BasicTextureRegion(Texture texture, Rect2f region) {
        this.texture = texture;
        this.region = JomlUtil.from(region);
    }

    public BasicTextureRegion(Texture texture, Vector2f offset, Vector2f size) {
        this(texture, Rect2f.createFromMinAndSize(offset, size));
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
        return JomlUtil.rectangleiFromMinAndSize(
                TeraMath.floorToInt(region.minX * texture.getWidth()),
                TeraMath.floorToInt(region.minY * texture.getHeight()), getWidth(), getHeight());
    }
}
