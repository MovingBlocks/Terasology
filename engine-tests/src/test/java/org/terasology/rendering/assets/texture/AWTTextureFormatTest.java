/*
 * Copyright 2018 MovingBlocks
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

import org.junit.Assert;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

public class AWTTextureFormatTest {
    @Test
    public void invalidImageTypeTest() {
        BufferedImage image = createBufferedImage(1, 2, BufferedImage.TYPE_BYTE_GRAY);
        try {
            AWTTextureFormat.convertToTextureData(image, Texture.FilterMode.LINEAR);
            Assert.fail("IOException should be thrown");
        } catch (IOException ex) {
            Assert.assertEquals("Unsupported AWT format: " + image.getType(), ex.getMessage());
        }
    }

    @Test
    public void successTest()
            throws IOException {
        BufferedImage image = createBufferedImage(2, 3, BufferedImage.TYPE_3BYTE_BGR);
        TextureData textureData = AWTTextureFormat.convertToTextureData(image, Texture.FilterMode.LINEAR);

        Assert.assertNotNull(textureData);
        Assert.assertEquals(2, textureData.getWidth());
        Assert.assertEquals(3, textureData.getHeight());
        Assert.assertEquals(Texture.FilterMode.LINEAR, textureData.getFilterMode());
    }

    private BufferedImage createBufferedImage(final int width, final int height, final int imageType) {
        BufferedImage image = new BufferedImage(width, height, imageType);
        WritableRaster raster = image.getRaster();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                raster.setSample(j, i, 0, 50);
            }
        }
        return image;
    }
}
