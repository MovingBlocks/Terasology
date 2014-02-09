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

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.newdawn.slick.opengl.PNGDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetLoader;
import org.terasology.engine.module.Module;
import org.terasology.utilities.gson.CaseInsensitiveEnumTypeAdapterFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author Immortius
 */
public class PNGTextureLoader implements AssetLoader<TextureData> {

    private static final Logger logger = LoggerFactory.getLogger(PNGTextureLoader.class);

    private Gson gson;

    public PNGTextureLoader() {
        gson = new GsonBuilder().registerTypeAdapterFactory(new CaseInsensitiveEnumTypeAdapterFactory()).create();
    }

    @Override
    public TextureData load(Module module, InputStream stream, List<URL> urls) throws IOException {
        InputStream pngStream = null;
        if (urls.get(0).toString().endsWith(".png")) {
            pngStream = stream;
        } else {
            for (URL url : urls) {
                if (url.toString().endsWith(".png")) {
                    pngStream = url.openStream();
                    break;
                }
            }
        }
        if (pngStream == null) {
            throw new IOException("Missing png to go with texture json");
        }
        try {
            PNGDecoder decoder = new PNGDecoder(pngStream);

            ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
            decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.RGBA);
            buf.flip();

            ByteBuffer data = buf;
            int height = decoder.getHeight();
            int width = decoder.getWidth();

            Texture.FilterMode filterMode = Texture.FilterMode.NEAREST;
            Texture.WrapMode wrapMode = Texture.WrapMode.CLAMP;
            Texture.Type type = Texture.Type.TEXTURE2D;

            // TODO: Change asset loader setup so that the default filter mode can be set per asset location
            if (urls.get(0).toString().contains("/fonts/")) {
                filterMode = Texture.FilterMode.LINEAR;
            }

            for (URL url : urls) {
                if (url.toString().endsWith(".texinfo")) {
                    try (InputStreamReader reader = new InputStreamReader(url.openStream(), Charsets.UTF_8)) {
                        TextureMetadata metadata = gson.fromJson(reader, TextureMetadata.class);
                        if (metadata.filterMode != null) {
                            filterMode = metadata.filterMode;
                        }
                        if (metadata.wrapMode != null) {
                            wrapMode = metadata.wrapMode;
                        }
                        if (metadata.type != null) {
                            type = metadata.type;
                        }
                    }
                    break;
                }
            }

            if (type == Texture.Type.TEXTURE3D) {
                final int byteLength = 4 * 16 * 16 * 16;
                final int strideX = 16 * 4;
                final int strideY = 16 * 16 * 4;
                final int strideZ = 4;

                if (width % height != 0 || width / height != height) {
                    throw new RuntimeException("3D texture must be cubic (height^3) - width must thus be a multiple of height");
                }

                ByteBuffer alignedBuffer = ByteBuffer.allocateDirect(byteLength);

                for (int x = 0; x < height; x++) {
                    for (int y = 0; y < height; y++) {
                        for (int z = 0; z < height; z++) {
                            final int index = x * strideX + z * strideZ + strideY * y;

                            alignedBuffer.put(data.get(index));
                            alignedBuffer.put(data.get(index + 1));
                            alignedBuffer.put(data.get(index + 2));
                            alignedBuffer.put(data.get(index + 3));
                        }
                    }
                }

                alignedBuffer.flip();

                return new TextureData(height, height, height, new ByteBuffer[]{alignedBuffer}, wrapMode, filterMode);
            } else {
                return new TextureData(width, height, new ByteBuffer[]{data}, wrapMode, filterMode);
            }
        } catch (UnsupportedOperationException e) {
            throw new IOException(e);
        } finally {
            pngStream.close();
        }
    }

    private static class TextureMetadata {
        Texture.FilterMode filterMode;
        Texture.WrapMode wrapMode;
        Texture.Type type;
    }
}
