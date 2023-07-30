// Copyright 2023 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.opengl;

import com.google.common.collect.Lists;
import org.joml.Vector2i;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureData;
import org.terasology.engine.rust.EngineKernel;
import org.terasology.engine.rust.resource.TeraTexture;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.joml.geom.Rectanglei;

import java.util.List;


public class WgpuTexture extends Texture  {
    private TextureResources resource;
    private final EngineKernel kernel;
    public static TeraTexture.TextureDesc createDesc(TextureData data) {
        TeraTexture.TextureDesc desc = new TeraTexture.TextureDesc();
        desc.setFormat(TeraTexture.ImageFormat.R8G8B8A8_UNORM)
                .setDim(TeraTexture.TextureDimension.DIM_2D)
                .setWidth(data.getWidth())
                .setHeight(data.getHeight())
                .setLayers(1);
        return desc;
    }

    public WgpuTexture(EngineKernel kernel, ResourceUrn urn, AssetType<Texture, TextureData> assetType, TextureResources textureResources) {
        super(urn, assetType);
        this.kernel = kernel;
        this.resource = textureResources;
    }

    @Override
    protected void doReload(TextureData data) {
        this.resource = new TextureResources(data, this.kernel.resource.createTexture(createDesc(data), data.getBuffers()[0]));
    }

    @Override
    public WrapMode getWrapMode() {
        return WrapMode.CLAMP;
    }

    @Override
    public FilterMode getFilterMode() {
        return FilterMode.NEAREST;
    }

    @Override
    public TextureData getData() {
        return this.resource.data;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public int getDepth() {
        return 1;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public void subscribeToDisposal(DisposableResource subscriber) {
        this.resource.disposalSubscribers.add(subscriber);
    }

    @Override
    public void unsubscribeToDisposal(DisposableResource subscriber) {
        this.resource.disposalSubscribers.remove(subscriber);
    }

    @Override
    public Texture getTexture() {
        return this;
    }

    @Override
    public Rectanglef getRegion() {
        return new Rectanglef(FULL_TEXTURE_REGION); // object is not guarded
    }

    @Override
    public Rectanglei getPixelRegion() {
        return new Rectanglei(0, 0, getWidth(), getHeight());
    }

    @Override
    public int getWidth() {
        return this.resource.data.getWidth();
    }

    @Override
    public int getHeight() {
        return this.resource.data.getHeight();
    }

    @Override
    public Vector2i size() {
        return new Vector2i(this.resource.data.getWidth(), this.resource.data.getHeight());
    }

    public TeraTexture getTeraTexture() {
        return this.resource.texture;
    }

    public static class TextureResources implements DisposableResource {
        private final TeraTexture texture;
        private final List<DisposableResource> disposalSubscribers = Lists.newArrayList();
        private final TextureData data;

        public TextureResources(TextureData data, TeraTexture texture) {
            this.data = data;
            this.texture = texture;
        }

        @Override
        public void close() {
            disposalSubscribers.forEach(DisposableResource::close);
            this.texture.dispose();
        }
    }
}
