// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.texture;

import org.junit.jupiter.api.Test;
import org.terasology.nui.Color;
import org.terasology.engine.rendering.assets.texture.Texture.FilterMode;
import org.terasology.engine.rendering.assets.texture.Texture.WrapMode;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextureDataFactoryTest {

    @Test
    public void testRedColorNoAlphaTransformedToTextureUri() throws Exception {
        Color expectedColor = Color.RED;
        int expectedRed = -1;
        int expectedGreen = 0;
        int expectedBlue = 0;
        int expectedAlpha = -1;

        testRepeatedColorInDataTexture(expectedColor, expectedRed, expectedGreen, expectedBlue, expectedAlpha);
    }

    @Test
    public void testColorTransformedToTextureUri() throws Exception {
        int red = 0x12;
        int green = 0x3;
        int blue = 0xc4;
        int alpha = 0xe;
        Color expectedColor = new Color(red, green, blue, alpha);

        int expectedRed = 18;
        int expectedGreen = 3;
        int expectedBlue = -60;
        int expectedAlpha = 14;

        testRepeatedColorInDataTexture(expectedColor, expectedRed, expectedGreen, expectedBlue, expectedAlpha);
    }

    private void testRepeatedColorInDataTexture(Color expectedColor, int expectedRed, int expectedGreen, int expectedBlue, int expectedAlpha) {
        TextureData textureData = TextureDataFactory.newInstance(expectedColor);

        ByteBuffer[] buffers = textureData.getBuffers();
        assertEquals(1, buffers.length);
        ByteBuffer buffer = buffers[0];

        for (int offsetCounter = 0; offsetCounter < 4 * 16; offsetCounter++) {
            int offset = offsetCounter * 4;
            assertEquals(expectedRed, buffer.get(0 + offset));
            assertEquals(expectedGreen, buffer.get(1 + offset));
            assertEquals(expectedBlue, buffer.get(2 + offset));
            assertEquals(expectedAlpha, buffer.get(3 + offset));
        }

        assertEquals(16, textureData.getWidth());
        assertEquals(16, textureData.getHeight());
        assertEquals(FilterMode.NEAREST, textureData.getFilterMode());
        assertEquals(WrapMode.REPEAT, textureData.getWrapMode());
        assertEquals(Texture.Type.TEXTURE2D, textureData.getType());
    }
}
