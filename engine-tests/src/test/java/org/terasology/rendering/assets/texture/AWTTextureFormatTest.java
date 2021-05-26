// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.assets.texture;

import org.junit.jupiter.api.Test;
import org.terasology.engine.rendering.assets.texture.AWTTextureFormat;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureData;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class AWTTextureFormatTest {
    @Test
    public void invalidImageTypeTest() {
        BufferedImage image = createBufferedImage(1, 2, BufferedImage.TYPE_BYTE_GRAY);
        try {
            AWTTextureFormat.convertToTextureData(image, Texture.FilterMode.LINEAR);
            fail("IOException should be thrown");
        } catch (IOException ex) {
            assertEquals("Unsupported AWT format: " + image.getType(), ex.getMessage());
        }
    }

    @Test
    public void successTest()
            throws IOException {
        BufferedImage image = createBufferedImage(2, 3, BufferedImage.TYPE_3BYTE_BGR);
        TextureData textureData = AWTTextureFormat.convertToTextureData(image, Texture.FilterMode.LINEAR);

        assertNotNull(textureData);
        assertEquals(2, textureData.getWidth());
        assertEquals(3, textureData.getHeight());
        assertEquals(Texture.FilterMode.LINEAR, textureData.getFilterMode());
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
