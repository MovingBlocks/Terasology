/*
 * Copyright 2016 MovingBlocks
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

import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.assets.module.ModuleAssetDataProducer;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.awt.image.ColorModel;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.PathMatcher;
import java.util.List;
import javax.imageio.ImageIO;

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
            final WritableRaster raster = img.getRaster();
            final DataBufferByte dbf = (DataBufferByte) raster.getDataBuffer();
            byte[] data = dbf.getData();
            final int stride = data.length / (img.getWidth() * img.getHeight());
            // Sanity check
            if (stride > 4) {
                throw new IOException("Image data makes no sense (got "
                                      + data.length + ", expected "
                                      + img.getWidth() * img.getHeight() * stride
                                      + ")");
            }
            final ByteBuffer buf = ByteBuffer.allocateDirect(4 * img.getWidth() * img.getHeight());

            // Convert AWT image to proper internal format
            if (img.getType() == BufferedImage.TYPE_3BYTE_BGR) {
                for (int i = 0; i < data.length; i += stride) {
                    buf.put(data, i + 2, 1); // R
                    buf.put(data, i + 1, 1); // G
                    buf.put(data, i + 0, 1); // B
                    buf.put((byte) 255); // A
                }
            } else if (img.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
                for (int i = 0; i < data.length; i += stride) {
                    buf.put(data, i + 3, 1); // R
                    buf.put(data, i + 2, 1); // G
                    buf.put(data, i + 1, 1); // B
                    buf.put(data, i + 0, 1); // A
                }
            } else if (img.getType() == BufferedImage.TYPE_INT_ARGB) {
                for (int i = 0; i < data.length; i += stride) {
                    buf.put(data, i + 1, 1); // R
                    buf.put(data, i + 2, 1); // G
                    buf.put(data, i + 3, 1); // B
                    buf.put(data, i + 0, 1); // A
                }
            } else if (img.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
                final ColorModel cm = img.getColorModel();
                for (int i = 0; i < data.length; i += stride) {
                    buf.put((byte) cm.getRed(data[i])); // R
                    buf.put((byte) cm.getGreen(data[i])); // G
                    buf.put((byte) cm.getBlue(data[i])); // B
                    buf.put((byte) cm.getAlpha(data[i])); // A
                }
            } else {
                throw new IOException("Unsuported AWT format: " + img.getType());
            }
            buf.flip();

            Texture.FilterMode filterMode = defaultFilterMode;
            Texture.WrapMode wrapMode = Texture.WrapMode.CLAMP;

            return new TextureData(img.getWidth(), img.getHeight(), new ByteBuffer[]{buf}, wrapMode, filterMode);
        } catch (UnsupportedOperationException e) {
            throw new IOException(e);
        }
    }

    @Override
    public PathMatcher getFileMatcher() {
        return path -> super.getFileMatcher().matches(path) && pathMatcher.matches(path);
    }
}
