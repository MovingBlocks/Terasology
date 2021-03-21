// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.texture.subtexture;

import org.terasology.gestalt.assets.AssetData;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.engine.rendering.assets.texture.Texture;

/**
 */
public class SubtextureData implements AssetData {
    private Texture texture;
    private Rectanglef region;

    public SubtextureData(Texture texture, Rectanglef region) {
        this.texture = texture;
        this.region = region;
    }

    public Texture getTexture() {
        return texture;
    }

    public Rectanglef getRegion() {
        return region;
    }

}
