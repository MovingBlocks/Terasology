/*
 * Copyright 2014 MovingBlocks
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

import com.google.common.primitives.UnsignedBytes;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.math.Rect2i;
import org.terasology.rendering.nui.Color;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public final class TextureUtil {
    public static final String GENERATED_COLOR_NAME_PREFIX = "color";

    private TextureUtil() {
    }

    /**
     * Returns a AssetUri which represents a Texture of that color.
     *
     * @param color, including alpha, of the texture to represent.
     * @return an asset Uri for the texture
     */
    public static AssetUri getTextureUriForColor(Color color) {
        StringBuilder sb = new StringBuilder(GENERATED_COLOR_NAME_PREFIX);
        sb.append(".");

        appendColorName(sb, color);

        return new AssetUri(AssetType.TEXTURE, "engine", sb.toString());
    }

    /**
     * Method to convert the color string hex representation back to a color.
     * Package-only access as it is for internal use in ColorTextureAssetResolver,
     * but should be here for maintenance with the color-to-color-string code.
     *
     * @param sb    StringBuilder into which to append name
     * @param color represented by hexColorName
     * @return hexColorName RRGGBBAA in lower-case hex notation
     */
    private static void appendColorName(StringBuilder sb, Color color) {
        int red = color.r();
        if (red < 16) {
            sb.append('0');
        }
        sb.append(Integer.toHexString(red));

        int green = color.g();
        if (green < 16) {
            sb.append('0');
        }
        sb.append(Integer.toHexString(green));

        int blue = color.b();
        if (blue < 16) {
            sb.append('0');
        }
        sb.append(Integer.toHexString(blue));

        int alpha = color.a();
        if (alpha < 16) {
            sb.append('0');
        }
        sb.append(Integer.toHexString(alpha));
    }

    /**
     * Method to convert the color string hex representation back to a color.
     * Package-only access as it is for internal use in ColorTextureAssetResolver,
     * but should be here for maintenance with the color-to-color-string code.
     *
     * @param hexColorName RRGGBBAA in lower-case hex notation
     * @return color represented by hexColorName
     */
    static/* package-only */Color getColorForColorName(String hexColorName) {
        if (hexColorName.length() != 8) {
            // TODO: we should probably log a warning in this case.
            return null;
        }

        String redString = hexColorName.substring(0, 2);
        String greenString = hexColorName.substring(2, 4);
        String blueString = hexColorName.substring(4, 6);
        String alphaString = hexColorName.substring(6);

        int red = Integer.parseInt(redString, 16);
        int green = Integer.parseInt(greenString, 16);
        int blue = Integer.parseInt(blueString, 16);
        int alpha = Integer.parseInt(alphaString, 16);
        return new Color(red, green, blue, alpha);
    }

    public static BufferedImage convertToImage(TextureRegion textureRegion) {
        final int width = textureRegion.getWidth();
        final int height = textureRegion.getHeight();

        final Rect2i pixelRegion = textureRegion.getPixelRegion();
        final Texture texture = textureRegion.getTexture();
        ByteBuffer textureBytes = texture.getData().getBuffers()[0];
        int stride = texture.getWidth() * 4;
        int posX = pixelRegion.minX();
        int posY = pixelRegion.minY();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = UnsignedBytes.toInt(textureBytes.get((posY + y) * stride + (posX + x) * 4));
                int g = UnsignedBytes.toInt(textureBytes.get((posY + y) * stride + (posX + x) * 4 + 1));
                int b = UnsignedBytes.toInt(textureBytes.get((posY + y) * stride + (posX + x) * 4 + 2));
                int a = UnsignedBytes.toInt(textureBytes.get((posY + y) * stride + (posX + x) * 4 + 3));

                int argb = (a << 24) + (r << 16) + (g << 8) + b;
                image.setRGB(x, y, argb);
            }
        }
        return image;
    }

    public static ByteBuffer convertToByteBuffer(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        ByteBuffer data = ByteBuffer.allocateDirect(4 * width * height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                int a = (argb >> 24) & 0xFF;
                data.put(UnsignedBytes.checkedCast(r));
                data.put(UnsignedBytes.checkedCast(g));
                data.put(UnsignedBytes.checkedCast(b));
                data.put(UnsignedBytes.checkedCast(a));
            }
        }
        data.rewind();
        return data;
    }
}
