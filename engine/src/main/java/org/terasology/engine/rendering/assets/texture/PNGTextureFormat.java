// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.texture;

import de.matthiasmann.twl.utils.PNGDecoder;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.format.AbstractAssetFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.module.resources.FileReference;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Predicate;

/**
 */
public class PNGTextureFormat extends AbstractAssetFileFormat<TextureData> {

    private Texture.FilterMode defaultFilterMode;
    private final Predicate<FileReference> pathMatcher;


    public PNGTextureFormat(Texture.FilterMode defaultFilterMode, Predicate<FileReference> pathMatcher) {
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
    public Predicate<FileReference> getFileMatcher() {
        return path -> super.getFileMatcher().test(path) && pathMatcher.test(path);
    }
}
