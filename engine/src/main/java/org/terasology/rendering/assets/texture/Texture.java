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

import org.terasology.math.Rect2f;

/**
 * @author Immortius
 */
public interface Texture extends TextureRegionAsset<TextureData> {

    Rect2f FULL_TEXTURE_REGION = Rect2f.createFromMinAndSize(0, 0, 1, 1);

    public enum WrapMode {
        CLAMP,
        REPEAT
    }

    public enum FilterMode {
        NEAREST,
        LINEAR
    }

    WrapMode getWrapMode();

    FilterMode getFilterMode();

    // TODO: Remove when no longer needed
    TextureData getData();

    // TODO: This shouldn't be on texture
    int getId();

    public enum Type {
        TEXTURE2D,
        TEXTURE3D
    }
}
