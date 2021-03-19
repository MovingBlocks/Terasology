// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.texture;

import org.junit.jupiter.api.Test;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.naming.Name;
import org.terasology.nui.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
