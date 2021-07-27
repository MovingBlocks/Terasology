// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.texture;

import com.google.common.math.IntMath;
import org.terasology.gestalt.assets.AssetData;
import org.terasology.nui.Color;
import org.terasology.nui.Colorc;

import java.nio.ByteBuffer;
import java.util.Arrays;

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
        if (mipmaps[0].limit() % BYTES_PER_PIXEL != 0) {
            throw new IllegalArgumentException("Texture data format incorrect, must be a set of RGBA values for each pixel");
        }
        if (mipmaps[0].limit() != width * height * BYTES_PER_PIXEL) {
            throw new IllegalArgumentException("Texture data size incorrect, must be a set of RGBA values for each pixel (given "
                                               + mipmaps[0].limit() + ", expected " + width * height * BYTES_PER_PIXEL + ")");
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

    /**
     * Look up the color of a given pixel in the base mipmap level.
     */
    public Colorc getPixel(int x, int y, Color result) {
        ByteBuffer baseMipmap = data[0];
        int idx = BYTES_PER_PIXEL * (x + y * width);
        int pixel = baseMipmap.getInt(idx);
        result.set(pixel);
        return result;
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
