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
package org.terasology.rendering.nullrenderer;

import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.math.Rect2f;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;

/**
 * @author Immortius
 */
public class NullTexture extends AbstractAsset<TextureData> implements Texture {

    // TODO: Remove later;
    private TextureData data;

    private FilterMode filterMode;
    private WrapMode wrapMode;
    private int height;
    private int width;

    public NullTexture(AssetUri uri, TextureData data) {
        super(uri);
        reload(data);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public WrapMode getWrapMode() {
        return wrapMode;
    }

    @Override
    public Texture.FilterMode getFilterMode() {
        return filterMode;
    }

    @Override
    public TextureData getData() {
        return data;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public void reload(TextureData newData) {
        this.data = newData;
        this.filterMode = newData.getFilterMode();
        this.wrapMode = newData.getWrapMode();
        this.height = newData.getHeight();
        this.width = newData.getWidth();
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    @Override
    public Texture getTexture() {
        return this;
    }

    @Override
    public Rect2f getRegion() {
        return FULL_TEXTURE_REGION;
    }
}
