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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.audio.StreamingSoundData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 *
 */
public class OggStreamingSoundData implements StreamingSoundData {

    private static Logger logger = LoggerFactory.getLogger(OggStreamingSoundData.class);

    private final AssetDataFile stream;
    private OggReader reader;

    public OggStreamingSoundData(AssetDataFile stream) {
        this.stream = stream;
        reset();
    }

    @Override
    public ByteBuffer readNextInto(ByteBuffer dataBuffer) {
        try {
            reader.read(dataBuffer, 0, dataBuffer.capacity());
            dataBuffer.flip();
            return dataBuffer;
        } catch (IOException e) {
            throw new RuntimeException("Error reading from sound stream", e);
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
           reader = AccessController.doPrivileged((PrivilegedExceptionAction<OggReader>) () -> new OggReader(stream.openStream()));
        } catch (PrivilegedActionException e) {
            throw new RuntimeException("Failed to reset ogg stream", e);
        }
    }

    @Override
    public void dispose() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                logger.error("Failed to close ogg streaming", e);
            }
            reader = null;
        }
    }
}
