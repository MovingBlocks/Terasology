/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.rendering.assetLoaders;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;

import org.newdawn.slick.opengl.PNGDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetLoader;
import org.terasology.asset.AssetUri;
import org.terasology.rendering.assets.Texture;

import com.google.gson.Gson;

/**
 * @author Immortius
 */
public class PNGTextureLoader implements AssetLoader<Texture> {

    private static class TextureMetadata {
        Texture.FilterMode filterMode;
        Texture.WrapMode wrapMode;
    }

    private static final Logger logger = LoggerFactory.getLogger(PNGTextureLoader.class);

    private Gson gson;

    public PNGTextureLoader() {
        gson = new Gson();
    }

    @Override
    public Texture load(AssetUri uri, InputStream stream, List<URL> urls) throws IOException {
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
            decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
            buf.flip();

            ByteBuffer data = buf;
            int height = decoder.getHeight();
            int width = decoder.getWidth();

            Texture.FilterMode filterMode = Texture.FilterMode.Nearest;
            Texture.WrapMode wrapMode = Texture.WrapMode.Clamp;

            // TODO: Change asset loader setup so that the default filter mode can be set per asset location
            if (urls.get(0).toString().contains("/fonts/")) {
                filterMode = Texture.FilterMode.Linear;
            }


            for (URL url : urls) {
                if (url.toString().endsWith(".json")) {
                    InputStreamReader reader = null;
                    try {
                        reader = new InputStreamReader(url.openStream());
                        TextureMetadata metadata = gson.fromJson(reader, TextureMetadata.class);
                        if (metadata.filterMode != null) filterMode = metadata.filterMode;
                        if (metadata.wrapMode != null) wrapMode = metadata.wrapMode;
                    } finally {
                        // JAVA7: Replace with new handling
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e) {
                                logger.error("Error closing {}", url.toString(), e);
                            }
                        }
                    }
                    break;
                }
            }

            return new Texture(uri, new ByteBuffer[]{data}, width, height, wrapMode, filterMode);
        } finally {
            pngStream.close();
        }
    }
}
