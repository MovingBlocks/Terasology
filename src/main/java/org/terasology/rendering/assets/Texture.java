/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.rendering.assets;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Asset;
import org.terasology.asset.AssetUri;

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
public class Texture implements Asset {
    public enum WrapMode {
        Clamp(GL_CLAMP),
        Repeat(GL_REPEAT);

        private int glWrapEnum;

        private WrapMode(int glEnum) {
            this.glWrapEnum = glEnum;
        }

        public int getGLMode() {
            return glWrapEnum;
        }
    }

    public enum FilterMode {
        Nearest(GL_NEAREST_MIPMAP_NEAREST, GL_NEAREST),
        Linear(GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);

        private int glMinFilter;
        private int glMagFilter;

        private FilterMode(int glMinFilter, int glMagFilter) {
            this.glMinFilter = glMinFilter;
            this.glMagFilter = glMagFilter;
        }

        public int getGlMinFilter() {
            return glMinFilter;
        }

        public int getGlMagFilter() {
            return glMagFilter;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(Texture.class);

    private AssetUri uri;
    private int id;
    private int width;
    private int height;
    private WrapMode wrapMode = WrapMode.Clamp;
    private FilterMode filterMode = FilterMode.Nearest;
    private ByteBuffer[] data;

    public Texture(ByteBuffer[] data, int width, int height, WrapMode wrapMode, FilterMode filterMode) {
        this(new AssetUri(), data, width, height, wrapMode, filterMode);
    }

    public Texture(AssetUri uri, ByteBuffer[] data, int width, int height, WrapMode wrapMode, FilterMode filterMode) {
        if (data.length == 0) throw new IllegalArgumentException("Expected Data.length >= 1");
        this.uri = uri;
        this.width = width;
        this.height = height;
        this.wrapMode = wrapMode;
        this.filterMode = filterMode;
        this.data = data;

        id = glGenTextures();
        logger.debug("Bound texture '{}' - {}", uri, id);
        glBindTexture(GL11.GL_TEXTURE_2D, id);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapMode.getGLMode());
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapMode.getGLMode());
        GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filterMode.getGlMinFilter());
        GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filterMode.getGlMagFilter());
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, data.length - 1);

        for (int i = 0; i < data.length; i++) {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, i, GL11.GL_RGBA, width >> i, height >> i, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data[i]);
        }
    }

    @Override
    public AssetUri getURI() {
        return uri;
    }

    @Override
    public void dispose() {
        if (id != 0) {
            glDeleteTextures(id);
            id = 0;
        }
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

    public ByteBuffer getImageData(int mipmap) {
        return data[mipmap];
    }

    public int getMipmapCount() {
        return data.length;
    }

    public WrapMode getWrapMode() {
        return wrapMode;
    }

    public FilterMode getFilterMode() {
        return filterMode;
    }
}
