// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.texture;

import org.joml.Vector2i;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.joml.geom.Rectanglei;

/**
 * Interface for any asset that describes a region of a texture that can be rendered (can include textures themselves).
 *
 */
// TODO: Remove UITextureRegion extension when NUI is fully re-integrated
public interface TextureRegion extends org.terasology.nui.UITextureRegion {

    Texture getTexture();

    /**
     * @return The region of the texture represented by this asset
     */
    Rectanglef getRegion();

    /**
     * @return The pixel region of the texture represented by this asset
     */
    Rectanglei getPixelRegion();

    int getWidth();

    int getHeight();

    Vector2i size();
}
