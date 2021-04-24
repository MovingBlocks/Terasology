// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.layout;

import java.nio.ByteBuffer;

public abstract class Layout<T> {
    public static final int NONE = 0x0;
    public static final int FLOATING_POINT = 0x1;
    public static final int NORMALIZED = 0x2;

    public final int location;
    public final int size;
    public final T buffer;
    public final long flag;

    public Layout(int location, int size, T buffer, long flag) {
        this.location = location;
        this.size = size;
        this.buffer = buffer;
        this.flag = flag;
    }

    public abstract boolean hasContent();

    public abstract void write(int index, ByteBuffer data);

    public abstract int bytes();
}
