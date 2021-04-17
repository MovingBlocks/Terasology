// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

public class IndexResource {
    public final ByteBuffer buffer;
    public final int num;
    private int position = 0;

    public IndexResource(int num) {
        this.num = num;
        this.buffer = BufferUtils.createByteBuffer(num * Integer.BYTES);
    }

    public void put(int value) {
        buffer.putInt(position,value);
        position += Integer.BYTES;
    }
}
