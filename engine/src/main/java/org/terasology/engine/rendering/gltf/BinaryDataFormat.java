/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.rendering.gltf;

import com.google.common.io.ByteStreams;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;

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
