// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.inMemory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.serializers.PersistedDataWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class InMemoryWriter implements PersistedDataWriter<AbstractPersistedData> {
    private static final  Logger logger = LoggerFactory.getLogger(InMemoryWriter.class);

    @Override
    public byte[] writeBytes(AbstractPersistedData data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            writeTo(data, baos);
        } catch (IOException e) {
            logger.error("Cannot writeBytes", e);
        }
        return baos.toByteArray();
    }

    @Override
    public void writeTo(AbstractPersistedData data, OutputStream outputStream) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(data);
    }

    @Override
    public void writeTo(AbstractPersistedData data, ByteBuffer byteBuffer) throws IOException {
        byteBuffer.put(writeBytes(data));
    }
}
