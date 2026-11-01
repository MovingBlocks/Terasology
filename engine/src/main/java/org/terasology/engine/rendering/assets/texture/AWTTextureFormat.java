// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.texture;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.format.AbstractAssetFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.gestalt.module.resources.FileReference;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static org.terasology.gestalt.assets.module.ModuleAssetScanner.OVERRIDE_FOLDER;

@RegisterAssetFileFormat
public class AWTTextureFormat extends AbstractAssetFileFormat<TextureData> {

    private Texture.FilterMode defaultFilterMode;
    private Predicate<FileReference> pathMatcher;

    public AWTTextureFormat() {
        this(Texture.FilterMode.NEAREST, path -> {
            if (path.getPath().get(0).equals(OVERRIDE_FOLDER)) {
                return path.getPath().get(2).equals("textures");
            } else {
                return path.getPath().get(1).equals("textures");
            }
        });
    }
    public AWTTextureFormat(Texture.FilterMode defaultFilterMode, Predicate<FileReference> pathMatcher) {
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
    public Predicate<FileReference> getFileMatcher() {
        return path -> super.getFileMatcher().test(path) && pathMatcher.test(path);
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
            IntStream.iterate(0, i -> i < data.length, i -> i + stride).forEach(i -> {
                buf.put(data, i + 2, 1); // R
                buf.put(data, i + 1, 1); // G
                buf.put(data, i + 0, 1); // B
                buf.put((byte) 255); // A
            });
        } else if (image.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
            IntStream.iterate(0, i -> i < data.length, i -> i + stride).forEach(i -> {
                buf.put(data, i + 3, 1); // R
                buf.put(data, i + 2, 1); // G
                buf.put(data, i + 1, 1); // B
                buf.put(data, i + 0, 1); // A
            });
        } else if (image.getType() == BufferedImage.TYPE_INT_ARGB) {
            IntStream.iterate(0, i -> i < data.length, i -> i + stride).forEach(i -> {
                buf.put(data, i + 1, 1); // R
                buf.put(data, i + 2, 1); // G
                buf.put(data, i + 3, 1); // B
                buf.put(data, i + 0, 1); // A
            });
        } else if (image.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
            final ColorModel cm = image.getColorModel();
            IntStream.iterate(0, i -> i < data.length, i -> i + stride).forEach(i -> {
                buf.put((byte) cm.getRed(data[i])); // R
                buf.put((byte) cm.getGreen(data[i])); // G
                buf.put((byte) cm.getBlue(data[i])); // B
                buf.put((byte) cm.getAlpha(data[i])); // A
            });
        } else {
            throw new IOException("Unsupported AWT format: " + image.getType());
        }
        buf.flip();

        return new TextureData(image.getWidth(), image.getHeight(), new ByteBuffer[]{buf}, Texture.WrapMode.CLAMP, filterMode);
    }

}
