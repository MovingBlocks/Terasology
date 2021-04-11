// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.layout;

import gnu.trove.list.TByteList;

import java.nio.ByteBuffer;

public class ByteLayout extends Layout<TByteList> {
    public ByteLayout(int location, int size, TByteList buffer, int flags) {
        super(location, size, buffer, flags);
    }

    @Override
    public boolean hasContent() {
        return buffer.size() > 0;
    }

    @Override
    public void write(int index, ByteBuffer data) {
        for (int x = 0; x < size; x++) {
            data.put(buffer.get(index * size + x));
        }
    }

    @Override
    public int bytes() {
        return this.size * Byte.BYTES;
    }
}
