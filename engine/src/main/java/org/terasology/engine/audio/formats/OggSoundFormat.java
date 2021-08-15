// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.audio.formats;

import com.google.common.io.ByteStreams;
import org.lwjgl.BufferUtils;
import org.terasology.engine.audio.StaticSoundData;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.format.AbstractAssetFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetFileFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

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
