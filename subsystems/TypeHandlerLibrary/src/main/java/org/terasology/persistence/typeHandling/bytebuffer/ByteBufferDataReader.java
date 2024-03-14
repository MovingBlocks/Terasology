// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.bytebuffer;

import org.terasology.persistence.serializers.PersistedDataReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferDataReader implements PersistedDataReader<ByteBufferPersistedData> {
    @Override
    public ByteBufferPersistedData read(InputStream inputStream) throws IOException {
        throw new UnsupportedOperationException("Idk which size are you using");
    }

    @Override
    public ByteBufferPersistedData read(byte[] byteBuffer) throws IOException {
        return new ByteBufferPersistedData(ByteBuffer.wrap(byteBuffer).asReadOnlyBuffer());
    }

    @Override
    public ByteBufferPersistedData read(ByteBuffer byteBuffer) throws IOException {
        return new ByteBufferPersistedData(byteBuffer);
    }
}
