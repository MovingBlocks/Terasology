/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.rendering.assets.texture;

import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.math.geom.Rect2f;

/**
 */
public abstract class Texture extends TextureRegionAsset<TextureData> {

    public static final Rect2f FULL_TEXTURE_REGION = Rect2f.createFromMinAndSize(0, 0, 1, 1);

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
