// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.texture.subtexture;

import org.joml.Rectanglef;
import org.joml.Rectanglei;
import org.joml.Vector2i;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.math.JomlUtil;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Rect2f;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureRegionAsset;

import java.util.Optional;

/**
 *
 */
public class Subtexture extends TextureRegionAsset<SubtextureData> {

    private Texture texture;
    private Rect2f subregion;
    private final DisposableResource disposalAction;

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
