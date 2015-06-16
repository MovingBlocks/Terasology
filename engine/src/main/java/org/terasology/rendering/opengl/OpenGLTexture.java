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
package org.terasology.rendering.opengl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.subsystem.lwjgl.LwjglGraphics;
import org.terasology.math.Rect2f;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;

import java.nio.ByteBuffer;

/**
 * @author Immortius
 */
public class OpenGLTexture extends Texture {

    private static final Logger logger = LoggerFactory.getLogger(OpenGLTexture.class);

    private final LwjglGraphics graphicsManager;
    private transient int id;
    private transient LoadedTextureInfo loadedTextureInfo;

    public OpenGLTexture(ResourceUrn urn, AssetType<?, TextureData> assetType, TextureData data, LwjglGraphics graphicsManager) {
        super(urn, assetType);
        this.graphicsManager = graphicsManager;
        reload(data);
    }

    public void setId(int id) {
        this.id = id;
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

                loadedTextureInfo = new LoadedTextureInfo(size, size, size, data);

                if (id == 0) {
                    graphicsManager.createTexture3D(alignedBuffer, getWrapMode(), getFilterMode(),
                            size, (newId) -> {
                                synchronized (this) {
                                    if (id != 0) {
                                        graphicsManager.disposeTexture(id);
                                    }
                                    if (isDisposed()) {
                                        graphicsManager.disposeTexture(newId);
                                    } else {
                                        id = newId;
                                        logger.debug("Bound texture '{}' - {}", getUrn(), id);
                                    }
                                }
                            });
                } else {
                    graphicsManager.reloadTexture3D(id, alignedBuffer, getWrapMode(), getFilterMode(), size);
                }
                break;
            default:
                int width = data.getWidth();
                int height = data.getHeight();
                loadedTextureInfo = new LoadedTextureInfo(width, height, 1, data);
                if (id == 0) {
                    graphicsManager.createTexture2D(data.getBuffers(), getWrapMode(), getFilterMode(), width, height, (newId) -> {
                        synchronized (this) {
                            if (id != 0) {
                                graphicsManager.disposeTexture(id);
                            }
                            if (isDisposed()) {
                                graphicsManager.disposeTexture(newId);
                            } else {
                                id = newId;
                                logger.debug("Bound texture '{}' - {}", getUrn(), id);
                            }
                        }
                    });
                } else {
                    graphicsManager.reloadTexture2D(id, data.getBuffers(), getWrapMode(), getFilterMode(), width, height);
                }
                break;
        }
    }

    @Override
    protected void doDispose() {
        if (loadedTextureInfo != null) {
            graphicsManager.disposeTexture(id);
            loadedTextureInfo = null;
            id = 0;
        }
    }

    public int getId() {
        return id;
    }

    @Override
    public int getDepth() {
        if (loadedTextureInfo != null) {
            return loadedTextureInfo.getDepth();
        }
        return 0;
    }

    public int getWidth() {
        if (loadedTextureInfo != null) {
            return loadedTextureInfo.getWidth();
        }
        return 0;
    }

    public int getHeight() {
        if (loadedTextureInfo != null) {
            return loadedTextureInfo.getHeight();
        }
        return 0;
    }

    @Override
    public Vector2i size() {
        return new Vector2i(getWidth(), getHeight());
    }

    public Texture.WrapMode getWrapMode() {
        return loadedTextureInfo.getWrapMode();
    }

    public FilterMode getFilterMode() {
        return loadedTextureInfo.getFilterMode();
    }

    @Override
    public TextureData getData() {
        return new TextureData(loadedTextureInfo.getTextureData());
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
    public Rect2i getPixelRegion() {
        return Rect2i.createFromMinAndSize(0, 0, getWidth(), getHeight());
    }

    @Override
    public boolean isLoaded() {
        return id != 0;
    }

    private class LoadedTextureInfo {
        private final int width;
        private final int height;
        private final int depth;
        private final Texture.WrapMode wrapMode;
        private final Texture.FilterMode filterMode;
        private final TextureData textureData;

        public LoadedTextureInfo(int width, int height, int depth, TextureData data) {
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

}
