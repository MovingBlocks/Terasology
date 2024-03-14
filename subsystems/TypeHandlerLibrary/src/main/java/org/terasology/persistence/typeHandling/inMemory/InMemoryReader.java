// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.inMemory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.serializers.PersistedDataReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;

public class InMemoryReader implements PersistedDataReader<AbstractPersistedData> {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryReader.class);

    @Override
    public AbstractPersistedData read(InputStream inputStream) throws IOException {
        ObjectInputStream ois = new ObjectInputStream(inputStream);
        try {
            return (AbstractPersistedData) ois.readObject();
        } catch (ClassNotFoundException e) {
            logger.error("Cannot read to inputStream");
            return InMemoryPersistedDataSerializer.NULL;
        }
    }

    @Override
    public AbstractPersistedData read(byte[] byteBuffer) throws IOException {
        return read(new ByteArrayInputStream(byteBuffer));
    }

    @Override
    public AbstractPersistedData read(ByteBuffer byteBuffer) throws IOException {
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        return read(bytes);
    }
}
