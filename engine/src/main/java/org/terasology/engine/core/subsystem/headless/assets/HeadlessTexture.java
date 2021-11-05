// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless.assets;

import com.google.common.collect.Lists;
import org.joml.Vector2i;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureData;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.joml.geom.Rectanglei;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HeadlessTexture extends Texture {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger();

    private TextureData textureData;
    private int id;
    private final DisposalAction disposalAction;

    public HeadlessTexture(ResourceUrn urn, AssetType<?, TextureData> assetType, TextureData data,
                           DisposalAction disposableResource) {
        super(urn, assetType, disposableResource);
        disposalAction = disposableResource;
        reload(data);
        id = ID_COUNTER.getAndIncrement();
    }

    public static HeadlessTexture create(ResourceUrn urn, AssetType<?, TextureData> assetType, TextureData data) {
        return new HeadlessTexture(urn, assetType, data, new DisposalAction());
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

    /**
     *
     * @return
     */
    @Override
    public boolean isLoaded() {
        return isDisposed();
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
    public Texture getTexture() {
        return this;
    }

    @Override
    public Rectanglef getRegion() {
        return new Rectanglef(FULL_TEXTURE_REGION); // object is not guarded
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
    public Rectanglei getPixelRegion() {
        return new Rectanglei(0, 0, textureData.getWidth(), textureData.getHeight());
    }

    @Override
    public synchronized void subscribeToDisposal(DisposableResource subscriber) {
        disposalAction.disposalListeners.add(subscriber);
    }

    @Override
    public synchronized void unsubscribeToDisposal(DisposableResource subscriber) {
        disposalAction.disposalListeners.remove(subscriber);
    }

    public static class DisposalAction implements DisposableResource {

        private final List<DisposableResource> disposalListeners = Lists.newArrayList();

        @Override
        public void close() {
            disposalListeners.forEach(DisposableResource::close);
        }
    }
}
