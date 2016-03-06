/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.utilities.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.rendering.nui.Color;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import java.nio.ByteBuffer;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

/**
 * Tests texture asset resolvers.
 *
 */
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

            Assert.assertEquals(rgba, firstPixel);
        }
    }

    @Test
    public void testNoiseTextures() {

        int size = 256;

        ResourceUrn textureUriForWhiteNoise = TextureUtil.getTextureUriForWhiteNoise(size, 123354, 0, 255);
        String simpleString = textureUriForWhiteNoise.toString();
        Optional<Texture> tex = Assets.getTexture(simpleString);

        assertTrue(tex.isPresent());
        assertTrue(tex.get().getWidth() == size);
        assertTrue(tex.get().getHeight() == size);
    }
}

