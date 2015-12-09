/*
 * Copyright 2013 MovingBlocks
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

import org.junit.Test;
import org.terasology.rendering.assets.texture.Texture.FilterMode;
import org.terasology.rendering.assets.texture.Texture.WrapMode;
import org.terasology.rendering.nui.Color;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 */
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
