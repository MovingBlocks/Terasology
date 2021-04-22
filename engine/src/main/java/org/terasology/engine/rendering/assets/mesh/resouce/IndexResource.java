// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resouce;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

public class IndexResource {
    public final ByteBuffer buffer;
    public final int num;
    private int posIndex = 0;
    private int[] store;

    public IndexResource(int num, boolean cpuReadable) {
        this.num = num;
        this.buffer = BufferUtils.createByteBuffer(num * Integer.BYTES);
        if (cpuReadable) {
            store = new int[num];
        }
    }

    public int[] getStore() {
        return store;
    }

    public void rewind() {
        posIndex = 0;
    }

    public void put(int value) {
        buffer.putInt(posIndex * Integer.BYTES, value);
        if (store != null) {
            store[posIndex] = value;
        }
        posIndex++;
    }

    public void put(int index, int value) {
        buffer.putInt(index * Integer.BYTES, value);
        if (store != null) {
            store[index] = value;
        }
    }

    public void map(int startIndex, int endIndex, int[] arr, int index) {
        int i = 0;
        for (int x = startIndex; x < endIndex; x++, i++) {
            this.buffer.putInt((i + index) * Integer.BYTES, arr[x]);
            if (store != null) {
                store[i] = arr[x];
            }
        }
    }
}
