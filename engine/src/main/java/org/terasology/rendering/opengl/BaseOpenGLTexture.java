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

import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.math.Rect2f;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;

import static org.lwjgl.opengl.GL11.GL_CLAMP;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_NEAREST_MIPMAP_NEAREST;
import static org.lwjgl.opengl.GL11.GL_REPEAT;

/**
 * A OpenGL texture without associated data. Useful to wrap GL texture ids and make it usable through Assets
 *
 * @author synopia
 */
public class BaseOpenGLTexture extends AbstractAsset<TextureData> implements Texture {
    private int id;
    private int width;
    private int height;
    private int depth;
    private WrapMode wrapMode = WrapMode.CLAMP;
    private FilterMode filterMode = FilterMode.NEAREST;
    private Type textureType = Type.TEXTURE2D;

    public BaseOpenGLTexture(AssetUri uri) {
        super(uri);
    }

    @Override
    public TextureData getData() {
        return null;
    }

    @Override
    public void reload(TextureData data) {
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

    @Override
    public void dispose() {
    }

    @Override
    public boolean isDisposed() {
        return false;
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

    public WrapMode getWrapMode() {
        return wrapMode;
    }

    public FilterMode getFilterMode() {
        return filterMode;
    }

    public Type getTextureType() {
        return textureType;
    }

    public int getDepth() {
        return depth;
    }

    void setId(int id) {
        this.id = id;
    }

    void setWidth(int width) {
        this.width = width;
    }

    void setHeight(int height) {
        this.height = height;
    }

    void setDepth(int depth) {
        this.depth = depth;
    }

    void setWrapMode(WrapMode wrapMode) {
        this.wrapMode = wrapMode;
    }

    void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode;
    }

    void setTextureType(Type textureType) {
        this.textureType = textureType;
    }

    protected int getGLMode(WrapMode mode) {
        switch (mode) {
            case CLAMP:
                return GL_CLAMP;
            case REPEAT:
                return GL_REPEAT;
            default:
                throw new RuntimeException("Unsupported WrapMode '" + mode + "'");
        }
    }

    protected int getGlMinFilter(FilterMode mode) {
        switch (mode) {
            case LINEAR:
                return GL_LINEAR_MIPMAP_LINEAR;
            case NEAREST:
                return GL_NEAREST_MIPMAP_NEAREST;
            default:
                throw new RuntimeException("Unsupported FilterMode '" + mode + "'");
        }
    }

    protected int getGlMagFilter(FilterMode filterMode2) {
        switch (filterMode) {
            case LINEAR:
                return GL_LINEAR;
            case NEAREST:
                return GL_NEAREST;
            default:
                throw new RuntimeException("Unsupported FilterMode '" + filterMode + "'");
        }
    }
}
