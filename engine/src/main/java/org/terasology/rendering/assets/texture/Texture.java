// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.texture;

import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.joml.geom.Rectanglefc;

public abstract class Texture extends TextureRegionAsset<TextureData> {

    public static final Rectanglefc FULL_TEXTURE_REGION = new Rectanglef(0, 0, 1, 1);

    protected Texture(ResourceUrn urn, AssetType<?, TextureData> assetType) {
        super(urn, assetType);
    }

    public enum WrapMode {
        CLAMP,
        REPEAT
    }

    public enum FilterMode {
        NEAREST,
        LINEAR
    }

    public enum Type {
        TEXTURE2D,
        TEXTURE3D
    }

    public abstract WrapMode getWrapMode();

    public abstract FilterMode getFilterMode();

    // TODO: Remove when no longer needed
    public abstract TextureData getData();

    // TODO: This shouldn't be on texture
    public abstract int getId();

    public abstract int getDepth();

    public abstract boolean isLoaded();

    public abstract void subscribeToDisposal(Runnable subscriber);

    public abstract void unsubscribeToDisposal(Runnable subscriber);

}
