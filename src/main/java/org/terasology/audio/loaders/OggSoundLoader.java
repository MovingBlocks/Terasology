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

package org.terasology.audio.loaders;

import com.google.common.io.ByteStreams;
import org.lwjgl.BufferUtils;
import org.terasology.asset.AssetLoader;
import org.terasology.asset.AssetUri;
import org.terasology.audio.StaticSoundData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author Immortius
 */
public class OggSoundLoader implements AssetLoader<StaticSoundData> {

    @Override
    public StaticSoundData load(AssetUri uri, InputStream stream, List<URL> urls) throws IOException {
        try (OggReader reader = new OggReader(stream)) {
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
