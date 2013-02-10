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

package org.terasology.audio.loaders;

import static org.lwjgl.openal.AL10.alGenBuffers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.terasology.asset.AssetLoader;
import org.terasology.asset.AssetUri;
import org.terasology.audio.openAL.OggSound;
import org.terasology.audio.openAL.OpenALException;
import org.terasology.audio.Sound;
import org.terasology.utilities.OggReader;

/**
 * @author Immortius
 */
public class OggSoundLoader implements AssetLoader<Sound> {

    @Override
    public Sound load(AssetUri uri, InputStream stream, List<URL> urls) throws IOException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            OggReader reader = new OggReader(stream);

            byte buffer[] = new byte[1024];
            int read;
            int totalRead = 0;

            do {
                read = reader.read(buffer, 0, buffer.length);

                if (read < 0) {
                    break;
                }

                totalRead += read;

                bos.write(buffer, 0, read);
            } while (read > 0);

            buffer = bos.toByteArray();

            ByteBuffer data = BufferUtils.createByteBuffer(totalRead).put(buffer);
            data.flip();

            int channels = reader.getChannels();
            int sampleRate = reader.getRate();
            int bufferId = alGenBuffers();
            AL10.alBufferData(bufferId, channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, data, sampleRate);

            OpenALException.checkState("Uploading buffer");
            return new OggSound(uri, bufferId);
        } catch (IOException e) {
            throw new IOException("Failed to load sound: " + e.getMessage(), e);
        }
    }
}
