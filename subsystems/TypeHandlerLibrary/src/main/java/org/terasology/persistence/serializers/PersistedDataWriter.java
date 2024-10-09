// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.serializers;

import org.terasology.persistence.typeHandling.PersistedData;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Write {@link PersistedData} to files, stream, buffer, etc.
 */
public interface PersistedDataWriter<T extends PersistedData> {

    byte[] writeBytes(T data);

    void writeTo(T data, OutputStream outputStream) throws IOException;

    void writeTo(T data, ByteBuffer byteBuffer) throws IOException;
}
