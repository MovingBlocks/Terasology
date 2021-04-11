// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.layout;

import gnu.trove.list.TIntList;

import java.nio.ByteBuffer;

public class IntLayout extends Layout<TIntList> {
    public IntLayout(int location, int size, TIntList buffer) {
        super(location, size, buffer);
    }

    @Override
    public boolean hasContent() {
        return this.buffer.size() > 0;
    }

    @Override
    public void write(int index, ByteBuffer data) {
        for (int x = 0; x < size; x++) {
            data.putInt(buffer.get(index * size + x));
        }
    }

    @Override
    public int bytes() {
        return this.size * Integer.BYTES;
    }

}
