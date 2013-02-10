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
package org.terasology.audio.openAL;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetUri;
import org.terasology.utilities.OggReader;


public class OggStreamingSound extends OpenALStreamingSound {

    private static final Logger logger = LoggerFactory.getLogger(OggStreamingSound.class);
    private ByteBuffer dataBuffer = ByteBuffer.allocateDirect(4096 * 8);
    private OggReader file = null;


    public OggStreamingSound(AssetUri uri, URL source) {
        super(uri, source);
    }

    @Override
    public int getBufferBits() {
        return 16; // Ogg is always 16-bit
    }

    @Override
    public int getLength() {
        return -1; // not supported
    }

    @Override
    public int getChannels() {
        return file.getChannels();
    }

    @Override
    public int getSamplingRate() {
        return file.getRate();
    }

    @Override
    public void reset() {
        if (file != null) {
            try {
                file.close();
            } catch (IOException e) {
                logger.warn("Failed to close streaming sound: {}", getURI(), e);
            }
        }

        try {
            file = new OggReader(audioSource.openStream());
        } catch (IOException e) {
            logger.error("Failed to load streaming sound: {}", getURI(), e);
        }
    }

    @Override
    protected ByteBuffer fetchData() {
        try {
            int read = file.read(dataBuffer, 0, dataBuffer.capacity());
            dataBuffer.rewind();
            // do something :D
            if (read <= 0) {  // end of datastream
                return null;
            }

            return dataBuffer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
