// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.inMemory;

import java.nio.ByteBuffer;

// TODO, see https://github.com/MovingBlocks/Terasology/issues/5176 for reasoning.
@SuppressWarnings({"PMD.ArrayIsStoredDirectly", "PMD.MethodReturnsInternalArray"})
public class PersistedBytes extends AbstractPersistedData {

    private final byte[] bytes;

    public PersistedBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public byte[] getAsBytes() {
        return bytes;
    }

    @Override
    public ByteBuffer getAsByteBuffer() {
        return ByteBuffer.wrap(bytes);
    }

    @Override
    public boolean isBytes() {
        return true;
    }
}
