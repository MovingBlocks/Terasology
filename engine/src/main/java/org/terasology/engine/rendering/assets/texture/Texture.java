// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.texture;

import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.joml.geom.Rectanglefc;

public abstract class Texture extends TextureRegionAsset<TextureData> {

    public static final Rectanglefc FULL_TEXTURE_REGION = new Rectanglef(0, 0, 1, 1);

    protected Texture(ResourceUrn urn, AssetType<?, TextureData> assetType) {
        super(urn, assetType);
    }

    protected Texture(ResourceUrn urn, AssetType<?, TextureData> assetType, DisposableResource disposableResource) {
        super(urn, assetType,disposableResource);
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

    public abstract void subscribeToDisposal(DisposableResource subscriber);

    public abstract void unsubscribeToDisposal(DisposableResource subscriber);

}
