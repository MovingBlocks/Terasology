// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.audio.formats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.audio.StreamingSoundData;
import org.terasology.gestalt.assets.format.AssetDataFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;


public class OggStreamingSoundData implements StreamingSoundData {

    private static final Logger logger = LoggerFactory.getLogger(OggStreamingSoundData.class);

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
