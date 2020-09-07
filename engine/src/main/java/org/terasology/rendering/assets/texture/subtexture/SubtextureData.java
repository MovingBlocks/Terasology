/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.assets.texture.subtexture;

import org.terasology.gestalt.assets.AssetData;
import org.terasology.math.geom.Rect2f;
import org.terasology.rendering.assets.texture.Texture;

/**
 */
public class SubtextureData implements AssetData {
    private Texture texture;
    private Rect2f region;

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
