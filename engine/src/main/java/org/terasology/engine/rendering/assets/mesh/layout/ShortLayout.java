// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.layout;

import gnu.trove.list.TShortList;

import java.nio.ByteBuffer;

public class ShortLayout extends Layout<TShortList> {
    public ShortLayout(int location, int size, TShortList buffer, long flags) {
        super(location, size, buffer, flags);
    }

    @Override
    public boolean hasContent() {
        return buffer.size() > 0;
    }

    @Override
    public void write(int index, ByteBuffer data) {
        for (int x = 0; x < size; x++) {
            data.putShort(buffer.get(index * size + x));
        }
    }

    @Override
    public int bytes() {
        return this.size * Short.BYTES;
    }
}
