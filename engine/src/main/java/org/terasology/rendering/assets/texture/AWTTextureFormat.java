// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.texture;

import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.module.ModuleAssetDataProducer;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.file.PathMatcher;
import java.util.List;

/**
 */
@RegisterAssetFileFormat
public class AWTTextureFormat extends AbstractAssetFileFormat<TextureData> {

    private Texture.FilterMode defaultFilterMode;
    private PathMatcher pathMatcher;

    public AWTTextureFormat() {
        this(Texture.FilterMode.NEAREST, path -> {
                if (path.getName(1).toString().equals(ModuleAssetDataProducer.OVERRIDE_FOLDER)) {
                    return path.getName(3).toString().equals("textures");
                } else {
                    return path.getName(2).toString().equals("textures");
                }
            });
    }
    public AWTTextureFormat(Texture.FilterMode defaultFilterMode, PathMatcher pathMatcher) {
        super("jpeg", "jpg", "bmp", "gif", "png");
        this.defaultFilterMode = defaultFilterMode;
        this.pathMatcher = pathMatcher;
    }

    @Override
    public TextureData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        try (InputStream inStream = inputs.get(0).openStream()) {
            final BufferedImage img = ImageIO.read(inStream);
            return convertToTextureData(img, defaultFilterMode);
        } catch (UnsupportedOperationException e) {
            throw new IOException(e);
        }
    }

    @Override
    public PathMatcher getFileMatcher() {
        return path -> super.getFileMatcher().matches(path) && pathMatcher.matches(path);
    }

    /**
     * Converts a BufferedImage into a TextureData.
     *
     * @param image any type of BufferedImage
     * @param filterMode any method used to determine the texture color for a texture mapped pixel
     * @return a TextureData that contains the data from buffered image
     */
    public static TextureData convertToTextureData(final BufferedImage image, Texture.FilterMode filterMode) throws IOException {
        final WritableRaster raster = image.getRaster();
        final DataBufferByte dbf = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dbf.getData();
        final int stride = data.length / (image.getWidth() * image.getHeight());
        // Sanity check
        if (stride > 4) {
            throw new IOException("Image data is corrupted! We expect less number of pixels: "
              + image.getWidth() * image.getHeight() * stride + ", instead of " + data.length);
        }
        final ByteBuffer buf = ByteBuffer.allocateDirect(4 * image.getWidth() * image.getHeight());

        // Convert AWT image to proper internal format
        if (image.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            for (int i = 0; i < data.length; i += stride) {
                buf.put(data, i + 2, 1); // R
                buf.put(data, i + 1, 1); // G
                buf.put(data, i + 0, 1); // B
                buf.put((byte) 255); // A
            }
        } else if (image.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
            for (int i = 0; i < data.length; i += stride) {
                buf.put(data, i + 3, 1); // R
                buf.put(data, i + 2, 1); // G
                buf.put(data, i + 1, 1); // B
                buf.put(data, i + 0, 1); // A
            }
        } else if (image.getType() == BufferedImage.TYPE_INT_ARGB) {
            for (int i = 0; i < data.length; i += stride) {
                buf.put(data, i + 1, 1); // R
                buf.put(data, i + 2, 1); // G
                buf.put(data, i + 3, 1); // B
                buf.put(data, i + 0, 1); // A
            }
        } else if (image.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
            final ColorModel cm = image.getColorModel();
            for (int i = 0; i < data.length; i += stride) {
                buf.put((byte) cm.getRed(data[i])); // R
                buf.put((byte) cm.getGreen(data[i])); // G
                buf.put((byte) cm.getBlue(data[i])); // B
                buf.put((byte) cm.getAlpha(data[i])); // A
            }
        } else {
            throw new IOException("Unsupported AWT format: " + image.getType());
        }
        ((Buffer) buf).flip(); // Explicitly casting for Java11/Java8 compability. problem at bytecode level

        return new TextureData(image.getWidth(), image.getHeight(), new ByteBuffer[]{buf}, Texture.WrapMode.CLAMP, filterMode);
    }

}
