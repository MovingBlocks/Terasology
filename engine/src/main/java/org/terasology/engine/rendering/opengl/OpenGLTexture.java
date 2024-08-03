// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.opengl;

import com.google.common.collect.Lists;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.core.subsystem.lwjgl.LwjglGraphicsManager;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureData;

import java.nio.ByteBuffer;
import java.util.List;

public class OpenGLTexture extends Texture {

    private static final Logger logger = LoggerFactory.getLogger(OpenGLTexture.class);

    private final TextureResources resources;

    public OpenGLTexture(ResourceUrn urn, AssetType<?, TextureData> assetType, TextureData data,  TextureResources textureResources) {
        super(urn, assetType, textureResources);
        this.resources = textureResources;
        reload(data);
    }

    public static OpenGLTexture create(ResourceUrn urn, AssetType<?, TextureData> assetType, TextureData data, LwjglGraphicsManager graphicsManager) {
        return new OpenGLTexture(urn, assetType, data, new TextureResources(graphicsManager));
    }


    public void setId(int id) {
        resources.id = id;
    }

    @Override
    protected void doReload(TextureData data) {
        switch (data.getType()) {
            // TODO: reconsider how 3D textures handled (probably separate asset implementation with common interface?
            case TEXTURE3D:
                if (data.getWidth() % data.getHeight() != 0 || data.getWidth() / data.getHeight() != data.getHeight()) {
                    throw new RuntimeException("3D texture must be cubic (height^3) - width must thus be a multiple of height");
                }
                int size = data.getHeight();

                final int byteLength = 4 * 16 * 16 * 16;
                final int strideX = 16 * 4;
                final int strideY = 16 * 16 * 4;
                final int strideZ = 4;

                ByteBuffer alignedBuffer = ByteBuffer.allocateDirect(byteLength);
                for (int x = 0; x < size; x++) {
                    for (int y = 0; y < size; y++) {
                        for (int z = 0; z < size; z++) {
                            final int index = x * strideX + z * strideZ + strideY * y;

                            alignedBuffer.put(data.getBuffers()[0].get(index));
                            alignedBuffer.put(data.getBuffers()[0].get(index + 1));
                            alignedBuffer.put(data.getBuffers()[0].get(index + 2));
                            alignedBuffer.put(data.getBuffers()[0].get(index + 3));
                        }
                    }
                }
                alignedBuffer.flip();

                resources.loadedTextureInfo = new LoadedTextureInfo(size, size, size, data);

                if (resources.id == 0) {
                    resources.graphicsManager.createTexture3D(alignedBuffer, getWrapMode(), getFilterMode(),
                            size, (newId) -> {
                                synchronized (this) {
                                    if (resources.id != 0) {
                                        resources.graphicsManager.disposeTexture(resources.id);
                                    }
                                    if (isDisposed()) {
                                        resources.graphicsManager.disposeTexture(newId);
                                    } else {
                                        resources.id = newId;
                                        logger.debug("Bound texture '{}' - {}", getUrn(), resources.id); //NOPMD
                                    }
                                }
                            });
                } else {
                    resources.graphicsManager.reloadTexture3D(resources.id, alignedBuffer, getWrapMode(), getFilterMode(), size);
                }
                break;
            default:
                int width = data.getWidth();
                int height = data.getHeight();
                resources.loadedTextureInfo = new LoadedTextureInfo(width, height, 1, data);
                if (resources.id == 0) {
                    resources.graphicsManager.createTexture2D(data.getBuffers(), getWrapMode(), getFilterMode(), width, height, (newId) -> {
                        synchronized (this) {
                            if (resources.id != 0) {
                                resources.graphicsManager.disposeTexture(resources.id);
                            }
                            if (isDisposed()) {
                                resources.graphicsManager.disposeTexture(newId);
                            } else {
                                resources.id = newId;
                                logger.debug("Bound texture '{}' - {}", getUrn(), resources.id); //NOPMD
                            }
                        }
                    });
                } else {
                    resources.graphicsManager.reloadTexture2D(resources.id, data.getBuffers(), getWrapMode(), getFilterMode(), width, height);
                }
                break;
        }
    }

    @Override
    public int getId() {
        return resources.id;
    }

    @Override
    public int getDepth() {
        if (resources.loadedTextureInfo != null) {
            return resources.loadedTextureInfo.getDepth();
        }
        return 0;
    }

    @Override
    public int getWidth() {
        if (resources.loadedTextureInfo != null) {
            return resources.loadedTextureInfo.getWidth();
        }
        return 0;
    }

    @Override
    public int getHeight() {
        if (resources.loadedTextureInfo != null) {
            return resources.loadedTextureInfo.getHeight();
        }
        return 0;
    }

    @Override
    public Vector2i size() {
        return new Vector2i(getWidth(), getHeight());
    }

    @Override
    public Texture.WrapMode getWrapMode() {
        return resources.loadedTextureInfo.getWrapMode();
    }

    @Override
    public FilterMode getFilterMode() {
        return resources.loadedTextureInfo.getFilterMode();
    }

    @Override
    public TextureData getData() {
        return new TextureData(resources.loadedTextureInfo.getTextureData());
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
    public synchronized void subscribeToDisposal(DisposableResource subscriber) {
        resources.disposalSubscribers.add(subscriber);
    }

    @Override
    public synchronized void unsubscribeToDisposal(DisposableResource subscriber) {
        resources.disposalSubscribers.remove(subscriber);
    }

    @Override
    public boolean isLoaded() {
        return resources.id != 0;
    }

    private static class LoadedTextureInfo {
        private final int width;
        private final int height;
        private final int depth;
        private final Texture.WrapMode wrapMode;
        private final Texture.FilterMode filterMode;
        private final TextureData textureData;

         LoadedTextureInfo(int width, int height, int depth, TextureData data) {
            this.width = width;
            this.height = height;
            this.depth = depth;
            this.wrapMode = data.getWrapMode();
            this.filterMode = data.getFilterMode();
            this.textureData = data;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getDepth() {
            return depth;
        }

        public Texture.WrapMode getWrapMode() {
            return wrapMode;
        }

        public Texture.FilterMode getFilterMode() {
            return filterMode;
        }

        public TextureData getTextureData() {
            return textureData;
        }
    }

    private static class TextureResources implements DisposableResource {

        private final LwjglGraphicsManager graphicsManager;
        private volatile int id;
        private volatile LoadedTextureInfo loadedTextureInfo;

        private final List<DisposableResource> disposalSubscribers = Lists.newArrayList();

         TextureResources(LwjglGraphicsManager graphicsManager) {
            this.graphicsManager = graphicsManager;
        }


        @Override
        public void close() {
            if (loadedTextureInfo != null) {
                disposalSubscribers.forEach(DisposableResource::close);
                graphicsManager.disposeTexture(id);
                loadedTextureInfo = null;
                id = 0;
            }
        }
    }
}
