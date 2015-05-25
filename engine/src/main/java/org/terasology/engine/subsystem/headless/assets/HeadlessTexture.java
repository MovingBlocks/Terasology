/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.engine.subsystem.headless.assets;

import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.math.Rect2f;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;

import java.util.concurrent.atomic.AtomicInteger;

public class HeadlessTexture extends Texture {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger();

    private TextureData textureData;
    private int id;

    public HeadlessTexture(ResourceUrn urn, AssetType<?, TextureData> assetType, TextureData data) {
        super(urn, assetType);
        reload(data);
        id = ID_COUNTER.getAndIncrement();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getDepth() {
        switch (textureData.getType()) {
            case TEXTURE3D:
                return textureData.getHeight();
            default:
                return 1;
        }
    }

    @Override
    protected void doReload(TextureData data) {
        this.textureData = data;
    }

    @Override
    public TextureData getData() {
        return new TextureData(textureData);
    }

    @Override
    protected void doDispose() {
        this.textureData = null;
    }

    @Override
    public Texture getTexture() {
        return this;
    }

    @Override
    public Rect2f getRegion() {
        return FULL_TEXTURE_REGION;
    }

    @Override
    public int getWidth() {
        switch (textureData.getType()) {
            case TEXTURE3D:
                return textureData.getHeight();
            default:
                return textureData.getWidth();
        }
    }

    @Override
    public int getHeight() {
        return textureData.getHeight();
    }

    @Override
    public Vector2i size() {
        return new Vector2i(getWidth(), getHeight());
    }

    @Override
    public WrapMode getWrapMode() {
        return textureData.getWrapMode();
    }

    @Override
    public FilterMode getFilterMode() {
        return textureData.getFilterMode();
    }

    @Override
    public Rect2i getPixelRegion() {
        return Rect2i.createFromMinAndSize(0, 0, textureData.getWidth(), textureData.getHeight());
    }
}
