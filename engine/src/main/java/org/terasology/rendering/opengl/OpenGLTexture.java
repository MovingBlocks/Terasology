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

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.math.Rect2f;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_CLAMP;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_NEAREST_MIPMAP_NEAREST;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexParameterf;

/**
 * @author Immortius
 */
public class OpenGLTexture extends AbstractAsset<TextureData> implements Texture {

    private static final Logger logger = LoggerFactory.getLogger(OpenGLTexture.class);

    private int id;
    private int width;
    private int height;
    private int depth;
    private WrapMode wrapMode = WrapMode.CLAMP;
    private FilterMode filterMode = FilterMode.NEAREST;
    private Type textureType = Type.TEXTURE2D;

    // TODO: Make the retention of this dependent on a keep-in-memory setting
    private TextureData textureData;

    /**
     * Note: Generally should not be called directly. Instead use Assets.generateAsset().
     *
     * @param uri
     * @param data
     */
    // TODO: Create lwjgl renderer subsystem, and make this package private
    public OpenGLTexture(AssetUri uri, TextureData data) {
        super(uri);

        reload(data);
    }

    @Override
    public void reload(TextureData data) {
        this.width = data.getWidth();
        this.height = data.getHeight();
        this.depth = data.getDepth();
        this.wrapMode = data.getWrapMode();
        this.filterMode = data.getFilterMode();
        this.textureType = data.getType();
        this.textureData = data;

        if (id == 0) {
            id = glGenTextures();
        }

        switch (textureType) {
            case TEXTURE2D:
                logger.debug("Bound texture '{}' - {}", getURI(), id);
                glBindTexture(GL11.GL_TEXTURE_2D, id);

                glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, getGLMode(wrapMode));
                glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, getGLMode(wrapMode));
                GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, getGlMinFilter(filterMode));
                GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, getGlMagFilter(filterMode));
                GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, data.getBuffers().length - 1);

                if (data.getBuffers().length > 0) {
                    for (int i = 0; i < data.getBuffers().length; i++) {
                        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, i, GL11.GL_RGBA, width >> i, height >> i, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffers()[i]);
                    }
                } else {
                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
                }
                break;
            case TEXTURE3D:
                logger.debug("Bound texture '{}' - {}", getURI(), id);
                glBindTexture(GL12.GL_TEXTURE_3D, id);

                glTexParameterf(GL12.GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, getGLMode(wrapMode));
                glTexParameterf(GL12.GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, getGLMode(wrapMode));
                glTexParameterf(GL12.GL_TEXTURE_3D, GL12.GL_TEXTURE_WRAP_R, getGLMode(wrapMode));

                GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MIN_FILTER, getGlMinFilter(filterMode));
                GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MAG_FILTER, getGlMagFilter(filterMode));

                GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
                GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL12.GL_TEXTURE_MAX_LEVEL, data.getBuffers().length - 1);

                if (data.getBuffers().length > 0) {
                    for (int i = 0; i < data.getBuffers().length; i++) {
                        GL12.glTexImage3D(GL12.GL_TEXTURE_3D, i, GL11.GL_RGBA, width, height, depth, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffers()[i]);
                    }
                } else {
                    GL12.glTexImage3D(GL12.GL_TEXTURE_3D, 0, GL11.GL_RGBA, width, height, depth, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
                }

                break;
        }
    }

    private int getGLMode(WrapMode mode) {
        switch (mode) {
            case CLAMP:
                return GL_CLAMP;
            case REPEAT:
                return GL_REPEAT;
            default:
                throw new RuntimeException("Unsupported WrapMode '" + mode + "'");
        }
    }

    private int getGlMinFilter(FilterMode mode) {
        switch (mode) {
            case LINEAR:
                return GL_LINEAR_MIPMAP_LINEAR;
            case NEAREST:
                return GL_NEAREST_MIPMAP_NEAREST;
            default:
                throw new RuntimeException("Unsupported FilterMode '" + mode + "'");
        }
    }

    private int getGlMagFilter(FilterMode filterMode2) {
        switch (filterMode) {
            case LINEAR:
                return GL_LINEAR;
            case NEAREST:
                return GL_NEAREST;
            default:
                throw new RuntimeException("Unsupported FilterMode '" + filterMode + "'");
        }
    }

    @Override
    public void dispose() {
        if (id != 0) {
            glDeleteTextures(id);
            id = 0;
        }
    }

    @Override
    public boolean isDisposed() {
        return id == 0;
    }

    public int getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public Vector2i size() {
        return new Vector2i(width, height);
    }

    public Texture.WrapMode getWrapMode() {
        return wrapMode;
    }

    public FilterMode getFilterMode() {
        return filterMode;
    }

    @Override
    public TextureData getData() {
        return textureData;
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
        return Rect2i.createFromMinAndSize(0, 0, width, height);
    }
}
