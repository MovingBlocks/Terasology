// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.texture;

import com.google.common.primitives.UnsignedBytes;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.gestalt.naming.Name;
import org.terasology.nui.Color;
import org.terasology.nui.Colorc;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public final class TextureUtil {
    public static final Name COLOR_RESOURCE_NAME = new Name("Color");
    public static final Name NOISE_RESOURCE_NAME = new Name("Noise");

    private TextureUtil() {
    }

    /**
     * Returns a AssetUri which represents a Texture of that color.
     *
     * @param color including alpha, of the texture to represent.
     * @return an asset Uri for the texture
     */
    public static ResourceUrn getTextureUriForColor(Colorc color) {
        return new ResourceUrn(TerasologyConstants.ENGINE_MODULE, COLOR_RESOURCE_NAME, new Name(color.toHex()));
    }

    /**
     * Returns a AssetUri which represents a Texture that contains white noise
     *
     * @param size the size of the texture (both width and height)
     * @param seed the seed value for the noise generator
     * @param min  the minimum noise value (can be lower than 0 and will be clamped then)
     * @param max  the minimum noise value (can be larger than 255 and will be clamped then)
     * @return an asset Uri for the texture
     */
    public static ResourceUrn getTextureUriForWhiteNoise(int size, long seed, int min, int max) {

        String name = String.format("%s.%d.%d.%d.%d", "white", size, seed, min, max);

        return new ResourceUrn(TerasologyConstants.ENGINE_MODULE, NOISE_RESOURCE_NAME, new Name(name));
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

        final Rectanglei pixelRegion = textureRegion.getPixelRegion();
        final Texture texture = textureRegion.getTexture();
        ByteBuffer textureBytes = texture.getData().getBuffers()[0];
        int stride = texture.getWidth() * 4;
        int posX = pixelRegion.minX;
        int posY = pixelRegion.minY;

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

    /**
     * Converts a BufferedImage into a ByteBuffer based on 32-bit values
     * in RGBA byte order
     *
     * @param image any type of BufferedImage
     * @return a ByteBuffer that contains the data in RGBA byte order
     */
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
