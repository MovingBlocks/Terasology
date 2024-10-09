// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.bytebuffer;

import org.terasology.persistence.serializers.PersistedDataWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ByteBufferDataWriter implements PersistedDataWriter<ByteBufferPersistedData> {
    @Override
    public byte[] writeBytes(ByteBufferPersistedData data) {
        return data.getData().array();
    }

    @Override
    public void writeTo(ByteBufferPersistedData data, OutputStream outputStream) throws IOException {
        throw new UnsupportedOperationException("Idk which size are you using");
    }

    @Override
    public void writeTo(ByteBufferPersistedData data, ByteBuffer byteBuffer) throws IOException {
        byteBuffer.put(data.getAsByteBuffer());
    }
}
