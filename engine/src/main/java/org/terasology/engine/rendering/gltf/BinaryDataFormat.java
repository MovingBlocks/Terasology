// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf;

import com.google.common.io.ByteStreams;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.format.AbstractAssetFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetFileFormat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Asset format for reading binary files into a byte array, primarily to support glTF
 */
@RegisterAssetFileFormat
public class BinaryDataFormat extends AbstractAssetFileFormat<ByteBufferData> {

    public BinaryDataFormat() {
        super("bin");
    }

    @Override
    public ByteBufferData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        try (InputStream in = inputs.get(0).openStream()) {
            byte[] data = ByteStreams.toByteArray(in);
            return new ByteBufferData(data);
        }
    }
}
