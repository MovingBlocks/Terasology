/*
 * Copyright 2012
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

package org.terasology.asset.loaders;

import com.google.gson.Gson;
import org.newdawn.slick.opengl.PNGDecoder;
import org.terasology.asset.AssetLoader;
import org.terasology.asset.AssetUri;
import org.terasology.rendering.assets.Texture;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Immortius
 */
public class PNGTextureLoader implements AssetLoader<Texture> {
    private static class TextureMetadata {
        Texture.FilterMode filterMode;
        Texture.WrapMode wrapMode;
    }

    private Logger logger = Logger.getLogger(getClass().getName());
    private Gson gson;

    public PNGTextureLoader() {
        gson = new Gson();
    }

    @Override
    public Texture load(InputStream stream, AssetUri uri, List<URL> urls) throws IOException {
        PNGDecoder decoder = new PNGDecoder(stream);

        ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
        decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.RGBA);
        buf.flip();

        ByteBuffer data = buf;
        int height = decoder.getHeight();
        int width = decoder.getWidth();
        Texture.FilterMode filterMode = Texture.FilterMode.Nearest;
        Texture.WrapMode wrapMode = Texture.WrapMode.Clamp;

        for (URL url : urls) {
            if (url.toString().endsWith(".json")) {
                InputStreamReader reader = null;
                try {
                    reader = new InputStreamReader(url.openStream());
                    TextureMetadata metadata = gson.fromJson(reader, TextureMetadata.class);
                    if (metadata.filterMode != null) filterMode = metadata.filterMode;
                    if (metadata.wrapMode != null) wrapMode = metadata.wrapMode;
                }
                finally {
                    // JAVA7: Replace with new handling
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            logger.log(Level.SEVERE, "Error closing " + url.toString(), e);
                        }
                    }
                }
                break;
            }
        }

        return new Texture(uri, new ByteBuffer[] {data}, width, height, wrapMode, filterMode);
    }
}
