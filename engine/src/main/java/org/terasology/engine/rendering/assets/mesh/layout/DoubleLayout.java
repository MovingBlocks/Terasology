// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.layout;

import gnu.trove.list.TDoubleList;

import java.nio.ByteBuffer;

public class DoubleLayout extends Layout<TDoubleList> {
    public DoubleLayout(int location, int size, TDoubleList buffer, int flags) {
        super(location, size, buffer, flags | Layout.FLOATING_POINT);
    }

    @Override
    public boolean hasContent() {
        return buffer.size() > 0;
    }

    @Override
    public void write(int index, ByteBuffer data) {
        for (int x = 0; x < size; x++) {
            data.putDouble(buffer.get(index * size + x));
        }
    }

    @Override
    public int bytes() {
        return this.size * Double.BYTES;
    }
}
