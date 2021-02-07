// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.assets.texture.subtexture;

import org.joml.Vector2i;
import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.math.JomlUtil;
import org.terasology.math.TeraMath;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegionAsset;

import java.util.Optional;

/**
 */
public class Subtexture extends TextureRegionAsset<SubtextureData> {

    private Texture texture;
    private Rectanglef subregion;
    private Runnable disposalAction;

    public Subtexture(ResourceUrn urn, AssetType<?, SubtextureData> assetType, SubtextureData data) {
        super(urn, assetType);
        disposalAction = this::dispose;
        reload(data);
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
        return subregion;
    }

    @Override
    public Rectanglei getPixelRegion() {
        return JomlUtil.rectangleiFromMinAndSize(
                TeraMath.floorToInt(subregion.minX() * texture.getWidth()),
                TeraMath.floorToInt(subregion.minY() * texture.getHeight()), getWidth(), getHeight());
    }

    @Override
    public int getWidth() {
        return TeraMath.ceilToInt(texture.getWidth() * subregion.getSizeX());
    }

    @Override
    public int getHeight() {
        return TeraMath.ceilToInt(texture.getHeight() * subregion.getSizeY());
    }

    @Override
    public Vector2i size() {
        return new Vector2i(getWidth(), getHeight());
    }
}
