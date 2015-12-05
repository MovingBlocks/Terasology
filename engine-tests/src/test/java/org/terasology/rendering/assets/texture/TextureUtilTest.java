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
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.TerasologyConstants;
import org.terasology.naming.Name;
import org.terasology.rendering.nui.Color;

import static org.junit.Assert.assertEquals;

/**
 */
public class TextureUtilTest {

    @Test
    public void testColorTransformedToTextureUri() throws Exception {
        ResourceUrn assetUri = TextureUtil.getTextureUriForColor(Color.RED);
        assertEquals(TerasologyConstants.ENGINE_MODULE, assetUri.getModuleName());
        assertEquals(new Name("color"), assetUri.getResourceName());
        assertEquals(new Name("ff0000ff"), assetUri.getFragmentName());

        int red = 0x12;
        int green = 0x3;
        int blue = 0xc4;
        int alpha = 0xe;
        assetUri = TextureUtil.getTextureUriForColor(new Color(red, green, blue, alpha));
        assertEquals(TerasologyConstants.ENGINE_MODULE, assetUri.getModuleName());
        assertEquals(new Name("color"), assetUri.getResourceName());
        assertEquals(new Name("1203c40e"), assetUri.getFragmentName());
    }

    @Test
    public void testColorNameTransformedToColor() throws Exception {
        Color actualColor = TextureUtil.getColorForColorName("ff0000ff");
        Color expectedColor = Color.RED;
        assertEquals(expectedColor, actualColor);

        actualColor = TextureUtil.getColorForColorName("1203c40e");
        int red = 0x12;
        int green = 0x3;
        int blue = 0xc4;
        int alpha = 0xe;
        expectedColor = new Color(red, green, blue, alpha);
        assertEquals(expectedColor, actualColor);
    }

    @Test
    public void testColorTransformedToAssetUriTransformedToColor() throws Exception {
        Color expectedColor = Color.RED;
        ResourceUrn assetUri = TextureUtil.getTextureUriForColor(expectedColor);
        Color actualColor = TextureUtil.getColorForColorName(assetUri.getFragmentName().toLowerCase());
        assertEquals(expectedColor, actualColor);

        int red = 0x12;
        int green = 0x3;
        int blue = 0xc4;
        int alpha = 0xe;
        expectedColor = new Color(red, green, blue, alpha);
        assetUri = TextureUtil.getTextureUriForColor(expectedColor);
        actualColor = TextureUtil.getColorForColorName(assetUri.getFragmentName().toLowerCase());
        assertEquals(expectedColor, actualColor);
    }
}
