// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.texture;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.terasology.engine.TerasologyTestingEnvironment;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.nui.Color;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;

import java.nio.ByteBuffer;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests texture asset resolvers.
 */
@Tag("TteTest")
public class TextureAssetResolverTest extends TerasologyTestingEnvironment {

    @Test
    public void testColorTextures() {

        Random r = new FastRandom(123456);

        for (int i = 0; i < 10; i++) {
            int rgba = r.nextInt();
            Color red = new Color(rgba);
            ResourceUrn textureUriForColor = TextureUtil.getTextureUriForColor(red);
            String simpleString = textureUriForColor.toString();
            Optional<Texture> tex = Assets.getTexture(simpleString);
            assertTrue(tex.isPresent());
            ByteBuffer dataBuffer = tex.get().getData().getBuffers()[0];
            int firstPixel = dataBuffer.asIntBuffer().get(0);

            assertEquals(rgba, firstPixel);
        }
    }

    @Test
    public void testNoiseTextures() {

        int size = 256;

        ResourceUrn textureUriForWhiteNoise = TextureUtil.getTextureUriForWhiteNoise(size, 123354, 0, 255);
        String simpleString = textureUriForWhiteNoise.toString();
        Optional<Texture> tex = Assets.getTexture(simpleString);

        assertTrue(tex.isPresent());
        assertEquals(tex.get().getWidth(), size);
        assertEquals(tex.get().getHeight(), size);
    }
}

