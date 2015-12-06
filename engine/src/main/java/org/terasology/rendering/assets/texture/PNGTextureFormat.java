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

import de.matthiasmann.twl.utils.PNGDecoder;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.PathMatcher;
import java.util.List;

/**
 */
public class PNGTextureFormat extends AbstractAssetFileFormat<TextureData> {

    private Texture.FilterMode defaultFilterMode;
    private PathMatcher pathMatcher;

    public PNGTextureFormat(Texture.FilterMode defaultFilterMode, PathMatcher pathMatcher) {
        super("png");
        this.defaultFilterMode = defaultFilterMode;
        this.pathMatcher = pathMatcher;
    }

    @Override
    public TextureData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        try (InputStream pngStream = inputs.get(0).openStream()) {
            PNGDecoder decoder = new PNGDecoder(pngStream);

            ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
            decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
            buf.flip();

            int height = decoder.getHeight();
            int width = decoder.getWidth();

            Texture.FilterMode filterMode = defaultFilterMode;
            Texture.WrapMode wrapMode = Texture.WrapMode.CLAMP;

            return new TextureData(width, height, new ByteBuffer[]{buf}, wrapMode, filterMode);
        } catch (UnsupportedOperationException e) {
            throw new IOException(e);
        }
    }

    @Override
    public PathMatcher getFileMatcher() {
        return path -> super.getFileMatcher().matches(path) && pathMatcher.matches(path);
    }
}
