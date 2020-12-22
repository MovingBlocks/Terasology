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

import org.joml.Rectanglef;
import org.joml.Rectanglei;
import org.joml.Vector2i;
import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.math.JomlUtil;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Rect2f;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegionAsset;

import java.util.Optional;

/**
 */
public class Subtexture extends TextureRegionAsset<SubtextureData> {

    private Texture texture;
    private Rect2f subregion;
    private Runnable disposalAction;
    private SubtextureData initData;

    public Subtexture(ResourceUrn urn, AssetType<?, SubtextureData> assetType, SubtextureData initData) {
        super(urn, assetType);
        disposalAction = this::dispose;
        this.initData = initData;
    }

    public void glInitialize() {
        reload(initData);
    }

    @Override
    protected void doReload(SubtextureData data) {
        data.getTexture().subscribeToDisposal(disposalAction);
        if (texture != null) {
              texture.unsubscribeToDisposal(disposalAction);
        }
        this.texture = data.getTexture();
        this.subregion = data.getRegion();
    }

    @Override
    protected Optional<? extends Asset<SubtextureData>> doCreateCopy(ResourceUrn instanceUrn, AssetType<?, SubtextureData> parentAssetType) {
        return Optional.of(new Subtexture(instanceUrn, parentAssetType, new SubtextureData(texture, subregion)));
    }

    @Override
    public Texture getTexture() {
        return texture;
    }

    @Override
    public Rectanglef getRegion() {
        return JomlUtil.from(subregion);
    }

    @Override
    public Rectanglei getPixelRegion() {
        return JomlUtil.rectangleiFromMinAndSize(
                TeraMath.floorToInt(subregion.minX() * texture.getWidth()),
                TeraMath.floorToInt(subregion.minY() * texture.getHeight()), getWidth(), getHeight());
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
