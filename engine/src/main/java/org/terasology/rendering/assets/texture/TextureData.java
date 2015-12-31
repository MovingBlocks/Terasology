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
package org.terasology.rendering.assets.texture;

import com.google.common.math.IntMath;
import org.terasology.assets.AssetData;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 */
public class TextureData implements AssetData {
    private static final int BYTES_PER_PIXEL = 4;

    private int width;
    private int height;
    private Texture.WrapMode wrapMode = Texture.WrapMode.CLAMP;
    private Texture.FilterMode filterMode = Texture.FilterMode.NEAREST;
    private Texture.Type type = Texture.Type.TEXTURE2D;
    private ByteBuffer[] data;

    public TextureData(int width, int height, Texture.WrapMode wrapMode, Texture.FilterMode filterMode) {
        this.width = width;
        this.height = height;
        this.wrapMode = wrapMode;
        this.filterMode = filterMode;
    }

    public TextureData(int width, int height, ByteBuffer[] mipmaps, Texture.WrapMode wrapMode, Texture.FilterMode filterMode, Texture.Type type) {
        this(width, height, wrapMode, filterMode);

        if (mipmaps.length == 0) {
            throw new IllegalArgumentException("Must supply at least one mipmap");
        }

        this.type = type;
        this.data = Arrays.copyOf(mipmaps, mipmaps.length);

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }
        if (mipmaps[0].limit() != width * height * BYTES_PER_PIXEL) {
            throw new IllegalArgumentException("Texture data size incorrect, must be a set of RGBA values for each pixel (width * height)");
        }
        if (mipmaps.length > 1 && !(IntMath.isPowerOfTwo(width) && IntMath.isPowerOfTwo(height))) {
            throw new IllegalArgumentException("Texture width, height and depth must be powers of 2 for mipmapping");
        }
        for (int i = 1; i < mipmaps.length; ++i) {
            int mipWidth = width >> i;
            int mipHeight = height >> i;
            if (mipWidth * mipHeight * BYTES_PER_PIXEL != mipmaps[i].limit()) {
                throw new IllegalArgumentException("Mipmap has wrong dimensions");
            }
        }
    }

    public TextureData(int width, int height, ByteBuffer[] mipmaps, Texture.WrapMode wrapMode, Texture.FilterMode filterMode) {
        this(width, height, mipmaps, wrapMode, filterMode, Texture.Type.TEXTURE2D);
    }

    public TextureData(TextureData fromCopy) {
        this(fromCopy.width, fromCopy.height, fromCopy.wrapMode, fromCopy.filterMode);
        this.type = fromCopy.type;
        this.data = new ByteBuffer[fromCopy.data.length];
        for (int i = 0; i < fromCopy.data.length; ++i) {
            data[i] = fromCopy.data[i].duplicate();
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Texture.Type getType() {
        return type;
    }

    public Texture.WrapMode getWrapMode() {
        return wrapMode;
    }

    public Texture.FilterMode getFilterMode() {
        return filterMode;
    }

    public ByteBuffer[] getBuffers() {
        return data;
    }

    public void setFilterMode(Texture.FilterMode filterMode) {
        this.filterMode = filterMode;
    }

    public void setWrapMode(Texture.WrapMode wrapMode) {
        this.wrapMode = wrapMode;
    }

    public void setType(Texture.Type type) {
        this.type = type;
    }
}
