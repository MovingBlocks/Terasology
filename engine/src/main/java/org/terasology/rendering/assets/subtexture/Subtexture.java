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
package org.terasology.rendering.assets.subtexture;

import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.math.Rect2f;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.TextureRegion;
import org.terasology.rendering.assets.texture.Texture;

/**
 * @author Immortius
 */
public class Subtexture extends AbstractAsset<SubtextureData> implements TextureRegion {

    private Texture texture;
    private Rect2f subregion;

    public Subtexture(AssetUri uri, SubtextureData data) {
        super(uri);
        reload(data);
    }

    @Override
    public void reload(SubtextureData data) {
        this.texture = data.getTexture();
        this.subregion = data.getRegion();

    }

    @Override
    public void dispose() {
        texture = null;
    }

    @Override
    public boolean isDisposed() {
        return texture == null || texture.isDisposed();
    }

    @Override
    public Texture getTexture() {
        return texture;
    }

    @Override
    public Rect2f getRegion() {
        return subregion;
    }

    @Override
    public int getWidth() {
        return TeraMath.ceilToInt(texture.getWidth() * subregion.width());
    }

    @Override
    public int getHeight() {
        return TeraMath.ceilToInt(texture.getHeight() * subregion.height());
    }

    @Override
    public Vector2i size() {
        return new Vector2i(getWidth(), getHeight());
    }
}
