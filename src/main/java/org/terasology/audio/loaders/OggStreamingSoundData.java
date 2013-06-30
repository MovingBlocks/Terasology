/*
 * Copyright 2013 Moving Blocks
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.audio.StreamingSoundData;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 *
 */
public class OggStreamingSoundData implements StreamingSoundData {

    private static Logger logger = LoggerFactory.getLogger(OggStreamingSoundData.class);

    private URL url;
    private OggReader reader;

    public OggStreamingSoundData(URL url) throws IOException {
        this.url = url;
        reader = new OggReader(url.openStream());
    }

    @Override
    public ByteBuffer readNextInto(ByteBuffer dataBuffer) {
        try {
            int read = reader.read(dataBuffer, 0, dataBuffer.capacity());
            dataBuffer.rewind();
            return dataBuffer;
        } catch (IOException e) {
            throw new RuntimeException("Error reading from sound stream at " + url, e);
        }
    }

    @Override
    public int getChannels() {
        return reader.getChannels();
    }

    @Override
    public int getBufferBits() {
        return 16;
    }

    @Override
    public int getSamplingRate() {
        return reader.getRate();
    }

    @Override
    public void reset() {
        if (reader != null) {
            dispose();
        }
        try {
            reader = new OggReader(url.openStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to reset ogg stream from " + url, e);
        }
    }

    @Override
    public void dispose() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                logger.error("Failed to close ogg streaming from {}", url, e);
            }
            reader = null;
        }
    }
}