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

package org.terasology.audio.formats;

import com.google.common.io.ByteStreams;
import org.lwjgl.BufferUtils;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.audio.StaticSoundData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 */
@RegisterAssetFileFormat
public class OggSoundFormat extends AbstractAssetFileFormat<StaticSoundData> {

    public OggSoundFormat() {
        super("ogg");
    }

    @Override
    public StaticSoundData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        try (OggReader reader = new OggReader(inputs.get(0).openStream())) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ByteStreams.copy(reader, bos);

            ByteBuffer data = BufferUtils.createByteBuffer(bos.size()).put(bos.toByteArray());
            data.flip();

            return new StaticSoundData(data, reader.getChannels(), reader.getRate(), 16);
        } catch (IOException e) {
            throw new IOException("Failed to load sound: " + e.getMessage(), e);
        }
    }
}
