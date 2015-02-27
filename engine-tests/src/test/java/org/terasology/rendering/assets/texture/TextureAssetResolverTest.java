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

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.asset.AssetFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.engine.subsystem.headless.assets.HeadlessTexture;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.Color;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

/**
 * Tests texture asset resolvers.
 * @author Martin Steiger
 */
public class TextureAssetResolverTest extends TerasologyTestingEnvironment {

    @BeforeClass
    public static void setupAssetManager() {
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        assetManager.setAssetFactory(AssetType.TEXTURE, new AssetFactory<TextureData, Texture>() {
            @Override
            public Texture buildAsset(AssetUri uri, TextureData data) {
                return new HeadlessTexture(uri, data);
            }
        });

        assetManager.addResolver(AssetType.TEXTURE, new ColorTextureAssetResolver());
        assetManager.addResolver(AssetType.TEXTURE, new NoiseTextureAssetResolver());
    }

    @Test
    public void testColorTextures() {

        Random r = new FastRandom(123456);

        for (int i = 0; i < 10; i++) {
            int rgba = r.nextInt();
            Color red = new Color(rgba);
            AssetUri textureUriForColor = TextureUtil.getTextureUriForColor(red);
            String simpleString = textureUriForColor.toSimpleString();
            Texture tex = Assets.getTexture(simpleString);
            ByteBuffer dataBuffer = tex.getData().getBuffers()[0];
            int firstPixel = dataBuffer.asIntBuffer().get(0);

            Assert.assertEquals(rgba, firstPixel);
        }
    }

    @Test
    public void testNoiseTextures() {

        int size = 256;

        AssetUri textureUriForWhiteNoise = TextureUtil.getTextureUriForWhiteNoise(size, 123354, 0, 255);
        String simpleString = textureUriForWhiteNoise.toSimpleString();
        Texture tex = Assets.getTexture(simpleString);

        Assert.assertTrue(tex.getWidth() == size);
        Assert.assertTrue(tex.getHeight() == size);
    }
}

