// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.rendering.assets.texture;

import org.terasology.math.TeraMath;
import org.terasology.nui.Color;
import org.terasology.rendering.assets.texture.Texture.FilterMode;
import org.terasology.rendering.assets.texture.Texture.WrapMode;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Creates TextureData objects based on specific criteria
 *
 */
public final class TextureDataFactory {
    // Lwjgl 2.x currently requires textures to be powers of 16, although this should change in 3.0.
    private static final int TEXTURE_WIDTH = 16;
    private static final int TEXTURE_HEIGHT = 16;

    private TextureDataFactory() {
    }

    /**
     * Create TextureData for a Texture all of a single color.
     * @param color to use for creating TextureData
     * @return TextureData created using specified color
     */
    public static TextureData newInstance(Color color) {

        byte red = (byte) color.r();
        byte green = (byte) color.g();
        byte blue = (byte) color.b();
        byte alpha = (byte) color.a();

        ByteBuffer data = ByteBuffer.allocateDirect(4 * TEXTURE_WIDTH * TEXTURE_HEIGHT);
        for (int height = 0; height < TEXTURE_HEIGHT; height++) {
            for (int width = 0; width < TEXTURE_WIDTH; width++) {
                data.put(red).put(green).put(blue).put(alpha);
            }
        }

        // The buffer must be reset back to the initial position before passing it onward.
        ((Buffer) data).rewind(); // Explicitly casting for Java11/Java8 compability. problem at bytecode level

        return new TextureData(TEXTURE_WIDTH, TEXTURE_HEIGHT, new ByteBuffer[]{data}, WrapMode.REPEAT, FilterMode.NEAREST);
    }

    public static TextureData createWhiteNoiseTexture(int size, long seed, int min, int max) {
        int width = size;
        int height = size;
        ByteBuffer data = ByteBuffer.allocateDirect(4 * width * height);

        Random rng = new FastRandom(seed);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                data.put((byte) TeraMath.clamp(rng.nextInt(min, max), 0, 255));
                data.put((byte) TeraMath.clamp(rng.nextInt(min, max), 0, 255));
                data.put((byte) TeraMath.clamp(rng.nextInt(min, max), 0, 255));
                data.put((byte) 255);
            }
        }

        // The buffer must be reset back to the initial position before passing it onward.
        data.rewind();

        return new TextureData(width, height, new ByteBuffer[]{data}, WrapMode.REPEAT, FilterMode.NEAREST);
    }
}
