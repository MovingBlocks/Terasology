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
package org.terasology.rendering.assets.texture;

import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;

/**
 */
public class BasicTextureRegion implements TextureRegion {
    private Texture texture;
    private Rect2f region;

    public BasicTextureRegion(Texture texture, Rect2f region) {
        this.texture = texture;
        this.region = region;
    }

    public BasicTextureRegion(Texture texture, Vector2f offset, Vector2f size) {
        this(texture, Rect2f.createFromMinAndSize(offset, size));
    }

    @Override
    public Texture getTexture() {
        return texture;
    }

    @Override
    public Rect2f getRegion() {
        return region;
    }

    @Override
    public int getWidth() {
        return TeraMath.ceilToInt(texture.getWidth() * region.width());
    }

    @Override
    public int getHeight() {
        return TeraMath.ceilToInt(texture.getHeight() * region.height());
    }

    @Override
    public Vector2i size() {
        return new Vector2i(getWidth(), getHeight());
    }

    @Override
    public Rect2i getPixelRegion() {
        return Rect2i.createFromMinAndSize(TeraMath.floorToInt(region.minX() * texture.getWidth())
                , TeraMath.floorToInt(region.minY() * texture.getHeight()), getWidth(), getHeight());
    }
}
