// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.texture.subtexture;

import org.terasology.gestalt.assets.AssetData;
import org.terasology.math.geom.Rect2f;
import org.terasology.engine.rendering.assets.texture.Texture;

/**
 */
public class SubtextureData implements AssetData {
    private final Texture texture;
    private final Rect2f region;

    public SubtextureData(Texture texture, Rect2f region) {
        this.texture = texture;
        this.region = region;
    }

    public Texture getTexture() {
        return texture;
    }

    public Rect2f getRegion() {
        return region;
    }

}
